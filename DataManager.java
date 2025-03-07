import java.util.*;
import java.io.*;
import java.awt.*;

public class DataManager implements Serializable{

  //importing related instance variables
  private static final String[] OP_CODE = {"VALUE","RANGE","SERIES"}; //importance discussed in importData()
  private final String DIRECTORY;
  private String[] CATAGORY;
  private HashMap<String,ArrayList<String>> FILTER;
  private int[] DATE;
  private boolean noFilters;
  private boolean argumentsAlreadyParsed = false;

  //post import, analysis instance variables
  private ArrayList<ArrayList<String>> DATA = new ArrayList<ArrayList<String>>();
  private ArrayList<String> DATA_CATAGORY = new ArrayList<String>();
  private HashMap<String,HashMap<String,String>> LOOKUP_TABLE = new HashMap<String,HashMap<String,String>>();

  //misc
  private transient boolean debugging = true;



  public DataManager(String dir){
    DIRECTORY = dir; //enables the client to use any set of files across any directory
  }

  public DataManager(){
    String cd = System.getProperty("user.dir"); //determines current working directory
    if(cd.substring(cd.length()-5).equals("\\data"))
      DIRECTORY = cd; //works in actual current working directory
    else
      DIRECTORY = cd +"\\data"; //works in current subdirectory "data"
  }

  public void parseArgument(String argument) throws IllegalArgumentException, IllegalCallerException{
    //-------EXAMPLE INPUT FORMAT--------
    //catagorys:filters:dates
    //cat1,cat2,catN:cat1.val1,cat2.(range1,range2),cat3.[val1,val2,val3]:date1-date2
    //cat1 , cat2 , catN : cat1.val1 , cat2.(startRange-endRange) , cat3.[val1-val2-val3-valN] : date1 , date2
                  //checks over the truth table below for possible argument combinations and their resultant authencitiy...
                  //true means the arugument is accepted, false means it is not allowed
                  /*  C:F:D   true
                       :F:D   true
                      C: :D   true
                      C:F:    false
                      C: :    false
                       :F:    false
                       : :D   false
                       : :    false */

    //check if already ran before
      if(argumentsAlreadyParsed)
        throw new IllegalCallerException("Instances of DataManager can only parse one argument. Please create another instance for another argument.");

      if( argument.substring(0,2).equals("::") || argument.substring(argument.length()-1,argument.length()).equals(":") ){
        throw new IllegalArgumentException("Arguments must be in Catagories:Filters:Dates format");
      }
      if( argument.equals("::") ){
        throw new IllegalArgumentException("Empty argument");
      }

    //splits catagories from filters (if necessary)
      String[] mainArg = argument.split(":");
        if(mainArg.length>3) throw new IllegalArgumentException("Incorrect entry. Only one \":\" delimiter per condition statement. Erroneous \":\" count: "+(mainArg.length-1));
      String categoryArg = mainArg[0];
      String filterPairs = mainArg[1];
      String dateArg = mainArg[2];

    //Assigns catagories... very easy compared to filters and dates
      CATAGORY = categoryArg.split(",");


    //Assigns filters... if only filters were as simple as catagories and dates :(
      FILTER = new HashMap<String,ArrayList<String>>();

      //checks for filters
        noFilters = filterPairs.length()==0 ? true : false;

      //IF THEY'RE FILTERS, goes through every filter pair
      if(!noFilters)
      for(String pair : filterPairs.split(",")){

        //splits into raw keys and values
        String[] pairArr = pair.split("\\.");
          if(pairArr.length==1) throw new IllegalArgumentException("Filters must inputted as a key/value pair with one \".\" delimiter.");
          if(pairArr.length!=2) throw new IllegalArgumentException("Incorrect pairing. Only one \".\" delimiter per pair. Erroneous \".\" count: "+(pairArr.length-1));
        String key = pairArr[0];
        String value = pairArr[1];

        //the arraylist to be passed into the HashMap as the value
        ArrayList<String> valueList;

        //determines which kind of value holder (ordinary value, range, series)
        switch(value.substring(0,1)){
          //range
          case "(" -> {
            //instantiates the ArrayList that will be the value of the filter HashMap
            valueList = new ArrayList<String>(3);
            valueList.add(OP_CODE[1]); //indicates the ArrayList contains a range of values

            //substrings out the (), splits into two arguments
            String[] rangeArgs = value.substring(1,value.length()-1).split("-");
              if(rangeArgs.length!=2) throw new IllegalArgumentException("Range must consist of 2 arguments, not "+rangeArgs.length);

            for(String realValue : rangeArgs){
              valueList.add(realValue);
            }

            FILTER.put(key, valueList);
          }
          //series
          case "[" -> {
            //instantiates the ArrayList that will be the value of the filter HashMap
            valueList = new ArrayList<String>();
            valueList.add(OP_CODE[2]); //indicates the ArrayList contains a series of values

            //substrings out the [], splits/checks for multiple values
            String[] seriesArgs = value.substring(1,value.length()-1).split("-");
              if(seriesArgs.length<=1) throw new IllegalArgumentException("Series must consist of 2+ arguments, not "+seriesArgs.length);

            for(String realValue : seriesArgs){
              valueList.add(realValue);
            }

            FILTER.put(key, valueList);
          }
          //ordinary value
          default -> {
            //instantiates the ArrayList that will be the value of the filter HashMap
            valueList = new ArrayList<String>(2);

            valueList.add(OP_CODE[0]); //indicates the ArrayList contains only a single value
            valueList.add(value); //adds value

            FILTER.put(key, valueList);
          }
        }
      } //end of filter pair loop

    //imports Dates... very easy like catagories (just now with String->int conversion)
    boolean dateRange = dateArg.substring(0,1).equals("(") ? true : false;

    if(dateRange){
      dateArg = dateArg.substring(1,dateArg.length()-1); //removes trailing paranthesis
      String temp[] = dateArg.split("-");
      int lowerBoundary = Integer.parseInt(temp[0]);
      int upperBoundary = Integer.parseInt(temp[1]);
      int range = upperBoundary - lowerBoundary;

      //finds only valid files (this helps when the client imports a range of date files)
      ArrayList<Integer> filteredDate = new ArrayList<Integer>();
      for(int date=lowerBoundary;date<=upperBoundary;date++)
        if( (new File(DIRECTORY + "\\" + date + ".csv")).exists() )
          filteredDate.add(date);

      DATE = new int[filteredDate.size()];
      for(int x=0;x<DATE.length;x++)
        DATE[x] = filteredDate.get(x);

    }
    else{
      String temp[] = dateArg.split(",");
      DATE = new int[temp.length];
      for(int x=0;x<temp.length;x++){
        //converts string to int
        try{
          DATE[x] = Integer.parseInt(temp[x]);
        }
        catch(NumberFormatException e){
          e.printStackTrace();
        }
      }
    }

    //ensures this method is only ran once per instance of DataManager
    argumentsAlreadyParsed = true;

    importData();
  }

