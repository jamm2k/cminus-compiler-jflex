# Compilador C- em Java

## Descrição
Compilador para a linguagem C- desenvolvido como projeto acadêmico.

### Características da Linguagem C-
- Variáveis inteiras e arrays
- Funções com parâmetros
- Estruturas de controle: if, while
- Operações aritméticas e relacionais
- Entrada/saída: input() e output()

## Estrutura do Projeto
```
src/
├── lexer/       # Análise léxica (JFlex)
├── parser/      # Análise sintática
├── semantic/    # Análise semântica
└── codegen/     # Geração de código
```

## Como Compilar

### Pré-requisitos
- Java JDK 17+
- JFlex 1.9.1

### Passos
1. Gerar o lexer:
```bash
jflex -d src/lexer src/lexer/CMinus.flex
```

2. Compilar o projeto:
```bash
javac -d bin src/**/*.java
```

3. Executar:
```bash
java -cp bin Main programa.cm
```

## Gramática C- (Simplificada)

```
programa → declaracao-lista
declaracao-lista → declaracao-lista declaracao | declaracao
declaracao → var-declaracao | fun-declaracao

var-declaracao → tipo ID ; | tipo ID [ NUM ] ;
fun-declaracao → tipo ID ( params ) composto-decl

tipo → int | void
params → param-lista | void
param-lista → param-lista , param | param
param → tipo ID | tipo ID [ ]

composto-decl → { local-declaracoes statement-lista }
local-declaracoes → local-declaracoes var-declaracao | vazio
statement-lista → statement-lista statement | vazio

statement → expressao-decl | composto-decl | selecao-decl
          | iteracao-decl | retorno-decl

expressao-decl → expressao ; | ;
selecao-decl → if ( expressao ) statement | if ( expressao ) statement else statement
iteracao-decl → while ( expressao ) statement
retorno-decl → return ; | return expressao ;

expressao → var = expressao | simples-expressao
var → ID | ID [ expressao ]
simples-expressao → soma-expressao relacional soma-expressao | soma-expressao
relacional → <= | < | > | >= | == | !=
soma-expressao → soma-expressao soma termo | termo
soma → + | -
termo → termo mult fator | fator
mult → * | /
fator → ( expressao ) | var | chamada | NUM
chamada → ID ( args )
args → arg-lista | vazio
arg-lista → arg-lista , expressao | expressao
```

## Exemplos de Programas

### Exemplo 1: Fatorial
```c
int fat(int n) {
    if (n <= 1)
        return 1;
    else
        return n * fat(n - 1);
}

void main(void) {
    int x;
    x = input();
    output(fat(x));
}
```

### Exemplo 2: Soma de Array
```c
int soma(int arr[], int n) {
    int i;
    int s;
    s = 0;
    i = 0;
    while (i < n) {
        s = s + arr[i];
        i = i + 1;
    }
    return s;
}

void main(void) {
    int nums[5];
    int i;
    i = 0;
    while (i < 5) {
        nums[i] = input();
        i = i + 1;
    }
    output(soma(nums, 5));
}
```

## Autor
José Arthur de Mello

## Referências
- Compilers: Principles, Techniques, and Tools (Dragon Book)
- JFlex Documentation: https://jflex.de/
- ChatGPT (lol)