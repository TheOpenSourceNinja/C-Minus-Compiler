package ninja.theopensource.cminuscompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Scanner implements Constants {
	private File file;
	private java.util.Scanner fileScanner;
	private int currentLine = 0;
	private int positionInLine = 0;
	private ArrayList< ArrayList< Token > > tokens;
	
	private Token stringToToken( String tokenString ) {
		Token result = new Token( ERROR, "" );
		
		if( tokenString.matches( "[0-9]+[.]?[0-9]*$" ) ) { //if it's a number
			result = new Token( NUMBER, tokenString );
		} else {
			switch( tokenString ) {
				case "": {
					break;
				}
				case "else": {
					result = new Token( ELSE, tokenString );
					break;
				}
				case "if": {
					result = new Token( IF, tokenString );
					break;
				}
				case "int": {
					result = new Token( INT, tokenString );
					break;
				}
				case "return": {
					result = new Token( RETURN, tokenString );
					break;
				}
				case "void": {
					result = new Token( VOID, tokenString );
					break;
				}
				case "while": {
					result = new Token( WHILE, tokenString );
					break;
				}
				case "+": {
					result = new Token( PLUS, tokenString );
					break;
				}
				case "-": {
					result = new Token( MINUS, tokenString );
					break;
				}
				case "*": {
					result = new Token( MULT, tokenString );
					break;
				}
				case "/": {
					result = new Token( DIV, tokenString );
					break;
				}
				case "<": { //TODO: This is LS?
					result = new Token( LS, tokenString );
					break;
				}
				case "<=": {
					result = new Token( LEQ, tokenString );
					break;
				}
				case ">": {
					result = new Token( GT, tokenString );
					break;
				}
				case "==": {
					result = new Token( EQ, tokenString );
					break;
				}
				case "!=": {
					result = new Token( NEQ, tokenString );
					break;
				}
				case "=": {
					result = new Token( ASSIGN, tokenString );
					break;
				}
				case ";": {
					result = new Token( SEMI, tokenString );
					break;
				}
				case ",": {
					result = new Token( COMMA, tokenString );
					break;
				}
				case "(": {
					result = new Token( LPAREN, tokenString );
					break;
				}
				case ")": {
					result = new Token( RPAREN, tokenString );
					break;
				}
				case "[": { //TODO: Are these brackets or braces?
					result = new Token( LBRACKET, tokenString );
					break;
				}
				case "]": {
					result = new Token( RBRACKET, tokenString );
					break;
				}
				case "{": {
					result = new Token( LBRACE, tokenString );
					break;
				}
				case "}": {
					result = new Token( RBRACE, tokenString );
					break;
				}
				case "/*": {
					result = new Token( START_COMMENT, tokenString );
					break;
				}
				case "*/": {
					result = new Token( STOP_COMMENT, tokenString );
					break;
				}
				case "read": {
					result = new Token( READ, tokenString );
					break;
				}
				case "write": {
					result = new Token( WRITE, tokenString );
					break;
				}
				default: {
					System.out.println( "Default case reached" );
					result = new Token( ID, tokenString );
					break;
				}
			}
		}
		
		return result;
	}
	
	public Scanner( File newFile ) {
		file = newFile;
		
		if( !file.exists() || !file.canRead() ) {
			System.err.println( "Error: File unreadable" );
			return;
		}
		
		try {
			fileScanner = new java.util.Scanner( file );
		} catch( FileNotFoundException e ) {
			return;
		}
		

		ArrayList<String> fileLines = new ArrayList<String>();
		while ( fileScanner.hasNext() ) {
			fileLines.add( fileScanner.nextLine() );
		};
		
		tokens = new ArrayList< ArrayList< Token > >();
		Iterator<String> it = fileLines.iterator();
		
		boolean ignoreComment = false;
		
		while( it.hasNext() ) {
			String line = it.next();
			ArrayList< Token > newList = new ArrayList< Token >();
			StringReader lineReader = new StringReader( line );
			
			String tokenString = "";
			try {
				int data = lineReader.read();
				while( data != -1 ) {
					char c = (char) data;
					tokenString = tokenString + c;
					
					if( !ignoreComment ) {
						switch( c ) {
							case ' ':
							case '\t': {
								System.out.println( c + " is a space" );
								tokenString = tokenString.trim();
								
								if( !tokenString.isEmpty() ) {
									System.out.println( "tokenString: " + tokenString );
									newList.add( stringToToken( tokenString ) );
									tokenString = "";
								}
								break;
							}
							case ';': {
								tokenString = tokenString.substring( 0, tokenString.length() - 1 );
								System.out.println( c + " is a semicolon" );
								newList.add( stringToToken( tokenString ) );
								tokenString = "" + c;
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case '[': {
								System.out.println( c + " is a left square bracket" );
								if( !tokenString.equals( "[" ) ) {
									newList.add( stringToToken( tokenString.substring( 0, tokenString.length() - 1 ) ) );
									tokenString = "[";
								}
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case '(': {
								System.out.println( c + " is a left parenthesis" );
								if( !tokenString.equals( "(" ) ) {
									newList.add( stringToToken( tokenString.substring( 0, tokenString.length() - 1 ) ) );
									tokenString = "(";
								}
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case ')': {
								System.out.println( c + " is a right parenthesis" );
								if( !tokenString.equals( ")" ) ) {
									newList.add( stringToToken( tokenString.substring( 0, tokenString.length() - 1 ) ) );
									tokenString = ")";
								}
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case ',': {
								System.out.println( c + " is a comma" );
								if( !tokenString.equals( "," ) ) {
									newList.add( stringToToken( tokenString.substring( 0, tokenString.length() - 1 ) ) );
									tokenString = ",";
								}
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case '=': {
								System.out.println( c + " is an equals sign" );
								if( !tokenString.equals( "=" ) ) {
									newList.add( stringToToken( tokenString.substring( 0, tokenString.length() - 1 ) ) );
									tokenString = "=";
								}
								newList.add( stringToToken( tokenString ) );
								tokenString = "";
								break;
							}
							case '*': {
								if( tokenString.equals( "/*" ) ) {
									ignoreComment = true;
								}
							}
						}
					} else if( tokenString.endsWith("*/") ) {
						ignoreComment = false;
						tokenString = "";
					}
					
					data = lineReader.read();
				}
				
				if( !tokenString.isEmpty() ) {
					System.out.println( "tokenString: " + tokenString );
					newList.add( stringToToken( tokenString ) );
					tokenString = "";
				}
				
			} catch( IOException e ) {
				return;
			}
			
			tokens.add( newList );
		}
		
		return;
	}
	
	public Token getToken() {
		Token result = null;
		try {
			ArrayList<Token> tokenLine = tokens.get( currentLine );
			
			while( positionInLine >= tokenLine.size() ) {
				positionInLine = 0;
				currentLine++;
				tokenLine = tokens.get( currentLine );
			}
			result = tokenLine.get( positionInLine );
			positionInLine++;
			if( positionInLine >= tokenLine.size() ) {
				positionInLine = 0;
				currentLine++;
			}
		} catch( IndexOutOfBoundsException e ) {
			result = new Token( EOF, "" );
		}
		return result;
	}
	
	public Token getTokenAndPrint() {
		Token t = getToken();
		
		if( t.type != EOF ) {
			System.out.print( currentLine );
			System.out.println( " " + t );
		}
		
		return t;
	}
}