  public void importData() throws IllegalArgumentException{


    //ArrayList to hold all sucsessful rows through filtering
      ArrayList<String[]> filteredRows = new ArrayList<String[]>();

    //records if the file loop has already ran once. Useful for one time code segments
      boolean firstIteration = true;

    for(int fileIndex=0 ; fileIndex < DATE.length ; fileIndex++){
      //import file
        String fileName = "" + DATE[fileIndex] + ".csv";
        File data = null;
        Scanner kb = null;
        try{
          data = new File(DIRECTORY+"\\"+fileName);
          kb = new Scanner(data);
        }
        catch(FileNotFoundException e){
          e.printStackTrace();
        }

      //establishes headers
        String[] HEADER = (kb.next()).split(",");

      //locates filter keys (csv headers) and their index BEFORE reading every row
        ArrayList<Integer> keyIndex = new ArrayList<Integer>();

        FILTER.entrySet().forEach(entry -> {
          String targetHeader = entry.getKey();
          for(int x=0;x<HEADER.length;x++){
            if(HEADER[x].equals(targetHeader)){
              keyIndex.add(x);
            }
          }
        });

      //CAUTION: VERY COMPUTATIONALLY INTENSIVE
      //iterates through EVERY line of the CSV
        //if no filters
        if(noFilters){
          while(kb.hasNext()){
            final String[] row = kb.next().split(",");
            filteredRows.add(row);
          }
        }
        //if filters
        else{
          while(kb.hasNext()){

            final String[] row = kb.next().split(",");

            //filter VALUE matches
            FILTER.entrySet().forEach(entry -> {

              final String typeOfValue = entry.getValue().get(0); //one of the OP_CODE's (value, range, series)
              switch(typeOfValue){
                case "VALUE" -> {

                  for(int i : keyIndex)
                    if(row[i].equals(entry.getValue().get(1)))
                      filteredRows.add(row);

                }
                case "RANGE" -> {

                  for(int i : keyIndex)
                    if( Integer.parseInt(entry.getValue().get(1)) <= Integer.parseInt(row[i]) &&
                        Integer.parseInt(entry.getValue().get(2)) >= Integer.parseInt(row[i])   )
                      filteredRows.add(row);

                }
                case "SERIES" -> {

                  for(String value : entry.getValue())
                    for(int i : keyIndex)
                      if(row[i].equals(value))
                        filteredRows.add(row);

                }
              }
            });
          }
        }

      //closes the Scanner
        kb.close();

      //finds valid catagories/headers to only include in final data
        ArrayList<Integer> catIndex = new ArrayList<Integer>();
        for(int x=0;x<HEADER.length;x++){
          for(String cat : CATAGORY)
            if(HEADER[x].equals(cat))
              catIndex.add(x);
        }

        //adds the headers to the DATA_CATAGORY
          boolean noCatagories = CATAGORY.length == 1 && CATAGORY[0].length() == 0;

          if(firstIteration){
            if(noCatagories){
              ArrayList<String> tempRow = new ArrayList<String>();
              for(String header : HEADER){
                DATA_CATAGORY.add(header);
              }
            }
            else{
              ArrayList<String> tempRow = new ArrayList<String>();
              for(int i : catIndex){
                DATA_CATAGORY.add(HEADER[i]);
              }
            }
          }



      //iterates through valid filteredRows
        for(String[] row : filteredRows){
          ArrayList<String> tempRow = new ArrayList<String>();
          //if zero or one catagory was passed in, print the entire row
          if(noCatagories){
            for(int x=0;x<row.length;x++)
              tempRow.add(row[x]);
          }
          //if catagories passed in, only print them
          else{
            for(int collumn : catIndex){
              tempRow.add(row[collumn]);
            }
          }

          DATA.add(tempRow);
        }

      System.out.println("File "+(fileIndex+1) +"/"+ DATE.length + " parsed");

      firstIteration = false;
    } // <-- bracket for month for loop

  }

