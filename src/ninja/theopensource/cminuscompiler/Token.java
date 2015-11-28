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
		return "" + type + " " + value;
	}
}
