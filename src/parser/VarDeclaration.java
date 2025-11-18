package parser;

public class VarDeclaration extends Declaration {
    public Integer arraySize; // null se não for array
    
    public VarDeclaration(int line, String type, String name, Integer arraySize) {
        super(line, type, name);
        this.arraySize = arraySize;
    }
    
    @Override
    public String toString(int indent) {
        String arrStr = arraySize != null ? "[" + arraySize + "]" : "";
        return String.format("%sVarDecl: %s %s%s\n", getIndent(indent), type, name, arrStr);
    }
}