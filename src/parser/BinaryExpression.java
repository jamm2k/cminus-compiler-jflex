package parser;

public class BinaryExpression extends Expression {
    public String operator;
    public Expression left;
    public Expression right;
    
    public BinaryExpression(int line, String operator, Expression left, Expression right) {
        super(line);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("BinaryOp: ").append(operator).append("\n");
        sb.append(left.toString(indent + 1));
        sb.append(right.toString(indent + 1));
        return sb.toString();
    }
}