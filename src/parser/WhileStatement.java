package parser;

public class WhileStatement extends Statement {
    public Expression condition;
    public Statement body;
    
    public WhileStatement(int line, Expression condition, Statement body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("WhileStmt\n");
        sb.append(getIndent(indent + 1)).append("Condition:\n");
        sb.append(condition.toString(indent + 2));
        sb.append(getIndent(indent + 1)).append("Body:\n");
        sb.append(body.toString(indent + 2));
        return sb.toString();
    }
}