package parser;

public class ReturnStatement extends Statement {
    public Expression returnValue; // pode ser null
    
    public ReturnStatement(int line, Expression returnValue) {
        super(line);
        this.returnValue = returnValue;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("ReturnStmt\n");
        if (returnValue != null) {
            sb.append(returnValue.toString(indent + 1));
        }
        return sb.toString();
    }
}