package parser;

public abstract class Declaration extends ASTNode {
    public String name;
    public String type;
    
    public Declaration(int line, String type, String name) {
        super(line);
        this.type = type;
        this.name = name;
    }
}