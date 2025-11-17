package lexer;

%%

%class Lexer
%public
%type Token
%line
%column

%{
    private Token token(Token.Type type) {
        return new Token(type, yytext(), yyline + 1, yycolumn + 1);
    }
    
    private Token token(Token.Type type, String lexeme) {
        return new Token(type, lexeme, yyline + 1, yycolumn + 1);
    }
%}

DIGIT = [0-9]
LETTER = [a-zA-Z]
ID = {LETTER}({LETTER}|{DIGIT})*
NUM = {DIGIT}+
WHITESPACE = [ \t\r\n]+
COMMENT = "/*" ~"*/"

%%

/* regreas lexicas */

{WHITESPACE}    {  }
{COMMENT}       {  }

/* palavras reservadas */
"if"            { return token(Token.Type.IF); }  
"else"          { return token(Token.Type.ELSE); }
"int"           { return token(Token.Type.INT); }
"return"        { return token(Token.Type.RETURN); }
"void"          { return token(Token.Type.VOID); }
"while"         { return token(Token.Type.WHILE); }

/* operadores */
"+"             { return token(Token.Type.PLUS); }
"-"             { return token(Token.Type.MINUS); }
"*"             { return token(Token.Type.MULT); }
"/"             { return token(Token.Type.DIV); }

"<"             { return token(Token.Type.LT); }
"<="            { return token(Token.Type.LE); }
">"             { return token(Token.Type.GT); }
">="            { return token(Token.Type.GE); }
"=="            { return token(Token.Type.EQ); }
"!="            { return token(Token.Type.NE); }

"="             { return token(Token.Type.ASSIGN); }

/* delimitadores */
";"             { return token(Token.Type.SEMI); }
","             { return token(Token.Type.COMMA); }
"("             { return token(Token.Type.LPAREN); }
")"             { return token(Token.Type.RPAREN); }
"["             { return token(Token.Type.LBRACKET); }
"]"             { return token(Token.Type.RBRACKET); }
"{"             { return token(Token.Type.LBRACE); }
"}"             { return token(Token.Type.RBRACE); }

{ID}            { return token(Token.Type.ID); }

{NUM}           { return token(Token.Type.NUM); }

<<EOF>>         { return token(Token.Type.EOF); }

/* erro */
.               { return token(Token.Type.ERROR, yytext()); }