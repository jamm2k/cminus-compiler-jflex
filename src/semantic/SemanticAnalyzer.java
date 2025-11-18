package semantic;

import parser.*;
import java.util.*;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private List<String> errors;
    private String currentFunction;
    
    public SemanticAnalyzer() {
        symbolTable = new SymbolTable();
        errors = new ArrayList<>();
        currentFunction = null;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    private void error(int line, String message) {
        String err = String.format("Erro semântico linha %d: %s", line, message);
        errors.add(err);
        System.err.println(err);
    }
    
    public void analyze(Program program) {
        // primeira passada: adiciona todas as declarações globais
        for (Declaration decl : program.declarations) {
            if (decl instanceof VarDeclaration) {
                analyzeVarDeclaration((VarDeclaration) decl);
            } else if (decl instanceof FunctionDeclaration) {
                FunctionDeclaration func = (FunctionDeclaration) decl;
                
                // add funcao na tabela
                SymbolTable.Symbol funcSymbol = new SymbolTable.Symbol(
                    func.name, func.type, SymbolTable.Symbol.Kind.FUNCTION, 0);
                
                List<String> paramTypes = new ArrayList<>();
                for (Parameter param : func.params) {
                    paramTypes.add(param.type);
                }
                funcSymbol.setParamTypes(paramTypes);
                
                if (!symbolTable.insert(funcSymbol)) {
                    error(func.getLine(), "Função '" + func.name + "' já declarada");
                }
            }
        }
        
        SymbolTable.Symbol mainFunc = symbolTable.lookup("main");
        if (mainFunc == null) {
            error(0, "Função 'main' não encontrada");
        } else if (!mainFunc.getType().equals("void")) {
            error(0, "Função 'main' deve retornar void");
        } else if (!mainFunc.getParamTypes().isEmpty()) {
            error(0, "Função 'main' não deve ter parâmetros");
        }
        
        // segunda passada - analisa corpo das funções
        for (Declaration decl : program.declarations) {
            if (decl instanceof FunctionDeclaration) {
                analyzeFunctionDeclaration((FunctionDeclaration) decl);
            }
        }
    }
    
    private void analyzeVarDeclaration(VarDeclaration varDecl) {
        SymbolTable.Symbol.Kind kind = varDecl.arraySize != null ? 
            SymbolTable.Symbol.Kind.ARRAY : SymbolTable.Symbol.Kind.VARIABLE;
        
        SymbolTable.Symbol symbol = new SymbolTable.Symbol(
            varDecl.name, varDecl.type, kind, symbolTable.getCurrentScope());
        
        if (kind == SymbolTable.Symbol.Kind.ARRAY) {
            symbol.setArraySize(varDecl.arraySize);
            if (varDecl.arraySize <= 0) {
                error(varDecl.getLine(), "Tamanho de array deve ser maior que zero");
            }
        }
        
        if (!symbolTable.insert(symbol)) {
            error(varDecl.getLine(), "Variável '" + varDecl.name + "' já declarada neste escopo");
        }
    }
    
    private void analyzeFunctionDeclaration(FunctionDeclaration funcDecl) {
        currentFunction = funcDecl.name;
        symbolTable.enterScope();
        
        // adiciona parametros no escopo da funçao
        for (Parameter param : funcDecl.params) {
            SymbolTable.Symbol.Kind kind = param.isArray ? 
                SymbolTable.Symbol.Kind.ARRAY : SymbolTable.Symbol.Kind.PARAMETER;
            
            SymbolTable.Symbol paramSymbol = new SymbolTable.Symbol(
                param.name, param.type, kind, symbolTable.getCurrentScope());
            
            if (!symbolTable.insert(paramSymbol)) {
                error(param.getLine(), "Parâmetro '" + param.name + "' duplicado");
            }
        }
        
        analyzeCompoundStatement(funcDecl.body);
        
        symbolTable.exitScope();
        currentFunction = null;
    }
    
    private void analyzeCompoundStatement(CompoundStatement stmt) { //escopo ja foi criado na função, não cria dnv

        for (VarDeclaration varDecl : stmt.localVars) {
            analyzeVarDeclaration(varDecl);
        }
        
        for (Statement s : stmt.statements) {
            analyzeStatement(s);
        }
    }
    
    private void analyzeStatement(Statement stmt) {
        if (stmt instanceof CompoundStatement) {
            symbolTable.enterScope();
            analyzeCompoundStatement((CompoundStatement) stmt);
            symbolTable.exitScope();
        } else if (stmt instanceof IfStatement) {
            IfStatement ifStmt = (IfStatement) stmt;
            String condType = analyzeExpression(ifStmt.condition);
            if (!condType.equals("int")) {
                error(ifStmt.getLine(), "Condição do if deve ser do tipo int");
            }
            analyzeStatement(ifStmt.thenStmt);
            if (ifStmt.elseStmt != null) {
                analyzeStatement(ifStmt.elseStmt);
            }
        } else if (stmt instanceof WhileStatement) {
            WhileStatement whileStmt = (WhileStatement) stmt;
            String condType = analyzeExpression(whileStmt.condition);
            if (!condType.equals("int")) {
                error(whileStmt.getLine(), "Condição do while deve ser do tipo int");
            }
            analyzeStatement(whileStmt.body);
        } else if (stmt instanceof ReturnStatement) {
            ReturnStatement retStmt = (ReturnStatement) stmt;
            SymbolTable.Symbol func = symbolTable.lookup(currentFunction);
            
            if (retStmt.returnValue == null) {
                if (!func.getType().equals("void")) {
                    error(retStmt.getLine(), "Função '" + currentFunction + 
                          "' deve retornar " + func.getType());
                }
            } else {
                String retType = analyzeExpression(retStmt.returnValue);
                if (!retType.equals(func.getType())) {
                    error(retStmt.getLine(), "Tipo de retorno incompatível. Esperado: " + 
                          func.getType() + ", Encontrado: " + retType);
                }
            }
        } else if (stmt instanceof ExpressionStatement) {
            ExpressionStatement exprStmt = (ExpressionStatement) stmt;
            if (exprStmt.expression != null) {
                analyzeExpression(exprStmt.expression);
            }
        }
    }
    
    private String analyzeExpression(Expression expr) {
        if (expr instanceof AssignExpression) {
            AssignExpression assign = (AssignExpression) expr;
            String varType = analyzeVariable(assign.var);
            String valueType = analyzeExpression(assign.value);
            
            if (!varType.equals(valueType)) {
                error(assign.getLine(), "Tipos incompatíveis na atribuição");
            }
            return varType;
            
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expr;
            String leftType = analyzeExpression(binExpr.left);
            String rightType = analyzeExpression(binExpr.right);
            
            if (!leftType.equals("int") || !rightType.equals("int")) {
                error(binExpr.getLine(), "Operandos devem ser do tipo int");
            }
            return "int";
            
        } else if (expr instanceof Variable) {
            return analyzeVariable((Variable) expr);
            
        } else if (expr instanceof CallExpression) {
            CallExpression call = (CallExpression) expr;
            SymbolTable.Symbol func = symbolTable.lookup(call.functionName);
            
            if (func == null) {
                error(call.getLine(), "Função '" + call.functionName + "' não declarada");
                return "int";
            }
            
            if (func.getKind() != SymbolTable.Symbol.Kind.FUNCTION) {
                error(call.getLine(), "'" + call.functionName + "' não é uma função");
                return "int";
            }
            
            if (call.arguments.size() != func.getParamTypes().size()) {
                error(call.getLine(), "Numero incorreto de argumentos. Esperado: " + 
                      func.getParamTypes().size() + ", Encontrado: " + call.arguments.size());
            } else {

                for (int i = 0; i < call.arguments.size(); i++) { //verifica os tipos de cada argumento
                    Expression arg = call.arguments.get(i);
                    String expectedType = func.getParamTypes().get(i);
                    
                    if (arg instanceof Variable) {
                        Variable varArg = (Variable) arg;
                        SymbolTable.Symbol argSymbol = symbolTable.lookup(varArg.name);
                        
                        if (argSymbol != null && 
                            argSymbol.getKind() == SymbolTable.Symbol.Kind.ARRAY && //aceita array sem indice
                            varArg.index == null) {
                            continue;
                        }
                    }
                    
                    String argType = analyzeExpression(arg);
                    
                    if (!argType.equals(expectedType)) {
                        error(call.getLine(), "Tipo do argumento " + (i+1) + 
                              " incompativel. Esperado: " + expectedType + 
                              ", Encontrado: " + argType);
                    }
                }
            }
            
            return func.getType();
            
        } else if (expr instanceof NumberExpression) {
            return "int";
        }
        
        return "int";
    }
    
    private String analyzeVariable(Variable var) {
        SymbolTable.Symbol symbol = symbolTable.lookup(var.name);
        
        if (symbol == null) {
            error(var.getLine(), "Variavel '" + var.name + "' nao declarada");
            return "int";
        }
        
        //verifica se é array
        if (var.index != null) {
            // usando como array: arr[i]
            if (symbol.getKind() != SymbolTable.Symbol.Kind.ARRAY &&
                symbol.getKind() != SymbolTable.Symbol.Kind.PARAMETER) {
                error(var.getLine(), "'" + var.name + "' nao e um array");
            }
            String indexType = analyzeExpression(var.index);
            if (!indexType.equals("int")) {
                error(var.getLine(), "Indice do array deve ser do tipo int");
            }
        } else {
            if (symbol.getKind() == SymbolTable.Symbol.Kind.ARRAY) {
            }
        }
        
        return symbol.getType();
    }
}