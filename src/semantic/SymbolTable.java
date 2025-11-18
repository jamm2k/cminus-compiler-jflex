package semantic;

import java.util.*;


public class SymbolTable {
    
    public static class Symbol {
        public enum Kind { VARIABLE, ARRAY, FUNCTION, PARAMETER }
        
        private String name;
        private String type; // "int" ou "void"
        private Kind kind;
        private int arraySize; // arrays
        private List<String> paramTypes; //funções
        private int scope;
        
        public Symbol(String name, String type, Kind kind, int scope) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.scope = scope;
            this.arraySize = -1;
            this.paramTypes = new ArrayList<>();
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public Kind getKind() { return kind; }
        public int getScope() { return scope; }
        public int getArraySize() { return arraySize; }
        public List<String> getParamTypes() { return paramTypes; }
        
        public void setArraySize(int size) { this.arraySize = size; }
        public void setParamTypes(List<String> types) { this.paramTypes = types; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Symbol{name='%s', type='%s', kind=%s, scope=%d", 
                                  name, type, kind, scope));
            if (kind == Kind.ARRAY) {
                sb.append(", size=").append(arraySize);
            }
            if (kind == Kind.FUNCTION) {
                sb.append(", params=").append(paramTypes);
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    private List<Map<String, Symbol>> scopes;
    private int currentScope;
    
    public SymbolTable() {
        scopes = new ArrayList<>();
        scopes.add(new HashMap<>()); // escopo global
        currentScope = 0;
        
        addBuiltInFunctions();
    }
    
    private void addBuiltInFunctions() {
        // input() : int
        Symbol input = new Symbol("input", "int", Symbol.Kind.FUNCTION, 0);
        input.setParamTypes(new ArrayList<>());
        scopes.get(0).put("input", input);
        
        // output(int x) : void
        Symbol output = new Symbol("output", "void", Symbol.Kind.FUNCTION, 0);
        output.setParamTypes(Arrays.asList("int"));
        scopes.get(0).put("output", output);
    }
    
    public void enterScope() {
        currentScope++;
        if (currentScope >= scopes.size()) {
            scopes.add(new HashMap<>());
        } else {
            scopes.get(currentScope).clear();
        }
    }
    
    public void exitScope() {
        if (currentScope > 0) {
            scopes.get(currentScope).clear();
            currentScope--;
        }
    }
    
    public boolean insert(Symbol symbol) {
        Map<String, Symbol> currentScopeTable = scopes.get(currentScope);
        
        if (currentScopeTable.containsKey(symbol.getName())) {
            return false; // já existe no escopo atual
        }
        
        currentScopeTable.put(symbol.getName(), symbol);
        return true;
    }
    
    public Symbol lookup(String name) {
        //busca do escopo atual até o global
        for (int i = currentScope; i >= 0; i--) {
            Symbol symbol = scopes.get(i).get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }
    
    public Symbol lookupCurrentScope(String name) {
        return scopes.get(currentScope).get(name);
    }
    
    public int getCurrentScope() {
        return currentScope;
    }
    
    public void printTable() {
        System.out.println("\n=== Tabela de Símbolos ===");
        for (int i = 0; i <= currentScope; i++) {
            System.out.println("\nEscopo " + i + ":");
            Map<String, Symbol> scope = scopes.get(i);
            for (Symbol symbol : scope.values()) {
                System.out.println("  " + symbol);
            }
        }
    }
}