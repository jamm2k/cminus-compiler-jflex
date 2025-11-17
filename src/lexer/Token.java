package lexer;


public class Token {
    public enum Type {
        IF, ELSE, INT, RETURN, VOID, WHILE, //palavras reservadas
        
        PLUS, MINUS, MULT, DIV, //operadores
        LT, LE, GT, GE, EQ, NE,
        ASSIGN,
        
        SEMI, COMMA,
        LPAREN, RPAREN,
        LBRACKET, RBRACKET,
        LBRACE, RBRACE,
        
        ID, NUM,
        
        EOF, ERROR
    }
    
    private Type type;
    private String lexeme;
    private int line;
    private int column;
    
    public Token(Type type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    @Override
    public String toString() {
        return String.format("Token{type=%s, lexeme='%s', line=%d, col=%d}", 
                           type, lexeme, line, column);
    }
    
    
    public static Type getKeywordType(String word) { //verifica se é uma palavra reservada
        switch(word) {
            case "if": return Type.IF;
            case "else": return Type.ELSE;
            case "int": return Type.INT;
            case "return": return Type.RETURN;
            case "void": return Type.VOID;
            case "while": return Type.WHILE;
            default: return Type.ID;
        }
    }
}