package parser;

public class IfStatement extends Statement {
    public Expression condition;
    public Statement thenStmt;
    public Statement elseStmt; // pode ser null
    
    public IfStatement(int line, Expression condition, Statement thenStmt, Statement elseStmt) {
        super(line);
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("IfStmt\n");
        sb.append(getIndent(indent + 1)).append("Condition:\n");
        sb.append(condition.toString(indent + 2));
        sb.append(getIndent(indent + 1)).append("Then:\n");
        sb.append(thenStmt.toString(indent + 2));
        if (elseStmt != null) {
            sb.append(getIndent(indent + 1)).append("Else:\n");
            sb.append(elseStmt.toString(indent + 2));
        }
        return sb.toString();
    }
}