package parser;

public class NumberExpression extends Expression {
    public int value;
    
    public NumberExpression(int line, int value) {
        super(line);
        this.value = value;
    }
    
    @Override
    public String toString(int indent) {
        return getIndent(indent) + "Num: " + value + "\n";
    }
}