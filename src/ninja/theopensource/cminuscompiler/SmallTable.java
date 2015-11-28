package ninja.theopensource.cminuscompiler;

/*
 * Author: broegb
 * Created: Thursday, August 07, 2003 7:09:00 AM
 * Modified: Thursday, August 07, 2003 7:09:00 AM
 */
import java.io.*;

public class SmallTable {
	TreeNode [] table;
	int current;
	final int MAX = 50;
	
	public SmallTable() {
		table = new TreeNode[ MAX ];
		current = -1;
		
		for( int i = 0; i < MAX; i++ )
			table[ i ] = new TreeNode( );
	}
	
	public void reset() {
		current = -1;
	}
	
	public boolean isEmpty() {
		return( current == -1 );
	}
	
	public int numberOfEntries() {
		return ( current + 1 );
	}
	
	public TreeNode entry( int i ) {
		if( (i >= 0) && (i <= current) )
			return table[ i ];
		else
			return null;
	}
	
	public void emit( PrintWriter f ) {
		if( f != null ) {
			for( int i = 0; i <= current; i++ ) {
				if( table[ i ].nodeType == Token.VARIABLE )
					//f.println( table[ i ].rename + ":\t\t 0" );
					f.println( "\t\t pushc 0 \t ;local variable " + table[ i ].rename );
				else if( table[ i ].nodeType == Token.ARRAY ) {
					//f.println( table[ i ].rename + ":\t\t.block " + table[ i ].nValue );
					int k = table[ i ].nValue;
					for( int j = 0; j < k; j++ )
						f.println( "\t\t pushc 0 \t ;local array " + table[ i ].rename + "[" + j + "]" );
				}
			}
		}
	}
	
	public int lookUp( String entry ) {
		int location = -1;
		
		for( int i = current; i >= 0; i-- ) {
			if( table[ i ].rename.equals( entry ) ) {
				location = i;
				break;
			}
		}
		return location;
	}

	public void insert( TreeNode entry ) {
		int index = lookUp( entry.rename );
		
		if( index < 0 ) {
			current++;
			table[ current ] = entry;
		}
	}
	
	public void printTable() {
		if( current == -1 )
			System.out.println( "No Entries" );
		else {
			for( int i = current; i >= 0; i-- )
				System.out.println( table[ i ].rename );
		}
	}

}