  public void importLookupTables() throws IllegalArgumentException{
    for(String catagoryName : DATA_CATAGORY){
      if( (new File(DIRECTORY+"\\lookupTables\\L_"+catagoryName+".csv_")).exists() ){
        File input = null;
        Scanner kb = null;
        try{
          input = new File(DIRECTORY+"\\lookupTables\\L_"+catagoryName+".csv_");
          kb = new Scanner(input);
        }
        catch(FileNotFoundException e){
          System.out.println("The catagory "+catagoryName+" did not have a lookup table. Perhaps it does not use a lookup table");
          e.printStackTrace();
        }

        HashMap<String,String> tempMap = new HashMap<String,String>();

        //skip headers
        kb.nextLine();

        int c=0; //debugging/exception counter
        while(kb.hasNext()){
          String next = kb.nextLine();
          String[] args = next.split(",");
            if(args.length<2) throw new IllegalArgumentException("Cannot make a HashMap from less than two values. Error details...\nLine: " + c + "\nCategory: " + catagoryName +"\nCsv text: "+ next +"\nArray: "+ Arrays.toString(args));
          String key = args[0];
          String value = "";
          for(int x=1;x<args.length;x++)
            value+=args[x];
          tempMap.put(key,value);

          c++; //debugging counter
        }

        LOOKUP_TABLE.put(catagoryName, tempMap);
      }
    }
  }

  public void useLookupValues(){
    for(String catagoryName : DATA_CATAGORY){
      if(LOOKUP_TABLE.containsKey(catagoryName)){
        int cat = DATA_CATAGORY.indexOf(catagoryName);
        for(int x=0;x<DATA.size();x++){
          if( LOOKUP_TABLE.get(catagoryName).containsKey( "\""+DATA.get(x).get(cat)+"\"" )) {
            DATA.get(x).set(cat, LOOKUP_TABLE.get(catagoryName).get("\""+DATA.get(x).get(cat)+"\""));
          }
        }
      }
    }
  }

  public void stopLookupValues(){
    //soon to be method
    //this will reverse values imported from lokup table hashmaps, returning them to their raw data types
  }

  public void exportToCSV(){

    long time = System.currentTimeMillis();
    String csvName = ""+time+".csv";

    File output = null;
    FileWriter fw = null;
    PrintWriter pw = null;
    try{
      output = new File(csvName);
      fw = new FileWriter(output);
      pw = new PrintWriter(fw);
    }
    catch(IOException e){
      e.printStackTrace();
    }

    for(String header : DATA_CATAGORY)
      pw.print(header+",");
    pw.print("\n");

    for(ArrayList<String> row : DATA){
      for(String item : row)
        pw.print(item+",");
      pw.print("\n");
    }

    try{
      pw.close();
      fw.close();
    }
    catch(IOException e){
      e.printStackTrace();
    }

  }

  private int getCatatoryIndex(String inputCatagory){
    for(int x=0;x<DATA_CATAGORY.size();x++)
      if(inputCatagory.equalsIgnoreCase(DATA_CATAGORY.get(x)))
        return x;
    //if nothing was found
    return -1;
  }

