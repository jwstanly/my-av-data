import java.util.*;
import java.io.*;
import java.awt.*;

public class Driver{

  private static final boolean debugging = false;

  public static void main(String args[]){

    System.out.println("\nWelcome to MY-AV-DATA! Please enter an argument and parameters split by \"/\" delimiter. "+
                      "A full list of arguments and their parameters can be found on the GitHub repo's readme.");

    Scanner kb = new Scanner(System.in);
    while(true){ //tests for exit inside the while loop scope because of argument scope constraints
      System.out.print("\nARGUMENT/PARAM(S): ");
      String[] input = kb.next().split("/");

      //prints argument and parameters
      if(debugging)
        for(int x=0;x<input.length;x++)
          System.out.println("LOG: input["+x+"]: "+input[x]);

      //checks for quit condition
      if(input[0].equalsIgnoreCase("quit") || input[0].equalsIgnoreCase("q"))
        break;

      switch(input[0]){

        case "print" -> {
          DataManager temp = new DataManager();
          temp.parseArgument(input[1]);

          temp.printConditions();
          temp.printData();

          temp.importLookupTables();
          temp.useLookupValues();
          temp.printData();
        }

        case "csv" -> {
          DataManager temp = new DataManager();
          temp.parseArgument(input[1]);

          temp.exportToCSV();
        }

        case "sort" -> {
          DataManager temp = new DataManager();
          temp.parseArgument(input[2]);

          temp.sort(input[1]);

          temp.importLookupTables();
          temp.useLookupValues();
          temp.printData();

        }

        case "freq" -> {
          DataManager temp = new DataManager();
          temp.parseArgument(input[3]);

          temp.importLookupTables();
          temp.useLookupValues();

          temp.frequency(Boolean.parseBoolean(input[2]), input[1]);
        }

        case "serialize" -> {
          DataManager temp = new DataManager();
          temp.parseArgument(input[1]);

          temp.importLookupTables();
          temp.useLookupValues();

          try{
            FileOutputStream fileOut = new FileOutputStream("DataManagerObjectSerialBytes.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(temp);
            System.out.println("Sucsessfully serialized");
            out.close();
            fileOut.close();
          }
          catch(IOException e){
            System.out.println("Failure to serlialize DataManager object");
            e.printStackTrace();
          }
        }

        case "deserialize" -> {
          DataManager temp = null;

          try{
            FileInputStream fileIn = new FileInputStream("DataManagerObjectSerialBytes.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            temp = (DataManager)in.readObject();
            System.out.println("Sucsessfully deserialized");
            in.close();
            fileIn.close();
          }
          catch(IOException e){
            System.out.println("Failure to deserlialize DataManager object");
            e.printStackTrace();
          }
          catch(ClassNotFoundException e){
            System.out.println("DataManager class not found");
            e.printStackTrace();
          }

          temp.printConditions();
          temp.printData();
        }

        default -> System.out.println("Unkown argument. Try again.");
      }
    }



  }

}
