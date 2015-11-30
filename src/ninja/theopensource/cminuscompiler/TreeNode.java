package ninja.theopensource.cminuscompiler;

public class TreeNode implements Constants { 
	   public int lineNumber;     //Line in program where this construct is found 
	   public int nValue;         //Numerical value of a number 
	   public String sValue;      //Lexeme or string value of an identifier 
	   public int nodeType;       //PROGRAM, DECLARATION, etc. 
	   public int typeSpecifier;  //VOID or INT 
	   public String rename;      //Used by the Semantic Analyzer 
	   public boolean visited;    //Initialized to false, used for traversals 
	   public TreeNode C1;        //Pointer to Child 1 
	   public TreeNode C2;        //Pointer to Child 2 
	   public TreeNode C3;        //Pointer to Child 3 
	   public TreeNode sibling;   //Pointer to Sibling
	   
	   /*public TreeNode( int newNodeType ) {
		   nodeType = newNodeType;
	   }*/
	} 