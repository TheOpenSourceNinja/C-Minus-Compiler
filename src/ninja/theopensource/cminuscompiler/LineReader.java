package ninja.theopensource.cminuscompiler;
import java.io.*; 
/* 
      This is an example of reading a TEXT file, one line at a time.  
      The readLine() method returns a String.  The returned String is  
      null when the end of the file is reached. 
*/ 
class LineReader  { 
   public static void main( String [] args ) throws IOException { 
      File testFile = new File ( "test.data" ); 
      FileReader inStreamReader = new FileReader( testFile ); 
      BufferedReader reader = new BufferedReader( inStreamReader ); 
      String data = reader.readLine(); 
      while( data != null ) { 
   System.out.println( data ); 
   data = reader.readLine(); 
  } 
  reader.close(); 
 } 
} 
