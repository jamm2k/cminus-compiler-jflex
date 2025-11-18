package parser;

import java.util.List;

public class Program extends ASTNode {
    public List<Declaration> declarations;
    
    public Program(List<Declaration> declarations) {
        super(0);
        this.declarations = declarations;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("Program\n");
        for (Declaration decl : declarations) {
            sb.append(decl.toString(indent + 1));
        }
        return sb.toString();
    }
}