package ninja.theopensource.compiler;

import java.io.*; 
/* 
   This is an example of reading a TEXT file one character at a time.   
   The read() method returns an integer that must be cast to a 
   character.  When the end of the file is reached, the read()  
   method returns a -1. 
*/ 
class StreamReader  { 
   public static void main( String [] args ) throws IOException { 
      File testFile = new File ( "test.data" ); 
      FileReader reader = new FileReader( testFile ); 
      int data = reader.read(); 
      while( data != -1 ) { 
         char c = (char) data; 
   System.out.print( c ); 
   data = reader.read(); 
  } 
  reader.close(); 
 } 
} 
