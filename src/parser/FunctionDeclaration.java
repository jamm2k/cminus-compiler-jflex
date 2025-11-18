package parser;

import java.util.List;

public class FunctionDeclaration extends Declaration {
    public List<Parameter> params;
    public CompoundStatement body;
    
    public FunctionDeclaration(int line, String type, String name, 
                              List<Parameter> params, CompoundStatement body) {
        super(line, type, name);
        this.params = params;
        this.body = body;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("FunctionDecl: ")
          .append(type).append(" ").append(name).append("\n");
        sb.append(getIndent(indent + 1)).append("Parameters:\n");
        for (Parameter p : params) {
            sb.append(p.toString(indent + 2));
        }
        sb.append(getIndent(indent + 1)).append("Body:\n");
        sb.append(body.toString(indent + 2));
        return sb.toString();
    }
}