package codegen;

import parser.*;
import semantic.*;
import java.util.*;

public class CodeGenerator {
    private List<String> code;
    private int tempCounter;
    private int labelCounter;
    private SymbolTable symbolTable;
    
    public CodeGenerator(SymbolTable symbolTable) {
        this.code = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
        this.symbolTable = symbolTable;
    }
    
    public List<String> getCode() {
        return code;
    }
    
    private String newTemp() {
        return "t" + (tempCounter++);
    }
    
    private String newLabel() {
        return "L" + (labelCounter++);
    }
    
    private void emit(String instruction) {
        code.add(instruction);
    }
    
    public void generate(Program program) {
        emit("# Código intermediário C-");
        emit("# Formato: três endereços\n");
        
        // gera codigo das declarações globais
        for (Declaration decl : program.declarations) {
            if (decl instanceof VarDeclaration) {
                generateVarDeclaration((VarDeclaration) decl);
            } else if (decl instanceof FunctionDeclaration) {
                generateFunctionDeclaration((FunctionDeclaration) decl);
            }
        }
    }
    
    private void generateVarDeclaration(VarDeclaration varDecl) {
        if (varDecl.arraySize != null) {
            emit(String.format("ALLOC %s, %d  # array", varDecl.name, varDecl.arraySize));
        } else {
            emit(String.format("ALLOC %s, 1     # variável", varDecl.name));
        }
    }
    
    private void generateFunctionDeclaration(FunctionDeclaration funcDecl) {
        emit("\n# Função: " + funcDecl.name);
        emit("LABEL " + funcDecl.name);
        emit("BEGIN_FUNC " + funcDecl.name);
        
        for (Parameter param : funcDecl.params) {
            emit(String.format("PARAM %s", param.name));
        }
        
        generateCompoundStatement(funcDecl.body);
        
        // se for void, adiciona return (implicito)
        if (funcDecl.type.equals("void")) {
            emit("RETURN");
        }
        
        emit("END_FUNC " + funcDecl.name);
    }
    
    private void generateCompoundStatement(CompoundStatement stmt) {
        for (VarDeclaration varDecl : stmt.localVars) {
            generateVarDeclaration(varDecl);
        }
        
        for (Statement s : stmt.statements) {
            generateStatement(s);
        }
    }
    
    private void generateStatement(Statement stmt) {
        if (stmt instanceof CompoundStatement) {
            generateCompoundStatement((CompoundStatement) stmt);
            
        } else if (stmt instanceof IfStatement) {
            IfStatement ifStmt = (IfStatement) stmt;
            String condTemp = generateExpression(ifStmt.condition);
            String labelElse = newLabel();
            String labelEnd = newLabel();
            
            emit(String.format("IF_FALSE %s GOTO %s", condTemp, labelElse));
            generateStatement(ifStmt.thenStmt);
            emit(String.format("GOTO %s", labelEnd));
            emit(String.format("LABEL %s", labelElse));
            
            if (ifStmt.elseStmt != null) {
                generateStatement(ifStmt.elseStmt);
            }
            
            emit(String.format("LABEL %s", labelEnd));
            
        } else if (stmt instanceof WhileStatement) {
            WhileStatement whileStmt = (WhileStatement) stmt;
            String labelBegin = newLabel();
            String labelEnd = newLabel();
            
            emit(String.format("LABEL %s", labelBegin));
            String condTemp = generateExpression(whileStmt.condition);
            emit(String.format("IF_FALSE %s GOTO %s", condTemp, labelEnd));
            generateStatement(whileStmt.body);
            emit(String.format("GOTO %s", labelBegin));
            emit(String.format("LABEL %s", labelEnd));
            
        } else if (stmt instanceof ReturnStatement) {
            ReturnStatement retStmt = (ReturnStatement) stmt;
            if (retStmt.returnValue != null) {
                String temp = generateExpression(retStmt.returnValue);
                emit(String.format("RETURN %s", temp));
            } else {
                emit("RETURN");
            }
            
        } else if (stmt instanceof ExpressionStatement) {
            ExpressionStatement exprStmt = (ExpressionStatement) stmt;
            if (exprStmt.expression != null) {
                generateExpression(exprStmt.expression);
            }
        }
    }
    
    private String generateExpression(Expression expr) {
        if (expr instanceof AssignExpression) {
            AssignExpression assign = (AssignExpression) expr;
            String valueTemp = generateExpression(assign.value);
            
            if (assign.var.index != null) {
                // array: arr[i] = value
                String indexTemp = generateExpression(assign.var.index);
                emit(String.format("STORE_ARRAY %s[%s], %s", 
                                 assign.var.name, indexTemp, valueTemp));
            } else {
                //variável simples: x = value
                emit(String.format("STORE %s, %s", assign.var.name, valueTemp));
            }
            
            return valueTemp;
            
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expr;
            String leftTemp = generateExpression(binExpr.left);
            String rightTemp = generateExpression(binExpr.right);
            String resultTemp = newTemp();
            
            String op = getOperator(binExpr.operator);
            emit(String.format("%s = %s %s %s", resultTemp, leftTemp, op, rightTemp));
            
            return resultTemp;
            
        } else if (expr instanceof Variable) {
            Variable var = (Variable) expr;
            String temp = newTemp();
            
            if (var.index != null) {
                // Array: arr[i]
                String indexTemp = generateExpression(var.index);
                emit(String.format("%s = LOAD_ARRAY %s[%s]", temp, var.name, indexTemp));
            } else {
                // Variável simples: x
                emit(String.format("%s = LOAD %s", temp, var.name));
            }
            
            return temp;
            
        } else if (expr instanceof CallExpression) {
            CallExpression call = (CallExpression) expr;
            
            // Gera código para argumentos
            List<String> argTemps = new ArrayList<>();
            for (Expression arg : call.arguments) {
                argTemps.add(generateExpression(arg));
            }
            
            // Push dos argumentos
            for (String argTemp : argTemps) {
                emit(String.format("PUSH %s", argTemp));
            }
            
            // Chamada da função
            String resultTemp = newTemp();
            emit(String.format("%s = CALL %s, %d", resultTemp, call.functionName, 
                             call.arguments.size()));
            
            return resultTemp;
            
        } else if (expr instanceof NumberExpression) {
            NumberExpression num = (NumberExpression) expr;
            String temp = newTemp();
            emit(String.format("%s = %d", temp, num.value));
            return temp;
        }
        
        return newTemp();
    }
    
    private String getOperator(String op) {
        switch(op) {
            case "+": return "+";
            case "-": return "-";
            case "*": return "*";
            case "/": return "/";
            case "<": return "<";
            case "<=": return "<=";
            case ">": return ">";
            case ">=": return ">=";
            case "==": return "==";
            case "!=": return "!=";
            default: return op;
        }
    }
    
    public void printCode() {
        System.out.println("\n=== Código Intermediário Gerado ===");
        for (String line : code) {
            System.out.println(line);
        }
    }
}