package ninja.theopensource.cminuscompiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main implements Constants {
	
	private TreeNode realRoot; //Would have called this root, but I'm adding it after writing a bunch of functions that use the name "root" for their arguments.
	
	public static String intToString( int i ) {
		switch( i ) {
			case 0: {
				return "EOF";
			}
			case 1: {
				return "ERROR";
			}
			case 2: {
				return "ELSE";
			}
			case 3: {
				return "IF";
			}
			case 4: {
				return "INT";
			}
			case 5: {
				return "RETURN";
			}
			case 6: {
				return "VOID";
			}
			case 7: {
				return "WHILE";
			}
			case 8: {
				return "PLUS";
			}
			case 9: {
				return "MINUS";
			}
			case 10: {
				return "MULT";
			}
			case 11: {
				return "DIV";
			}
			case 12: {
				return "LS";
			}
			case 13: {
				return "LEQ";
			}
			case 14: {
				return "GT";
			}
			case 15: {
				return "GEQ";
			}
			case 16: {
				return "EQ";
			}
			case 17: {
				return "NEQ";
			}
			case 18: {
				return "ASSIGN";
			}
			case 19: {
				return "SEMI";
			}
			case 20: {
				return "COMMA";
			}
			case 21: {
				return "LPAREN";
			}
			case 22: {
				return "RPAREN";
			}
			case 23: {
				return "LBRACKET";
			}
			case 24: {
				return "RBRACKET";
			}
			case 25: {
				return "LBRACE";
			}
			case 26: {
				return "RBRACE";
			}
			case 27: {
				return "START_COMMENT";
			}
			case 28: {
				return "STOP_COMMENT";
			}
			case 30: {
				return "READ";
			}
			case 31: {
				return "WRITE";
			}
			case 32: {
				return "NUMBER";
			}
			case 33: {
				return "ID";
			}
			case 40: {
				return "PROGRAM";
			}
			case 41: {
				return "DECLARATION";
			}
			case 42: {
				return "VARIABLE";
			}
			case 43: {
				return "ARRAY";
			}
			case 44: {
				return "FUNCTION";
			}
			case 45: {
				return "EXPRESSION";
			}
			case 46: {
				return "CALL";
			}
			case 47: {
				return "COMPOUND";
			}
			case 48: {
				return "TYPE_SPECIFIER";
			}
			case 49: {
				return "PARAMETER_LIST";
			}
			case 50: {
				return "PARAMETER";
			}
			case 51: {
				return "STATEMENT_LIST";
			}
			case 52: {
				return "STATEMENT";
			}
			case 53: {
				return "ARGUMENTS";
			}
			default: {
				System.err.println( "There is no string for " + i );
				System.exit( -1 );
			}
		}
		return "";
	}
	
	private void showTree( TreeNode root, String prefix ) {
		String indent = "-";
		System.out.println( prefix + "Node type: " + intToString( root.nodeType ) );
		System.out.println( prefix + "Type specifier: " + intToString( root.typeSpecifier ) );
		System.out.println( prefix + "sValue: " + root.sValue );
		System.out.println( prefix + "nValue: " + root.nValue );
		System.out.println( prefix + "rename: " + root.rename );
		
		System.out.println();
		if( root.C1 != null ) {
			System.out.println( prefix + "C1:" );
			showTree( root.C1, prefix + indent );
		}
		if( root.C2 != null ) {
			System.out.println( prefix + "C2:" );
			showTree( root.C2, prefix + indent );
		}
		if( root.sibling != null ) {
			showTree( root.sibling, prefix );
		}
	}
	
	public Main( String[] args ) {
		String fileName;
		
		if( args.length == 0 ) {
			System.out.println( "File: " );
			java.util.Scanner javaScanner = new java.util.Scanner( System.in );
			fileName = javaScanner.next();
			javaScanner.close();
		} else {
			fileName = args[ 0 ];
		}
		
		
		File file = new File( fileName );
		if( !file.exists() || !file.canRead() ) {
			System.err.println( "Error: File unreadable" );
			System.exit( -1 );
		}
		
		Scanner scan = new Scanner( file );
		
		Token currentToken = new Token( EOF );
		ArrayList<Token> allTheTokens = new ArrayList<Token>();
		do {
			currentToken = scan.getToken();
			allTheTokens.add( currentToken );
		} while( currentToken.type != EOF );
		
		System.out.print( "Tokens (Phase 1): " );
		System.out.println( allTheTokens ); //TODO: Commented out. Uncomment before turning in.
		
		realRoot = new TreeNode();
		realRoot.nodeType = PROGRAM;
		buildSubTree( realRoot, allTheTokens );
		
		System.out.println( "Tree (Phase 2): " );
		showTree( realRoot, "" ); //TODO: Commented out so I can focus on the checker. Uncomment before turning in.
		
		Checker c = new Checker();
		c.check( realRoot );
		
		System.out.println( "Symbol table (Phase 3): " );
		c.print(); //TODO: Commented out so I can focus on the code generator. Uncomment before turning in.
		
		String outputFileName = fileName + ".asm";
		File output = new File( outputFileName );
		try {
			output.createNewFile();
			if( !file.canWrite() ) {
				System.err.println( "Error: Output file unwritable" );
				System.exit( -1 );
			}
		} catch( IOException e ) {
			System.err.println( e.getLocalizedMessage() );
		}
		
		System.out.println( "Generating code (Phase 4)..." );
		CodeGen generator = new CodeGen( realRoot, output );
		generator.genCode( realRoot );
		generator.closeFile();
		System.out.println( "Done. The generated code is in the file " + outputFileName );
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main m = new Main( args ); //Escape the static! Static is a pain!
	}
	
	private void findSiblings( TreeNode root, List<Token> tokens ) {
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
				case INT: {
					node.nodeType = VARIABLE;
					node.typeSpecifier = INT;
				}
				case VOID: {
					node.typeSpecifier = start.type;
					if( !tokens.isEmpty() ) {
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
													System.exit( -1 );
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
											
											if( endOfParameterList == -1 ) {
												node.C1.typeSpecifier = VOID;
											} else {
												node.C1.typeSpecifier = INT;
												buildSubTree( node.C1, tokens.subList(0, endOfParameterList ) );
												tokens = tokens.subList( endOfParameterList + 1, tokens.size() );
											}
											
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
	
	private void findArguments( TreeNode root, List<Token> tokens ) {
		
		while( !tokens.isEmpty() ) {
			TreeNode node = new TreeNode();
			
			Token start = tokens.get( 0 );
			tokens = tokens.subList( 1, tokens.size() );
			
			switch( start.type ) {
				case NUMBER: {
					if( tokens.isEmpty() ) {
						node.nodeType = NUMBER;
						node.typeSpecifier = INT;
						node.nValue = Integer.parseInt( start.value );
					} else {
						Token second = tokens.get( 0 );
						tokens = tokens.subList( 1, tokens.size() );
						
						switch( second.type ) {
							case COMMA: {
								node.nodeType = NUMBER;
								node.typeSpecifier = INT;
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
						node.nodeType = VARIABLE; //TODO: How can we not assume it's a variable? Might be an array.
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
	
	private void findExpression( Token start, TreeNode root, List<Token> tokens ) {
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
					node.C1.typeSpecifier = INT;
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
										node.C1 = node.C1.sibling;
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
								node.nodeType = MINUS;
								node.C2 = new TreeNode();
								
								int index = tokens.indexOf( new Token( MINUS, "-" ) );
								if( index == -1 ) {
									index = tokens.size();
								}
								findExpression( null, node.C2, tokens );
								tokens = tokens.subList( index,  tokens.size() );
								
								node.C2 = node.C2.sibling;
								
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
								node.C2 = node.C2.sibling;
								break;
							}
							case ASSIGN: {
								Token second = tokens.get( 0 );
								tokens = tokens.subList( 1,  tokens.size() );
								
								switch( second.type ) {
									case NUMBER: {
										node.nodeType = ASSIGN;
										node.C2 = new TreeNode();
										node.C2.nodeType = NUMBER;
										node.C2.typeSpecifier = INT;
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
													node.C2.C1 = node.C2.C1.sibling;
													tokens = tokens.subList( bracketIndex + 1, tokens.size() );
													break;
												}
												case PLUS: {
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
	
	private void findStatementListSiblings( TreeNode root, List<Token> tokens ) {
		while( !tokens.isEmpty() ) {
			
			TreeNode node = new TreeNode();
			Token start = tokens.get( 0 );
			tokens = tokens.subList( 1, tokens.size() );
			
			switch( start.type ) {
				case WRITE: {
					node.nodeType = WRITE;
					
					int endParen = tokens.indexOf( new Token( RPAREN, ")" ) );
					node.C1 = new TreeNode();
					node.C1.nodeType = EXPRESSION;
					
					findExpression( null, node.C1, tokens.subList( 1, endParen ) );
					tokens = tokens.subList( endParen + 1, tokens.size() );
					
					break;
				}
				case READ: {
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
					
					node.nodeType = COMPOUND;
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
					node.nodeType = RETURN;
					node.C1 = new TreeNode();
					
					int tokenIndex = tokens.indexOf( new Token( SEMI, ";" ) );
					findExpression( null, node.C1, tokens.subList( 0, tokenIndex ) );
					tokens = tokens.subList( tokenIndex + 1, tokens.size() );
					node.C1 = node.C1.sibling;
					
					break;
				}
				case IF: {
					node.nodeType = IF;
					node.C1 = new TreeNode();
					
					{
						int tokenIndex = tokens.indexOf( new Token( RPAREN, ")" ) );
						findExpression( null, node.C1, tokens.subList( 1, tokenIndex ) );
						tokens = tokens.subList( tokenIndex + 1,  tokens.size() );
					}
					node.C1 = node.C1.sibling;
					
					node.C2 = new TreeNode();
					node.C2.nodeType = COMPOUND;
					{
						int endOfBlock = tokens.indexOf( new Token( RBRACE, "}" ) );
						if( endOfBlock == -1 ) {
							endOfBlock = tokens.size();
						}
						buildSubTree( node.C2, tokens.subList( 1, endOfBlock ) );
					}
					
					node.C3 = new TreeNode();
					node.C3.nodeType = COMPOUND;
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
						case LBRACKET: {
							node.nodeType = EXPRESSION; //TODO: What node type to use here?
							int tokenIndex = tokens.indexOf( new Token( SEMI, ";" ) );
							findExpression( start, node, tokens.subList( 0, tokenIndex ) );
							tokens = tokens.subList( tokenIndex + 1, tokens.size() );
							break;
						}
						case ASSIGN: {
							node.nodeType = ASSIGN;
							
							node.C1 = new TreeNode();
							node.C1.nodeType = VARIABLE;
							node.C1.sValue = start.value;
							
							node.C2 = new TreeNode();
							
							int tokenIndex = tokens.indexOf( new Token( SEMI, ";" ) );
							findExpression( start, node.C2, tokens.subList( 0, tokenIndex ) );
							tokens = tokens.subList( tokenIndex + 1, tokens.size() );
							
							node.C2 = node.C2.sibling;
							break;
						}
						case LPAREN: { //It's a function call
							tokens = tokens.subList( 1, tokens.size() );
							
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
					node.nodeType = CALL;
					break;
				}
				case WHILE: {
					node.nodeType = WHILE;
					node.C1 = new TreeNode();
					node.C1.nodeType = EXPRESSION;
					
					{
						int tokenIndex = tokens.indexOf( new Token( RPAREN, ")" ) );
						findExpression( null, node.C1, tokens.subList( 1, tokenIndex ) );
						tokens = tokens.subList( tokenIndex + 1,  tokens.size() );
					}
					
					node.C1 = node.C1.sibling;
					
					node.C2 = new TreeNode();
					node.C2.nodeType = STATEMENT_LIST;
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
	
	private void buildSubTree( TreeNode root, List<Token> tokens ) {
		switch( root.nodeType ) {
			case PROGRAM: {
				findSiblings( root, tokens );
				break;
			}
			case PARAMETER_LIST: {
				
				TreeNode node = root;
				
				switch( node.typeSpecifier ) {
					case VOID: {
						//node.typeSpecifier = VOID;
						break;
					}
					case INT: {
						findSiblings( node, tokens );
						break;
					}
					default: {
						System.err.println( "Unhandled node.typeSpecifier: " + node.typeSpecifier );
						System.exit( -1 );
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
