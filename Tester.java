import java.util.*;
import java.io.*;
import java.awt.*;

public class Tester{
  public static void main(String args[]){

    //-------EXAMPLE INPUT FORMAT--------
    //catagorys:filters
    //cat1,cat2,catN:cat1.val1,cat2.(range1,range2),cat3.[val1,val2,val3]
    //cat1 , cat2 , catN : cat1.val1 , cat2.(startRange-endRange) , cat3.[val1-val2-val3-valN]

    Scanner kb = new Scanner(System.in);

    while(true){
      System.out.print("Conditions ~~ ");
      String input = kb.next();

      if(input.equalsIgnoreCase("q")) break;
      MyAVData bruh = new MyAVData(input);

      bruh.printConditions();
    }
  }
}
