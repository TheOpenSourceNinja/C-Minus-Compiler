package ninja.theopensource.cminuscompiler;

import java.util.ArrayList;

public class Checker implements Constants {
	private ArrayList<Symbol> symbolsTable;
	private ArrayList<Symbol> bigSymbolsTable;
	private String varNamePrefix;
	private int varNameSuffix;
	
	private void addSymbol( Symbol s ) {
		symbolsTable.add( s );
		bigSymbolsTable.add( s );
	}
	
	private void removeBlock( int block ) {
		while( symbolsTable.get( symbolsTable.size() - 1 ).blockLevel == block ) {
			symbolsTable.remove( symbolsTable.size() - 1 );
		}
	}
	
	private String uniqueName() {
		String name = varNamePrefix + varNameSuffix;
		varNameSuffix += 1;
		return name;
	}
	
	public Checker() {
		symbolsTable = new ArrayList<Symbol>();
		bigSymbolsTable = new ArrayList<Symbol>();
		varNamePrefix = "var";
		varNameSuffix = 0;
	}
	
	public void print() {
		System.out.println( "Small symbols table:" );
		System.out.println( symbolsTable );
		System.out.println( "Big symbols table:" );
		System.out.println( bigSymbolsTable );
	}
	
	public ArrayList<Symbol> check( TreeNode root ) {
		//System.out.println( "In ArrayList<Symbol> check()" );
		check( root, 0, false );
		return symbolsTable;
	}
	
