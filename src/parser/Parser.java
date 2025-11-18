package parser;

import lexer.*;
import java.io.*;
import java.util.*;

/**
 * Analisador sintático recursivo descendente para C-
 */
public class Parser {
    private Lexer lexer;
    private Token currentToken;
    private List<String> errors;
    
    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.errors = new ArrayList<>();
        advance(); // lê primeiro token
    }
    
    private void advance() throws IOException {
        currentToken = lexer.yylex();
        if (currentToken == null) {
            currentToken = new Token(Token.Type.EOF, "", 0, 0);
        }
    }
    
    private boolean match(Token.Type type) {
        return currentToken.getType() == type;
    }
    
    private void expect(Token.Type type) throws IOException {
        if (!match(type)) {
            error("Esperado " + type + " mas encontrado " + currentToken.getType());
        }
        advance();
    }
    
    private void error(String message) {
        String err = String.format("Erro na linha %d: %s", currentToken.getLine(), message);
        errors.add(err);
        System.err.println(err);
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    // programa → declaracao-lista
    public Program parse() throws IOException {
        List<Declaration> declarations = new ArrayList<>();
        
        while (!match(Token.Type.EOF)) {
            Declaration decl = parseDeclaration();
            if (decl != null) {
                declarations.add(decl);
            }
        }
        
        return new Program(declarations);
    }
    
    // declaracao → var-declaracao | fun-declaracao
    private Declaration parseDeclaration() throws IOException {
        if (!match(Token.Type.INT) && !match(Token.Type.VOID)) {
            error("Esperado tipo (int ou void)");
            advance();
            return null;
        }
        
        String type = currentToken.getLexeme();
        int line = currentToken.getLine();
        advance();
        
        if (!match(Token.Type.ID)) {
            error("Esperado identificador");
            return null;
        }
        
        String name = currentToken.getLexeme();
        advance();
        
        // Verifica se é função ou variável
        if (match(Token.Type.LPAREN)) {
            return parseFunctionDeclaration(line, type, name);
        } else {
            return parseVarDeclaration(line, type, name);
        }
    }
    
    // var-declaracao → tipo ID ; | tipo ID [ NUM ] ;
    private VarDeclaration parseVarDeclaration(int line, String type, String name) 
            throws IOException {
        Integer arraySize = null;
        
        if (match(Token.Type.LBRACKET)) {
            advance();
            if (!match(Token.Type.NUM)) {
                error("Esperado número para tamanho do array");
            } else {
                arraySize = Integer.parseInt(currentToken.getLexeme());
                advance();
            }
            expect(Token.Type.RBRACKET);
        }
        
        expect(Token.Type.SEMI);
        return new VarDeclaration(line, type, name, arraySize);
    }
    
    // fun-declaracao → tipo ID ( params ) composto-decl
    private FunctionDeclaration parseFunctionDeclaration(int line, String type, String name) 
            throws IOException {
        expect(Token.Type.LPAREN);
        List<Parameter> params = parseParams();
        expect(Token.Type.RPAREN);
        CompoundStatement body = parseCompoundStatement();
        
        return new FunctionDeclaration(line, type, name, params, body);
    }
    
    // params → param-lista | void | vazio
    private List<Parameter> parseParams() throws IOException {
        List<Parameter> params = new ArrayList<>();
        
        if (match(Token.Type.VOID)) {
            advance();
            return params; // retorna lista vazia
        }
        
        if (match(Token.Type.INT)) {
            params.add(parseParam());
            
            while (match(Token.Type.COMMA)) {
                advance();
                params.add(parseParam());
            }
        }
        
        return params;
    }
    
    // param → tipo ID | tipo ID [ ]
    private Parameter parseParam() throws IOException {
        if (!match(Token.Type.INT)) {
            error("Esperado tipo int");
        }
        advance();
        
        if (!match(Token.Type.ID)) {
            error("Esperado identificador");
        }
        String name = currentToken.getLexeme();
        int line = currentToken.getLine();
        advance();
        
        boolean isArray = false;
        if (match(Token.Type.LBRACKET)) {
            advance();
            expect(Token.Type.RBRACKET);
            isArray = true;
        }
        
        return new Parameter(line, "int", name, isArray);
    }
    
    // composto-decl → { local-declaracoes statement-lista }
    private CompoundStatement parseCompoundStatement() throws IOException {
        int line = currentToken.getLine();
        expect(Token.Type.LBRACE);
        
        List<VarDeclaration> localVars = new ArrayList<>();
        while (match(Token.Type.INT)) {
            String type = currentToken.getLexeme();
            int varLine = currentToken.getLine();
            advance();
            
            if (!match(Token.Type.ID)) {
                error("Esperado identificador");
                continue;
            }
            String name = currentToken.getLexeme();
            advance();
            
            localVars.add(parseVarDeclaration(varLine, type, name));
        }
        
        List<Statement> statements = new ArrayList<>();
        while (!match(Token.Type.RBRACE) && !match(Token.Type.EOF)) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        
        expect(Token.Type.RBRACE);
        return new CompoundStatement(line, localVars, statements);
    }
    
    // statement → expressao-decl | composto-decl | selecao-decl | iteracao-decl | retorno-decl
    private Statement parseStatement() throws IOException {
        if (match(Token.Type.LBRACE)) {
            return parseCompoundStatement();
        } else if (match(Token.Type.IF)) {
            return parseIfStatement();
        } else if (match(Token.Type.WHILE)) {
            return parseWhileStatement();
        } else if (match(Token.Type.RETURN)) {
            return parseReturnStatement();
        } else {
            return parseExpressionStatement();
        }
    }
    
    // selecao-decl → if ( expressao ) statement | if ( expressao ) statement else statement
    private IfStatement parseIfStatement() throws IOException {
        int line = currentToken.getLine();
        expect(Token.Type.IF);
        expect(Token.Type.LPAREN);
        Expression condition = parseExpression();
        expect(Token.Type.RPAREN);
        Statement thenStmt = parseStatement();
        
        Statement elseStmt = null;
        if (match(Token.Type.ELSE)) {
            advance();
            elseStmt = parseStatement();
        }
        
        return new IfStatement(line, condition, thenStmt, elseStmt);
    }
    
    // iteracao-decl → while ( expressao ) statement
    private WhileStatement parseWhileStatement() throws IOException {
        int line = currentToken.getLine();
        expect(Token.Type.WHILE);
        expect(Token.Type.LPAREN);
        Expression condition = parseExpression();
        expect(Token.Type.RPAREN);
        Statement body = parseStatement();
        
        return new WhileStatement(line, condition, body);
    }
    
    // retorno-decl → return ; | return expressao ;
    private ReturnStatement parseReturnStatement() throws IOException {
        int line = currentToken.getLine();
        expect(Token.Type.RETURN);
        
        Expression returnValue = null;
        if (!match(Token.Type.SEMI)) {
            returnValue = parseExpression();
        }
        
        expect(Token.Type.SEMI);
        return new ReturnStatement(line, returnValue);
    }
    
    // expressao-decl → expressao ; | ;
    private ExpressionStatement parseExpressionStatement() throws IOException {
        int line = currentToken.getLine();
        
        if (match(Token.Type.SEMI)) {
            advance();
            return new ExpressionStatement(line, null);
        }
        
        Expression expr = parseExpression();
        expect(Token.Type.SEMI);
        return new ExpressionStatement(line, expr);
    }
    
    // expressao → var = expressao | simples-expressao
    private Expression parseExpression() throws IOException {
        Expression expr = parseSimpleExpression();
        
        // Verifica se é uma atribuição
        if (expr instanceof Variable && match(Token.Type.ASSIGN)) {
            Variable var = (Variable) expr;
            int line = var.getLine();
            advance(); // consome '='
            Expression value = parseExpression();
            return new AssignExpression(line, var, value);
        }
        
        return expr;
    }
    
    // simples-expressao → soma-expressao relacional soma-expressao | soma-expressao
    private Expression parseSimpleExpression() throws IOException {
        Expression left = parseAdditiveExpression();
        
        if (isRelationalOp()) {
            String op = currentToken.getLexeme();
            int line = currentToken.getLine();
            advance();
            Expression right = parseAdditiveExpression();
            return new BinaryExpression(line, op, left, right);
        }
        
        return left;
    }
    
    private boolean isRelationalOp() {
        return match(Token.Type.LE) || match(Token.Type.LT) || 
               match(Token.Type.GT) || match(Token.Type.GE) || 
               match(Token.Type.EQ) || match(Token.Type.NE);
    }
    
    // soma-expressao → soma-expressao soma termo | termo
    private Expression parseAdditiveExpression() throws IOException {
        Expression left = parseTerm();
        
        while (match(Token.Type.PLUS) || match(Token.Type.MINUS)) {
            String op = currentToken.getLexeme();
            int line = currentToken.getLine();
            advance();
            Expression right = parseTerm();
            left = new BinaryExpression(line, op, left, right);
        }
        
        return left;
    }
    
    // termo → termo mult fator | fator
    private Expression parseTerm() throws IOException {
        Expression left = parseFactor();
        
        while (match(Token.Type.MULT) || match(Token.Type.DIV)) {
            String op = currentToken.getLexeme();
            int line = currentToken.getLine();
            advance();
            Expression right = parseFactor();
            left = new BinaryExpression(line, op, left, right);
        }
        
        return left;
    }
    
    // fator → ( expressao ) | var | chamada | NUM
    private Expression parseFactor() throws IOException {
        int line = currentToken.getLine();
        
        if (match(Token.Type.LPAREN)) {
            advance();
            Expression expr = parseExpression();
            expect(Token.Type.RPAREN);
            return expr;
        } else if (match(Token.Type.NUM)) {
            int value = Integer.parseInt(currentToken.getLexeme());
            advance();
            return new NumberExpression(line, value);
        } else if (match(Token.Type.ID)) {
            String name = currentToken.getLexeme();
            advance();
            
            if (match(Token.Type.LBRACKET)) {
                advance();
                Expression index = parseExpression();
                expect(Token.Type.RBRACKET);
                return new Variable(line, name, index);
            } else if (match(Token.Type.LPAREN)) {
                advance();
                List<Expression> args = parseArgs();
                expect(Token.Type.RPAREN);
                return new CallExpression(line, name, args);
            } else {
                return new Variable(line, name, null);
            }
        } else {
            error("Fator inválido");
            advance();
            return new NumberExpression(line, 0);
        }
    }
    
    // args → arg-lista | vazio
    private List<Expression> parseArgs() throws IOException {
        List<Expression> args = new ArrayList<>();
        
        if (!match(Token.Type.RPAREN)) {
            args.add(parseExpression());
            
            while (match(Token.Type.COMMA)) {
                advance();
                args.add(parseExpression());
            }
        }
        
        return args;
    }
}