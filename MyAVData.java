import java.util.*;

public class MyAVData{

  private static final String[] OP_CODE = {"VALUE","RANGE","SERIES"}; //importance discussed in importData()
  private String[] CATAGORY;
  private HashMap<String,ArrayList<String>> FILTER;

  private boolean debugging = true;

  public MyAVData(String argument){
    importData(argument);
  }

  //-------EXAMPLE INPUT FORMAT--------
  //catagorys:filters
  //cat1,cat2,catN:cat1.val1,cat2.(range1,range2),cat3.[val1,val2,val3]
  //cat1 , cat2 , catN : cat1.val1 , cat2.(startRange-endRange) , cat3.[val1-val2-val3-valN]
  public void importData(String argument) throws IllegalArgumentException{
    //checks for special initial input formats and exceptions
      boolean onlyCatagories = false;
      boolean onlyFilters = false;
      if(argument.substring(argument.length()-1,argument.length()).equals(":")) onlyCatagories = true;
      if(argument.substring(0,1).equals(":")) onlyFilters = true;
      if(argument.equals(":")) throw new IllegalArgumentException("Must input either a category or filter");

    //splits catagories from filters (if necessary)
      String categoryArg="";
      String filterPairs="";

      if(!onlyCatagories && !onlyFilters){
        String[] mainArg = argument.split(":");
          if(mainArg.length>2) throw new IllegalArgumentException("Incorrect entry. Only one \":\" delimiter per condition statement. Erroneous \":\" count: "+(mainArg.length-1));
        categoryArg = mainArg[0];
        filterPairs = mainArg[1];
      }
      if(onlyCatagories){
        categoryArg = argument.substring(0,argument.length()-1);
        filterPairs = "";
      }
      if(onlyFilters){
        categoryArg = "";
        filterPairs = argument.substring(1,argument.length());
      }

    //Assigns catagories... very easy compared to filters
      if(!onlyFilters){
        if(categoryArg.equalsIgnoreCase("null")){
          CATAGORY = null;
        }
        if(categoryArg.contains(",")){
          CATAGORY = categoryArg.split(",");
        }
        else{
          CATAGORY = new String[1];
          CATAGORY[0] = categoryArg;
        }
      }

    //Assigns filters... if only filters were as simple as catagories :(
      FILTER = new HashMap<String,ArrayList<String>>();

      //IF THEY'RE FILTERS, goes through every filter pair
      if(!onlyCatagories)
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
      }
    }

  public void printConditions(){
    System.out.println("\nCATAGORIES:   "+Arrays.toString(CATAGORY)+"\n");
      FILTER.entrySet().forEach(entry->{
        System.out.println("FILTERS:      (Key: ["+entry.getKey() + "], Value: " + Arrays.toString(entry.getValue().toArray())+")");
      });
    System.out.print("\n");
  }
}

//String[][][] filter = new String[/*order*/][/*key/value(type)*/][/*arg*/];

  //Ok, the filter array is nasty. I'm sorry there's probably a better way to implement this,
  //but bare with me along with my very novice data skills. The array's 3 pointers represent...
  //ORDER - the sequenced number of the input
  //KEY/VALUE(TYPE) - specificies which kind of arg it is, and whether the arg is a...
    //0 - key
    //1 - standard value
    //2 - start range for values
    //3 - end range for values
    //4 - value in list of values
  //ARG - the value itself

  //Here is an example flow down...
    //filter[] = { cat1.val1 , cat2.(startRange,endRange) , cat3.[val1,val2,val3,valN] }

      //filter[x] = cat1.val1
        //filter[x][] = {cat1 , val1}
          //filter[x][y] = cat1

      //filter[x] = cat2.(startRange,endRange)
        //filter
      //filter[x] = cat3.[val1,val2,val3,valN]
