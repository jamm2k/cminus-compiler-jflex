import lexer.*;
import parser.*;
import semantic.*;
import codegen.*;
import java.io.*;
import java.util.List;

/**
 * Classe principal do compilador C-
 * 
 * Uso: java Main <arquivo.cm>
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java Main <arquivo.cm>");
            System.exit(1);
        }
        
        String inputFile = args[0];
        
        try {
            System.out.println("===========================================");
            System.out.println("       COMPILADOR C- - Projeto Acadêmico");
            System.out.println("===========================================\n");
            
            System.out.println("Compilando arquivo: " + inputFile + "\n");
            
            // ==== FASE 1: Análise Léxica ====
            System.out.println("[1/4] Análise Léxica...");
            FileReader reader = new FileReader(inputFile);
            Lexer lexer = new Lexer(reader);
            
            // Teste do lexer (opcional - comentar para produção)
            // testLexer(inputFile);
            
            // ==== FASE 2: Análise Sintática ====
            System.out.println("[2/4] Análise Sintática...");
            lexer = new Lexer(new FileReader(inputFile)); // recria lexer
            Parser parser = new Parser(lexer);
            Program ast = parser.parse();
            
            if (!parser.getErrors().isEmpty()) {
                System.err.println("\nErros sintáticos encontrados:");
                for (String error : parser.getErrors()) {
                    System.err.println("  " + error);
                }
                System.exit(1);
            }
            
            System.out.println("Árvore Sintática Abstrata (AST):");
            System.out.println(ast.toString(0));
            
            // ==== FASE 3: Análise Semântica ====
            System.out.println("\n[3/4] Análise Semântica...");
            SemanticAnalyzer semantic = new SemanticAnalyzer();
            semantic.analyze(ast);
            
            if (!semantic.getErrors().isEmpty()) {
                System.err.println("\nErros semânticos encontrados:");
                for (String error : semantic.getErrors()) {
                    System.err.println("  " + error);
                }
                System.exit(1);
            }
            
            semantic.getSymbolTable().printTable();
            
            // ==== FASE 4: Geração de Código ====
            System.out.println("\n[4/4] Geração de Código...");
            CodeGenerator codeGen = new CodeGenerator(semantic.getSymbolTable());
            codeGen.generate(ast);
            codeGen.printCode();
            
            // Salva código gerado
            String outputFile = inputFile.replace(".cm", ".ic");
            saveCode(outputFile, codeGen.getCode());
            
            System.out.println("\n===========================================");
            System.out.println("✓ Compilação concluída com sucesso!");
            System.out.println("✓ Código intermediário salvo em: " + outputFile);
            System.out.println("===========================================\n");
            
        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo não encontrado: " + inputFile);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Testa o analisador léxico imprimindo todos os tokens
     */
    private static void testLexer(String inputFile) throws IOException {
        System.out.println("=== Tokens Gerados ===");
        FileReader reader = new FileReader(inputFile);
        Lexer lexer = new Lexer(reader);
        
        Token token;
        while ((token = lexer.yylex()).getType() != Token.Type.EOF) {
            System.out.println(token);
        }
        System.out.println(token); // EOF token
        System.out.println();
    }
    
    /**
     * Salva o código gerado em um arquivo
     */
    private static void saveCode(String filename, List<String> code) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String line : code) {
                writer.println(line);
            }
        }
    }
}