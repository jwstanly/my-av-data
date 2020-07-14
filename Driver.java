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
          MyAVData temp = new MyAVData();
          temp.parseArgument(input[1]);
          temp.printConditions();

          temp.importData();
          temp.printData();

          temp.importLookupTables();
          temp.useLookupValues();
          temp.printData();
        }

        case "csv" -> {
          MyAVData temp = new MyAVData();
          temp.parseArgument(input[1]);
          temp.importData();

          temp.exportToCSV();
        }

        default -> System.out.println("Unkown argument. Try again.");
      }
    }



  }

}