  public void sort(String inputCatagory){
    //INNER CLASS: quick sort for 2D arraylist (DATA)
    final int catIndex = getCatatoryIndex(inputCatagory); //must be final for inner class
    class ArrayList2DQuickSort{
      public int partition(ArrayList<ArrayList<String>> arr, int low, int high){
        int pivot = Integer.parseInt(arr.get(high).get(catIndex));
        int i = low-1; // index of smaller element
        for(int j=low; j<high; j++){
          if(Integer.parseInt(arr.get(j).get(catIndex)) <= pivot){
              i++;
              ArrayList<String> temp = arr.get(i);
              arr.set(i, arr.get(j));
              arr.set(j, temp);
          }
        }
        ArrayList<String> temp = arr.get(i+1);
        arr.set((i+1), arr.get(high));
        arr.set(high, temp);

        return i+1;
      }

      public void sort(ArrayList<ArrayList<String>> arr, int low, int high){
        if(low < high){
            int pi = partition(arr, low, high);

            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
      }
    }

    ArrayList2DQuickSort qs = new ArrayList2DQuickSort();
    qs.sort(DATA, 0, DATA.size()-1);
  }

  public LinkedHashMap<String,Integer> frequency(boolean sort, String... inputCatagory){

    //counts each occurance
    int[] catIndex = new int[inputCatagory.length];
    for(int x=0;x<inputCatagory.length;x++){
      catIndex[x] = getCatatoryIndex(inputCatagory[x]);
    }

    LinkedHashMap<String,Integer> freq = new LinkedHashMap<String,Integer>();
    for(int i : catIndex)
      for(int x=0;x<DATA.size();x++){
        Integer j = freq.get(DATA.get(x).get(i));
        freq.put(DATA.get(x).get(i), (j==null) ? 1 : j+1);
      }

    if(sort){
      //Sorts freqencys based on the following list
      LinkedList<Map.Entry<String,Integer>> list = new LinkedList<Map.Entry<String,Integer>>(freq.entrySet());

      //sorts list
      Collections.sort(list, new Comparator<Map.Entry<String,Integer>>(){
        public int compare(Map.Entry<String,Integer> entry1, Map.Entry<String,Integer> entry2){
            return (entry1.getValue()).compareTo(entry2.getValue());
        }
      });

      //put data from sorted list to hashmap
      LinkedHashMap<String,Integer> sortedFreq = new LinkedHashMap<String,Integer>();
      for(Map.Entry<String,Integer> entry : list)
          sortedFreq.put(entry.getKey(), entry.getValue());

      //prints values
      for(Map.Entry<String,Integer> entry : sortedFreq.entrySet()){
        double percentage = 100*(double)entry.getValue()/DATA.size();
        Object[] formatInput = {entry.getKey(), entry.getValue(), new String(""+Math.round(percentage*100.0)/100.0+"%")};
        System.out.format("%55s%15s%15s\n",formatInput);
      }
      return sortedFreq;
    }
    if(!sort){
      //prints values
      for(Map.Entry<String,Integer> entry : freq.entrySet()){
        Object[] formatInput = {entry.getKey(), entry.getValue(), (100*(double)entry.getValue()/DATA.size())};
        System.out.format("%55s%15s%15s\n",formatInput);
      }
    }

    return freq;

  }

  public ArrayList<ArrayList<String>> getData(){
    return DATA;
  }

  public ArrayList<String> getDataCatagories(){
    return DATA_CATAGORY;
  }

  public HashMap<String,HashMap<String,String>> getLookupTables(){
    return LOOKUP_TABLE;
  }

  public void clear(){
    //importing related instance variables
    CATAGORY = null;
    FILTER = null;
    DATE = null;
    noFilters = true;

    //post import, analysis instance variables
    DATA = null;
    DATA_CATAGORY = null;
    LOOKUP_TABLE = null;
  }

  public void printConditions(){
    System.out.println("\nCATAGORIES:   "+Arrays.toString(CATAGORY));
      FILTER.entrySet().forEach(entry -> {
        System.out.println("FILTER:      (Key: ["+entry.getKey() + "], Value: " + Arrays.toString(entry.getValue().toArray())+")");
      });
    System.out.println("DATES:        "+Arrays.toString(DATE)+"\n");
    System.out.print("\n");
  }

  public void printData(){
    System.out.println("CATAGORIES: "+Arrays.toString(DATA_CATAGORY.toArray()));
    for(int x=0;x<DATA.size();x++)
      System.out.println("ROW "+x+": "+ Arrays.toString(DATA.get(x).toArray()));
    System.out.println("\nDATA SIZE: "+ DATA.size());
  }
}
