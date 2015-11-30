package ninja.theopensource.cminuscompiler;

public class Token implements Constants {
	public int type;
	public String value;
	
	public Token( int newType ) {
		type = newType;
		value = "";
	}
	
	public Token( int newType, String newValue ) {
		type = newType;
		value = newValue;
	}
	
	public String toString() {
		return value;
	}
	
	public boolean equals( Object o ) {
		Token other = (Token) o;
		if( this.type == other.type && this.value.equals( other.value ) ) {
			return true;
		} else {
			return false;
		}
	}
}
