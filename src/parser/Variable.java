package parser;

public class Variable extends Expression {
    public String name;
    public Expression index; // null se não for array
    
    public Variable(int line, String name, Expression index) {
        super(line);
        this.name = name;
        this.index = index;
    }
    
    @Override
    public String toString(int indent) {
        if (index == null) {
            return getIndent(indent) + "Var: " + name + "\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("ArrayAccess: ").append(name).append("\n");
        sb.append(index.toString(indent + 1));
        return sb.toString();
    }
}