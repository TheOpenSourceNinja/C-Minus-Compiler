package ninja.theopensource.cminuscompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main implements Constants {
	
	private Scanner scan;
	
	public Main() {
		System.out.println( "File: " );
		java.util.Scanner javaScanner = new java.util.Scanner( System.in );
		String fileName = javaScanner.next();
		javaScanner.close();
		
		File file = new File( fileName );
		if( !file.exists() || !file.canRead() ) {
			System.err.println( "Error: File unreadable" );
			return;
		}
		
		scan = new Scanner( file );
		
		TreeNode root = new TreeNode();
		root.nodeType = PROGRAM;
		
		Token currentToken = new Token( EOF );
		ArrayList<Token> allTheTokens = new ArrayList<Token>();
		do {
			currentToken = scan.getToken();
			allTheTokens.add( currentToken );
		} while( currentToken.type != EOF );
		buildSubTree( root, allTheTokens );
		System.out.println( "Tree built" );
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main m = new Main(); //Escape the static! Static is a pain!
	}
	
	public void findSiblings( TreeNode root, List<Token> tokens ) {
		System.out.println( "In function findSiblings" );
		while( !tokens.isEmpty() ) {
			//try to identify the type of tree node represented by these tokens
			TreeNode node = new TreeNode();
			Token start = tokens.get( 0 );
			tokens = tokens.subList( 1, tokens.size() );
			switch( start.type ) {
				case EOF: {
					tokens.clear();
					break;
				}
				case COMMA:
				case RBRACKET:
				case RBRACE:
				case SEMI: {
					break;
				}
				case VOID:
				case INT: {
					node.typeSpecifier = start.type;
					Token second = tokens.get( 0 );
					tokens = tokens.subList( 1, tokens.size() );
					switch( second.type ) {
						case ID: {
							node.sValue = second.value;
							if( !tokens.isEmpty() ) {
								Token third = tokens.get( 0 );
								tokens = tokens.subList( 1, tokens.size() );
								switch( third.type ) {
									case LBRACKET: { //We're declaring an array
										node.nodeType = ARRAY;
										Token fourth = tokens.get( 0 );
										tokens = tokens.subList( 1, tokens.size() );
										switch( fourth.type ) {
											case NUMBER: {
												node.nValue = Integer.parseInt( fourth.value );
												break;
											}
											case RBRACKET: {
												break;
											}
											default: {
												System.err.println( "Unhandled fourth.type: " + fourth.type );
												System.exit(-1);
											}
										}
										
										root.sibling = node;
										root = node; //Don't want to accidentally replace sibling
										break;
									}
									case LPAREN: { //We're declaring a function
										node.nodeType = FUNCTION;
										
										node.C1 = new TreeNode();
										node.C1.nodeType = PARAMETER_LIST;
										int endOfParameterList = tokens.indexOf( new Token( RPAREN, ")" ) );
										buildSubTree( node.C1, tokens.subList(0, endOfParameterList ) );
										tokens = tokens.subList( endOfParameterList + 1, tokens.size() );
										
										node.C2 = new TreeNode();
										node.C2.nodeType = COMPOUND;
										int startOfCompound = tokens.indexOf( new Token( LBRACE, "{") );
										int endOfCompound;// = tokens.lastIndexOf( new Token( RBRACE, "}" ) );
										
										//Find endOfCompound: Find the *matching* }
										{
											int numberOfStarters = 1; //Number of {: when it matches the number of }, we're done
											int numberOfEnders = 0;
											int index = startOfCompound;
											do {
												index += 1;
												Token t = tokens.get( index );
												switch( t.type ) {
													case LBRACE: {
														numberOfStarters += 1;
														break;
													}
													case RBRACE: {
														numberOfEnders += 1;
														break;
													}
													default: {
														break;
													}
												}
											} while( numberOfStarters != numberOfEnders );
											endOfCompound = index;
											startOfCompound += 1; //We don't need to include the {
										}
										
										buildSubTree( node.C2, tokens.subList(startOfCompound, endOfCompound ) );
										tokens = tokens.subList( endOfCompound + 1, tokens.size() );
										
										root.sibling = node;
										root = node; //Don't want to accidentally replace sibling
										break;
									}
									case RPAREN:
									case COMMA:
									case SEMI: { //We're declaring a variable
										
										root.sibling = node;
										root = node; //Don't want to accidentally replace sibling
										break;
									}
									default: {
										System.err.println( "Unhandled third.type: " + third.type );
										System.exit(-1);
									}
								}
							} else {
								root.sibling = node;
								root = node; //Don't want to accidentally replace sibling
							}
							break;
						}
						default: {
							System.err.println( "Unhandled second.type: " + second.type );
							System.exit(-1);
						}
					}
					break;
				}
				case ID: {
					break;
				}
				default: {
					System.err.println( "Unhandled start.type: " + start.type );
					System.exit(-1);
				}
			}
		}
	}
	
	public void findArguments( TreeNode root, List<Token> tokens ) {
		System.out.println( "In function findArguments" );
		System.out.println( tokens );
		
		while( !tokens.isEmpty() ) {
			TreeNode node = new TreeNode();
			
			Token start = tokens.get( 0 );
			tokens = tokens.subList( 1, tokens.size() );
			
			switch( start.type ) {
				case NUMBER: {
					System.out.println( tokens );
					if( tokens.isEmpty() ) {
						node.nodeType = NUMBER;
						node.nValue = Integer.parseInt( start.value );
					} else {
						Token second = tokens.get( 0 );
						tokens = tokens.subList( 1, tokens.size() );
						
						switch( second.type ) {
							case COMMA: {
								node.nodeType = NUMBER;
								node.nValue = Integer.parseInt( start.value );
								break;
							}
							default: {
								System.err.println( "Unhandled second.type: " + second.type );
								System.exit( -1 );
							}
						}
					}
					break;
				}
				case ID: {
					if( tokens.isEmpty() ) {
						node.nodeType = VARIABLE;
						node.sValue = start.value;
					} else {
						Token second = tokens.get( 0 );
						tokens = tokens.subList( 1, tokens.size() );
						
						switch( second.type ) {
							case COMMA: {
								node.nodeType = VARIABLE;
								node.sValue = start.value;
								break;
							}
							default: {
								System.err.println( "Unhandled second.type: " + second.type );
								System.exit( -1 );
							}
						}
					}
					break;
				}
				default: {
					System.err.println( "Unhandled start.type: " + start.type );
					System.exit( -1 );
				}
			}
			
			root.sibling = node;
			root = node;
		}
	}
	
	public void findExpression( Token start, TreeNode root, List<Token> tokens ) {
		System.out.println( "In function findExpression" );
		System.out.println( tokens );
		while( !tokens.isEmpty() ) {
			
			if( start == null ) {
				start = tokens.get( 0 );
				tokens = tokens.subList( 1, tokens.size() );
			}
			
			TreeNode node = new TreeNode();
			node.nodeType = EXPRESSION;
			
			node.C1 = new TreeNode();
			node.C1.sValue = start.value;
			
			switch( start.type ) {
				case NUMBER: {
					node.C1.nodeType = NUMBER;
					node.C1.nValue = Integer.parseInt( start.value );
					break;
				}
				case ID: {
					node.C1.nodeType = VARIABLE; //Is this a valid assumption?
					if( !tokens.isEmpty() ) {
						Token first = tokens.get( 0 );
						tokens = tokens.subList( 1, tokens.size() );
						switch( first.type ) {
							case LBRACKET: {
								System.out.println( tokens );
								node.C1.nodeType = ARRAY;
								
								Token second = tokens.get( 0 );
								tokens = tokens.subList( 1, tokens.size() );
								
								switch( second.type ) {
									case INT: {
										node.C1.nValue = Integer.parseInt( second.value );
										tokens = tokens.subList( 1, tokens.size() );
										break;
									}
									case ID: {
										int index = tokens.indexOf( new Token( SEMI, ";" ) );
										if( index == -1 ) {
											index = tokens.size();
										}
										
										findExpression( second, node.C1, tokens.subList( 1, index ) );
										tokens = tokens.subList( index, tokens.size() );
										break;
									}
									default: {
										System.err.println( "Unhandled second.type: " + second.type );
										System.exit( -1 );
									}
								}
								
								break;
							}
							case MINUS: {
								System.out.println( tokens );
								node.nodeType = MINUS;
								node.C2 = new TreeNode();
								
								int index = tokens.indexOf( new Token( MINUS, "-" ) );
								if( index == -1 ) {
									index = tokens.size();
								}
								findExpression( null, node.C2, tokens );
								tokens = tokens.subList( index,  tokens.size() );
								
								break;
							}
							case LS: {
								node.nodeType = LS;
								node.C2 = new TreeNode();
								int index = tokens.indexOf( new Token( SEMI, ";" ) );
								if( index == -1 ) {
									index = tokens.size();
								}
								findExpression( null, node.C2, tokens );
								tokens = tokens.subList( index, tokens.size() );
								break;
							}
							case ASSIGN: {
								Token second = tokens.get( 0 );
								tokens = tokens.subList( 1,  tokens.size() );
								
								switch( second.type ) {
									case NUMBER: {
										System.out.println( tokens );
										node.nodeType = ASSIGN;
										node.C2 = new TreeNode();
										node.C2.nodeType = NUMBER;
										node.C2.nValue = Integer.parseInt( second.value );
										break;
									}
									case ID: {
										node.C2 = new TreeNode();
										
										if( tokens.isEmpty() ) {
											node.C2.nodeType = VARIABLE;
											node.C2.sValue = second.value;
										} else {
											Token third = tokens.get( 0 );
											tokens = tokens.subList( 1,  tokens.size() );
											
											switch( third.type ) {
												case LPAREN: { //It's a function call
													System.out.println( tokens );
													node.C2.nodeType = CALL;
													node.C2.sValue = second.value;
													node.C2.typeSpecifier = INT; //TODO: Find the real return type of the function.
													node.C2.C1 = new TreeNode();
													node.C2.C1.nodeType = ARGUMENTS;
													
													int index = tokens.indexOf( new Token( RPAREN, ")" ) );
													if( index == -1 ) {
														index = tokens.size();
													}
													findArguments( node.C2.C1, tokens.subList( 0, index ) );
													tokens = tokens.subList( index + 1, tokens.size() );
													break;
												}
												case SEMI: {
													node.C2.nodeType = VARIABLE;
													node.C2.sValue = second.value;
													break;
												}
												case LBRACKET: {
													node.C2.nodeType = ARRAY;
													node.C2.sValue = second.value;
													node.C2.C1 = new TreeNode();
													
													int bracketIndex = tokens.indexOf( new Token( RBRACKET, "]" ) );
													
													findExpression( null, node.C2.C1, tokens.subList( 0, bracketIndex ) );
													tokens = tokens.subList( bracketIndex + 1, tokens.size() );
													break;
												}
												case PLUS: {
													System.out.println( tokens );
													node.C2.nodeType = PLUS;
													node.C2.C1 = node;
													TreeNode temp = node.C2;
													node.C2 = null;
													node = temp;
													
													node.C2 = new TreeNode();
													
													{
														int tokenIndex = tokens.size(); //tokens.indexOf( new Token( SEMI, ";" ) );
														findExpression( null, node.C2, tokens.subList( 0, tokenIndex ) );
														tokens = tokens.subList( tokenIndex, tokens.size() );
													}
													
													node.C2 = node.C2.sibling;
													
													if( node.C1.typeSpecifier == EXPRESSION && node.C1.C1 != null && node.C1.C2 == null ) {
														node.C1 = node.C1.C1;
													}
													
													if( node.C2.typeSpecifier == EXPRESSION && node.C2.C1 != null && node.C2.C2 == null ) {
														node.C2 = node.C2.C1;
													}
													break;
												}
												default: {
													System.err.println( "Unhandled third.type: " + third.type );
													System.exit( -1 );
												}
											}
										}
										
										break;
									}
									default: {
										System.err.println( "Unhandled second.type: " + second.type );
										System.exit( -1 );
									}
								}
								
								break;
							}
							default: {
								System.err.println( "Unhandled first.type: " + first.type );
								System.exit( -1 );
							}
						}
					}
					break;
				}
				default: {
					System.err.println( "Unhandled start.type: " + start.type );
					System.exit( -1 );
				}
			}
			
			while( !tokens.isEmpty() && tokens.get( 0 ).type == SEMI ) {
				tokens = tokens.subList( 1, tokens.size() );
			}
			
			root.sibling = node;
			root = node;
			start = null;
		}
	}
	
	public void findStatementListSiblings( TreeNode root, List<Token> tokens ) {
		System.out.println( "In function findStatementListSiblings" );
		while( !tokens.isEmpty() ) {
			System.out.println( tokens );
			
			TreeNode node = new TreeNode();
			Token start = tokens.get( 0 );
			tokens = tokens.subList( 1, tokens.size() );
			
			switch( start.type ) {
				case WRITE: {
					System.out.println( tokens );
					node.nodeType = WRITE;
					
					int endParen = tokens.indexOf( new Token( RPAREN, ")" ) );
					node.C1 = new TreeNode();
					node.C1.nodeType = EXPRESSION;
					
					System.out.println( tokens.subList( 1, endParen ) );
					findExpression( null, node.C1, tokens.subList( 1, endParen ) );
					tokens = tokens.subList( endParen + 1, tokens.size() );
					
					break;
				}
				case READ: {
					System.out.println( tokens );
					node.nodeType = READ;
					
					Token second = tokens.get( 0 );
					tokens = tokens.subList( 1, tokens.size() );
					
					switch( second.type ) {
						case ID: {
							
							Token third = tokens.get( 0 );
							tokens = tokens.subList( 0, tokens.size() );
							
							node.C1 = new TreeNode();
							
							switch( third.type ) {
								case ID: {
									node.C1.nodeType = VARIABLE;
									node.C1.nValue = Integer.parseInt( second.value );
									break;
								}
								case LBRACKET: {
									node.C1.nodeType = ARRAY;
									node.C1.sValue = second.value;
									
									tokens = tokens.subList( tokens.indexOf( new Token( RBRACKET, "]" ) ) + 1, tokens.size() );
									break;
								}
								default: {
									System.err.println( "Unhandled third.type: " + third.type );
									System.exit( -1 );
								}
							}
							
							break;
						}
						default: {
							System.err.println( "Unhandled second.type: " + second.type );
							System.exit( -1 );
						}
					}
					
					break;
				}
				case INT: { //We're starting a compound node
					//tokens.add( 0, start );
					
					List<Token> newTokenList = new ArrayList<Token>();
					newTokenList.add( 0, start );
					for( int i = 0; i < tokens.size(); i++ ) {
						newTokenList.add( tokens.get( i ) );
					}
					
					System.out.println( tokens );
					System.out.println( newTokenList );
					
					node.nodeType = COMPOUND;
					System.out.println( newTokenList );
					{
						int endOfBlock = newTokenList.indexOf( new Token( RBRACE, "}" ) );
						if( endOfBlock == -1 ) {
							endOfBlock = newTokenList.size();
						}
						buildSubTree( node, newTokenList.subList( 0, endOfBlock ) );
						
						newTokenList = newTokenList.subList( endOfBlock, newTokenList.size() );
					}
					tokens = newTokenList;
					break;
				}
				case RETURN: {
					System.out.println( tokens );
					node.C1 = new TreeNode();
					
					int tokenIndex = tokens.indexOf( new Token( SEMI, ";" ) );
					findExpression( null, node.C1, tokens.subList( 0, tokenIndex ) );
					tokens = tokens.subList( tokenIndex + 1, tokens.size() );
					
					break;
				}
				case IF: {
					System.out.println( tokens );
					node.C1 = new TreeNode();
					
					{
						int tokenIndex = tokens.indexOf( new Token( RPAREN, ")" ) );
						findExpression( null, node.C1, tokens.subList( 1, tokenIndex ) );
						tokens = tokens.subList( tokenIndex + 1,  tokens.size() );
					}
					
					node.C2 = new TreeNode();
					node.C2.nodeType = COMPOUND;
					System.out.println( tokens );
					{
						int endOfBlock = tokens.indexOf( new Token( RBRACE, "}" ) );
						if( endOfBlock == -1 ) {
							endOfBlock = tokens.size();
						}
						buildSubTree( node.C2, tokens.subList( 1, endOfBlock ) );
					}
					
					node.C3 = new TreeNode();
					node.C3.nodeType = COMPOUND;
					System.out.println( tokens );
					{
						int endOfBlock = tokens.indexOf( new Token( RBRACE, "}" ) );
						buildSubTree( node.C3, tokens.subList( 1, endOfBlock ) );
						tokens = tokens.subList( endOfBlock + 1, tokens.size() );
					}
					break;
				}
				case ID: { //We found an expression
					Token second = tokens.get( 0 );
					tokens = tokens.subList( 0, tokens.size() );
					
					switch( second.type ) {
						case LBRACKET:
						case ASSIGN: {
							int tokenIndex = tokens.indexOf( new Token( SEMI, ";" ) );
							findExpression( start, node, tokens.subList( 0, tokenIndex ) );
							tokens = tokens.subList( tokenIndex + 1, tokens.size() );
							break;
						}
						case LPAREN: { //It's a function call
							tokens = tokens.subList( 1, tokens.size() );
							
							System.out.println( tokens );
							node.nodeType = CALL;
							node.sValue = start.value;
							node.typeSpecifier = INT; //TODO: Find the real return type of the function.
							node.C1 = new TreeNode();
							node.C1.nodeType = ARGUMENTS;
							
							int index = tokens.indexOf( new Token( RPAREN, ")" ) );
							if( index == -1 ) {
								index = tokens.size();
							}
							findArguments( node.C1, tokens.subList( 0, index ) );
							tokens = tokens.subList( index + 1, tokens.size() );
							
							break;
						}
						default: {
							System.err.println( "Unhandled second.type: " + second.type );
							System.exit( -1 );
						}
					}
					
					break;
				}
				case CALL: {
					System.out.println( tokens );
					break;
				}
				case WHILE: {
					System.out.println( tokens );
					node.C1 = new TreeNode();
					
					{
						int tokenIndex = tokens.indexOf( new Token( RPAREN, ")" ) );
						findExpression( null, node.C1, tokens.subList( 1, tokenIndex ) );
						tokens = tokens.subList( tokenIndex + 1,  tokens.size() );
					}
					
					node.C2 = new TreeNode();
					node.C2.nodeType = STATEMENT_LIST;
					System.out.println( tokens );
					{
						int startOfCompound = tokens.indexOf( new Token( LBRACE, "{") );
						int endOfCompound;// = tokens.lastIndexOf( new Token( RBRACE, "}" ) );
						
						//Find endOfCompound: Find the *matching* }
						{
							int numberOfStarters = 1; //Number of {: when it matches the number of }, we're done
							int numberOfEnders = 0;
							int index = startOfCompound;
							do {
								index += 1;
								Token t = tokens.get( index );
								switch( t.type ) {
									case LBRACE: {
										numberOfStarters += 1;
										break;
									}
									case RBRACE: {
										numberOfEnders += 1;
										break;
									}
									default: {
										break;
									}
								}
							} while( numberOfStarters != numberOfEnders );
							endOfCompound = index;
							startOfCompound += 1; //We don't need to include the {
						}
						
						buildSubTree( node.C2, tokens.subList( startOfCompound, endOfCompound ) );
						System.out.println( tokens.subList( startOfCompound, endOfCompound ) );
						System.out.println( tokens );
						System.out.println( tokens.subList( endOfCompound + 1, tokens.size() ) );
						tokens = tokens.subList( endOfCompound + 1, tokens.size() );
					}
					break;
				}
				default: {
					System.err.println( "Unhandled start.type: " + start.type );
					System.exit( -1 );
				}
			}
			
			while( !tokens.isEmpty() && tokens.get( 0 ).type == SEMI ) {
				tokens = tokens.subList( 1, tokens.size() );
			}
			
			root.sibling = node;
			root = node;
		}
	}
	
	public void buildSubTree( TreeNode root, List<Token> tokens ) {
		System.out.println( "In function buildSubTree" );
		switch( root.nodeType ) {
			case PROGRAM: {
				findSiblings( root, tokens );
				break;
			}
			case PARAMETER_LIST: {
				System.out.println( tokens );
				
				TreeNode node = new TreeNode();
				
				Token start = tokens.get( 0 );
				switch( start.type ) {
					case VOID: {
						node.typeSpecifier = VOID;
						break;
					}
					default: {
						findSiblings( node, tokens );
						break;
					}
				}
				break;
			}
			case COMPOUND: {
				root.C1 = new TreeNode();
				root.C1.nodeType = DECLARATION;
				
				//Find declarations
				{
					boolean keepGoing = true;
					int index = 0;
					do {
						Token t = tokens.get( index );
						switch( t.type ) {
							case INT: {
								index += 3; //I'm assuming here that these declarations can only take the form "int i;", not (e.g.) "int i = 0;"
								break;
							}
							default: {
								keepGoing = false;
								break;
							}
						}
					} while( keepGoing );
					
					if( index > 0 ) { //Zero means we found nothing
						findSiblings( root.C1, tokens.subList( 0, index ) );
						tokens = tokens.subList( index, tokens.size() );
					}
				}
				
				root.C2 = new TreeNode();
				root.C2.nodeType = STATEMENT_LIST;
				
				buildSubTree( root.C2, tokens );
				
				break;
			}
			case STATEMENT_LIST: {
				findStatementListSiblings( root, tokens );
				//tokens.clear();
				break;
			}
			default: {
				System.err.println( "Unhandled root.nodeType: " + root.nodeType );
				System.exit(-1);
			}
		}
	}
}
