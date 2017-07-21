   import java.io.*;
  
   public class unowntext {
   
      static String getLine() {
      	String derp = "";
         try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            derp = in.readLine();
         }
            catch (Exception e) {
               e.printStackTrace();
            }
         return derp;
      }
   
      static void printf(String derp) {
         System.out.print(derp);
      }
   
      static String convert(String derp) {
         String merp = "";
         for (int i = 0; i < derp.length(); i++) {
            if(derp.charAt(i) == 'k')
               merp += ":iconunown-" + derp.charAt(i) + "plz:";
            else if(derp.charAt(i) == ' ')
            	merp += ":iconemptyspaceplz:";
            else
               merp += ":iconunown" + derp.charAt(i) + "plz:";
         }
         return merp;
      }
   
      public static void main(String args[]) {
         printf("Enter the text to be converted: ");
         printf("\nResult: " + convert(getLine()));
      };
   }