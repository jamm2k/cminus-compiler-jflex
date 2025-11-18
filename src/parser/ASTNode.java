package parser;

/**
 * Classe base para nós da Árvore Sintática Abstrata (AST)
 */
public abstract class ASTNode {
    protected int line;
    
    public ASTNode(int line) {
        this.line = line;
    }
    
    public int getLine() {
        return line;
    }
    
    public abstract String toString(int indent);
    
    protected String getIndent(int level) {
        return "  ".repeat(level);
    }
}