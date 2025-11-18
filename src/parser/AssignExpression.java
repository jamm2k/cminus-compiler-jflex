package parser;

public class AssignExpression extends Expression {
    public Variable var;
    public Expression value;
    
    public AssignExpression(int line, Variable var, Expression value) {
        super(line);
        this.var = var;
        this.value = value;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("Assign\n");
        sb.append(var.toString(indent + 1));
        sb.append(value.toString(indent + 1));
        return sb.toString();
    }
}