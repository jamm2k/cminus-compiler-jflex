package parser;

public class ExpressionStatement extends Statement {
    public Expression expression; // pode ser null (statement vazio)
    
    public ExpressionStatement(int line, Expression expression) {
        super(line);
        this.expression = expression;
    }
    
    @Override
    public String toString(int indent) {
        if (expression == null) {
            return getIndent(indent) + "EmptyStmt\n";
        }
        return expression.toString(indent);
    }
}