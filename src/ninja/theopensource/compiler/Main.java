package ninja.theopensource.compiler;

import java.io.File;

public class Main implements Constants {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println( "File: " );
		java.util.Scanner javaScanner = new java.util.Scanner( System.in );
		String fileName = javaScanner.next();
		javaScanner.close();
		
		File file = new File( fileName );
		if( !file.exists() || !file.canRead() ) {
			System.err.println( "Error: File unreadable" );
			return;
		}
		
		Scanner scan = new Scanner( file );
		Token currentToken = new Token( EOF );
		System.out.println( "Tokens: " );
		do {
			currentToken = scan.getTokenAndPrint();
		} while( currentToken.type != EOF );
	}

}
