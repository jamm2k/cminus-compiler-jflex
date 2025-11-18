package parser;

import java.util.List;

public class CallExpression extends Expression {
    public String functionName;
    public List<Expression> arguments;
    
    public CallExpression(int line, String functionName, List<Expression> arguments) {
        super(line);
        this.functionName = functionName;
        this.arguments = arguments;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("Call: ").append(functionName).append("\n");
        if (!arguments.isEmpty()) {
            sb.append(getIndent(indent + 1)).append("Arguments:\n");
            for (Expression arg : arguments) {
                sb.append(arg.toString(indent + 2));
            }
        }
        return sb.toString();
    }
}