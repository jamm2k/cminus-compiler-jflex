package parser;

import java.util.List;

public class CompoundStatement extends Statement {
    public List<VarDeclaration> localVars;
    public List<Statement> statements;
    
    public CompoundStatement(int line, List<VarDeclaration> localVars, 
                           List<Statement> statements) {
        super(line);
        this.localVars = localVars;
        this.statements = statements;
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("CompoundStmt\n");
        if (!localVars.isEmpty()) {
            sb.append(getIndent(indent + 1)).append("LocalVars:\n");
            for (VarDeclaration v : localVars) {
                sb.append(v.toString(indent + 2));
            }
        }
        for (Statement s : statements) {
            sb.append(s.toString(indent + 1));
        }
        return sb.toString();
    }
}