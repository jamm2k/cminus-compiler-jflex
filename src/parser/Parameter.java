package parser;

public class Parameter extends ASTNode {
    public String type;
    public String name;
    public boolean isArray;
    
    public Parameter(int line, String type, String name, boolean isArray) {
        super(line);
        this.type = type;
        this.name = name;
        this.isArray = isArray;
    }
    
    @Override
    public String toString(int indent) {
        String arrStr = isArray ? "[]" : "";
        return String.format("%sParam: %s %s%s\n", getIndent(indent), type, name, arrStr);
    }
}