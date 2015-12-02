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
		System.out.println( bigSymbolsTable );
	}
	
	public ArrayList<Symbol> check( TreeNode root ) {
		System.out.println( "In ArrayList<Symbol> check()" );
		check( root, 0, false );
		return symbolsTable;
	}
	
	private void check( TreeNode root, int nestingLevel, boolean isInExpression ) {
		System.out.println( "In void check() nestingLevel " + nestingLevel );
		
		boolean checkC1 = false;
		boolean checkC2 = false;
		boolean checkSibling = false;
		
		switch( root.nodeType ) {
			case NUMBER: {
				//A number is not a symbol, has no children, has no siblings
				break;
			}
			case EXPRESSION: {
				//An expression is not a symbol, it has two children, and may have siblings.
				isInExpression = true;
				checkC1 = true;
				checkC2 = true;
				checkSibling = ( root.sibling != null );
			}
			case COMPOUND: {
				//A compound is not a symbol, it has two children and no siblings.
				checkC1 = true;
				checkC2 = true;
				break;
			}
			case PROGRAM: {
				//Program is not a symbol, it has no children, and has siblings. So check the siblings.
				checkSibling = true;
				break;
			}
			case ARRAY: {
				//Arrays are symbols
				Symbol array = new Symbol();
				array.ID = root.sValue;
				array.entryType = ARRAY;
				array.dataType = root.typeSpecifier;
				array.blockLevel = nestingLevel;
				array.arrayMax = root.nValue;
				array.rename = uniqueName();
				checkSibling = ( root.sibling != null );
				addSymbol( array );
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
				
				switch( root.typeSpecifier ) {
					case INT: {
						//Function is not void: it must contain a return statement
						//TODO: Finish this.
						
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
				break;
			}
			case PARAMETER_LIST: {
				//TODO: Is this all I have to do with parameter list nodes?
				checkSibling = ( root.sibling != null );
				break;
			}
			case VARIABLE: {
				//A variable must be declared before use and cannot be declared twice in the same block
				//Has it been declared before?
				boolean alreadyDeclared = false;
				for( int i = symbolsTable.size() - 1; !alreadyDeclared && i >= 0 && symbolsTable.get( i ).blockLevel <= nestingLevel; i-- ) {
					Symbol s = symbolsTable.get( i );
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
						//fjdksla;
					}
				} else {
					if( !isInExpression ) {
						System.out.println( "Variable " + root.sValue + " not already declared" );
						
						Symbol var = new Symbol();
						var.ID = root.sValue;
						var.entryType = VARIABLE;
						var.dataType = INT;
						var.blockLevel = nestingLevel;
						var.rename = uniqueName();
						addSymbol( var );
					} else {
						System.err.println( "Undeclared variable " + root.sValue + " used in expression" );
						System.exit( -1 );
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
				//An assignment is not a symbol, has two children and no siblings.
				checkC1 = true;
				checkC2 = true;
				isInExpression = true; //TODO: What happens at the end of the assignment statement: does this automatically revert to its previous value?
				break;
			}
			default: {
				System.err.println( "In void check(): Unhandled root.nodeType: " + root.nodeType );
				System.exit( -1 );
			}
		}
		
		if( checkC1 ) {
			check( root.C1, nestingLevel + 1, isInExpression ); //TODO: Is this the correct nestingLevel?
		}
		if( checkC2 ) {
			check( root.C2, nestingLevel + 1, isInExpression );
		}
		
		if( checkC1 || checkC2 ) {
			removeBlock( nestingLevel + 1 );
		}
		
		if( checkSibling ) {
			check( root.sibling, nestingLevel, isInExpression ); //TODO: Is this the correct nestingLevel?
		}
	}
}