	private void check( TreeNode root, int nestingLevel, boolean isInExpression ) {
		//System.out.println( "In void check() nestingLevel " + nestingLevel );
		
		boolean checkC1 = false;
		boolean checkC2 = false;
		boolean checkC3 = false;
		int c2NestingLevel = nestingLevel;
		int c1NestingLevel = nestingLevel;
		int c3NestingLevel = nestingLevel;
		boolean checkSibling = false;
		boolean siblingIsInExpression = isInExpression;
		
		switch( root.nodeType ) {
			case NUMBER: {
				//A number is not a symbol, has no children, has no siblings
				break;
			}
			case EXPRESSION: {
				//An expression is not a symbol, it has two children, and may have siblings.
				
				checkC1 = ( root.C1 != null );
				checkC2 = ( root.C2 != null );
				siblingIsInExpression = isInExpression;
				checkSibling = ( root.sibling != null );
				isInExpression = true;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				break;
			}
			case COMPOUND: {
				//A compound is not a symbol, it has two children and no siblings.
				checkC1 = true;
				checkC2 = true;
				c1NestingLevel = nestingLevel + 1;
				c2NestingLevel = c1NestingLevel;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}

				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				break;
			}
			case PROGRAM: {
				//Program is not a symbol, it has no children, and has siblings. So check the siblings.
				checkSibling = true;
				break;
			}
			case ARRAY: {
				//Arrays are symbols. They may have siblings and/or a C1 child.
				Symbol array = new Symbol();
				array.ID = root.sValue;
				array.entryType = ARRAY;
				array.dataType = root.typeSpecifier;
				array.blockLevel = nestingLevel;
				array.arrayMax = root.nValue;
				array.rename = uniqueName();
				root.rename = array.rename;
				addSymbol( array );
				
				checkSibling = ( root.sibling != null );
				checkC1 = ( root.C1 != null );
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				break;
			}
			case FUNCTION: {
				//Functions are symbols.
				Symbol function = new Symbol();
				function.ID = root.sValue;
				function.returnType = root.typeSpecifier;
				function.blockLevel = nestingLevel;
				function.entryType = FUNCTION;
				function.parameterList = root.C1;
				addSymbol( function );
				
				c1NestingLevel = nestingLevel + 1;
				c2NestingLevel = c1NestingLevel;
				
				switch( root.typeSpecifier ) {
					case INT: {
						//Function is not void: it must contain a return statement
						
						TreeNode compound = root.C2;
						TreeNode statementList = compound.C2;
						TreeNode current = statementList.sibling;
						
						boolean returnFound = false;
						
						while( !returnFound && current != null ) {
							returnFound = ( current.nodeType == RETURN );
							current = current.sibling;
						}
						
						if( !returnFound ) {
							System.err.println( "Function " + function.ID + " has no return statement." );
							System.exit( -1 );
						}
						
						break;
					}
					case VOID: {
						//Function is void: it must not be used in expressions
						if( isInExpression ) {
							System.err.println( "Function " + function.ID + " is void and thus cannot be used inside an expression." );
							System.exit( -1 );
						}
						
						TreeNode compound = root.C2;
						TreeNode statementList = compound.C2;
						TreeNode current = statementList.sibling;
						
						boolean returnFound = false;
						
						while( !returnFound && current != null ) {
							returnFound = ( current.nodeType == RETURN );
							current = current.sibling;
						}
						
						if( returnFound ) {
							System.err.println( "Function " + function.ID + " is void and thus must not have a return statement." );
							System.exit( -1 );
						}
						break;
					}
					default: {
						System.err.println( "In void check(): Unhandled function typeSpecifier: " + root.typeSpecifier );
						System.exit( -1 );
					}
				}
				
				checkC1 = true;
				checkC2 = true;
				checkSibling = ( root.sibling != null );
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				break;
			}
			case PARAMETER_LIST: {
				//A parameter list is not a symbol, has no children, and may have siblings.
				checkSibling = ( root.sibling != null );
				break;
			}
			case VARIABLE: {
				//A variable must be declared before use and cannot be declared twice in the same block
				//Has it been declared before?
				boolean alreadyDeclared = false;
				Symbol s = null;
				for( int i = symbolsTable.size() - 1; !alreadyDeclared && i >= 0 && symbolsTable.get( i ).blockLevel <= nestingLevel; i-- ) {
					s = symbolsTable.get( i );
					if( s.ID.equals( root.sValue ) && s.entryType == VARIABLE ) {
						alreadyDeclared = true;
						break;
					}
				}
				
				if( alreadyDeclared ) {
					if( !isInExpression ) {
						System.err.println( "Variable " + root.sValue + " already declared" );
						System.exit( -1 );
					} else {
						root.rename = s.rename;
					}
				} else {
					if( !isInExpression ) {
						//System.out.println( "Variable " + root.sValue + " not already declared" );
						
						Symbol var = new Symbol();
						var.ID = root.sValue;
						var.entryType = VARIABLE;
						var.dataType = INT;
						var.blockLevel = nestingLevel;
						var.rename = uniqueName();
						root.rename = var.rename;
						addSymbol( var );
					} else {
						if( !arrayAlreadyDeclaredInExpression( root, nestingLevel ) ) {
							System.err.println( "Undeclared variable " + root.sValue + " used in expression" );
							System.exit( -1 );
						}
					}
				}
				
				checkSibling = ( root.sibling != null );
				break;
			}
			case DECLARATION: {
				//A declaration is not a symbol, has no children, and may or may not have siblings.
				checkSibling = ( root.sibling != null );
				break;
			}
			case STATEMENT_LIST: {
				//A statement list is not a symbol, has no children, and may or may not have siblings.
				checkSibling = ( root.sibling != null );
				break;
			}
			case ASSIGN: {
				//An assignment is not a symbol, has two children, and may have siblings.
				checkC1 = true;
				checkC2 = true;
				
				siblingIsInExpression = isInExpression;
				checkSibling = ( root.sibling != null );
				isInExpression = true;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				break;
			}
			case MINUS:
			case PLUS: {
				//A plus or minus is part of an expression, is not a symbol, has two children, and may have siblings.
				checkC1 = true;
				checkC2 = true;
				
				siblingIsInExpression = isInExpression;
				checkSibling = ( root.sibling != null );
				isInExpression = true;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				break;
			}
			case WHILE: {
				//A while is not a symbol, has two children, and may have siblings.
				checkC1 = true;
				checkC2 = true;
				checkSibling = ( root.sibling != null );
				c2NestingLevel = nestingLevel + 1;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				break;
			}
			case LS: {
				//A less-than is not a symbol, and has two children. Does it have siblings? Not sure, so let's err on the side of caution.
				checkC1 = true;
				checkC2 = true;
				checkSibling = ( root.sibling != null );
				isInExpression = true;
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				break;
			}
			case IF: {
				//An if is not a symbol, has (up to?) three children, and may have siblings.
				checkC1 = ( root.C1 != null );
				checkC2 = ( root.C2 != null );
				checkC3 = ( root.C3 != null );
				checkSibling = ( root.sibling != null );
				
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				if( c1NestingLevel > nestingLevel ) {
					removeBlock( c1NestingLevel );
				}
				
				c2NestingLevel = nestingLevel + 1;
				if( checkC2 ) {
					check( root.C2, c2NestingLevel, isInExpression );
				}
				if( c2NestingLevel > nestingLevel ) {
					removeBlock( c2NestingLevel );
				}
				
				c3NestingLevel = nestingLevel + 1;
				if( checkC3 ) {
					check( root.C3, c1NestingLevel, isInExpression );
				}
				if( c3NestingLevel > nestingLevel ) {
					removeBlock( c3NestingLevel );
				}
				
				break;
			}
			case RETURN: {
				//A return node is not a symbol and has up to one child.
				checkC1 = ( root.C1 != null );
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				break;
			}
			case CALL: {
				//A function call is not a symbol, one child, and maybe siblings.
				checkC1 = true;
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				
				checkSibling = ( root.sibling != null );
				
				break;
			}
			case ARGUMENTS: {
				//An arguments node is not a symbol, has no children, and siblings.
				checkSibling = ( root.sibling != null );
				break;
			}
			case READ: {
				//A read node is not a symbol, has one child, and siblings.
				checkC1 = true;
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				checkSibling = ( root.sibling != null );
				break;
			}
			case WRITE: {
				//A write node is not a symbol, has one child, and siblings.
				checkC1 = true;
				if( checkC1 ) {
					check( root.C1, c1NestingLevel, isInExpression );
				}
				checkSibling = ( root.sibling != null );
				break;
			}
			default: {
				System.err.println( "In void check(): Unhandled root.nodeType: " + root.nodeType );
				System.exit( -1 );
			}
		}
		
		if( root.C1 != null && !checkC1 ) {
			System.err.println( "C1 error" );
		}
		if( root.C2 != null && !checkC2 ) {
			System.err.println( "C2 error" );
		}
		
		isInExpression = siblingIsInExpression;
		
		if( checkSibling ) {
			check( root.sibling, nestingLevel, isInExpression );
		}
	}

	private boolean arrayAlreadyDeclaredInExpression( TreeNode root, int nestingLevel ) {
		boolean alreadyDeclared = false;
		Symbol s = null;
		for( int i = symbolsTable.size() - 1; !alreadyDeclared && i >= 0 && symbolsTable.get( i ).blockLevel <= nestingLevel; i-- ) {
			s = symbolsTable.get( i );
			if( s.ID.equals( root.sValue ) && s.entryType == ARRAY ) {
				alreadyDeclared = true;
				break;
			}
		}
		
		if( alreadyDeclared ) {
			root.nodeType = ARRAY;
			root.rename = s.rename;
		}
		return alreadyDeclared;
	}
}
