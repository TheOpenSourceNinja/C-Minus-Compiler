package ninja.theopensource.cminuscompiler;

public class Symbol {
	public String ID; //The lexeme
	public int entryType; //variable, array, etc.
	public int dataType; //INT or VOID
	public int blockLevel; //The nesting level: 0 is unnested, 1 is a single level deep, etc.
	public TreeNode parameterList; //Just copied from the syntax tree
	public int returnType; //For functions: INT or VOID
	public int arrayMax; //The size of an array
	public String rename; //Each variable is given a unique name
	
	public String toString() {
		return "ID: " + ID + " dataType: " + dataType + " blockLevel: " + blockLevel + " returnType: " + returnType + " arrayMax: " + arrayMax + " Rename: " + rename + "\n";
	}
}
