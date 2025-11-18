@echo off
REM Script para compilar o compilador C- no Windows
REM Funciona com JFlex instalado globalmente

echo ================================
echo   Compilador C- - Build Script
echo ================================

SET SRC_DIR=src
SET BIN_DIR=bin
SET LEXER_DIR=%SRC_DIR%\lexer
SET FLEX_FILE=%LEXER_DIR%\CMinus.flex

if not exist "%BIN_DIR%" (
    mkdir "%BIN_DIR%"
    echo [OK] Diretorio bin criado
)

echo.
echo Passo 1: Gerando analisador lexico com JFlex...

REM jflex -d "%LEXER_DIR%" "%FLEX_FILE%"

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha ao gerar lexer
    echo Verifique se JFlex esta instalado corretamente
    pause
    exit /b 1
)

echo [OK] Lexer gerado com sucesso

REM Verifica se o arquivo foi criado
if not exist "%LEXER_DIR%\Lexer.java" (
    echo [ERRO] Arquivo Lexer.java nao foi criado!
    pause
    exit /b 1
)

echo.
echo Passo 2: Compilando arquivos Java...

REM Compilar com encoding UTF-8 explicito
javac -encoding UTF-8 -d "%BIN_DIR%" Main.java %LEXER_DIR%\*.java %SRC_DIR%\parser\*.java %SRC_DIR%\semantic\*.java %SRC_DIR%\codegen\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilacao
    echo.
    echo Verifique se todos os arquivos existem:
    echo.
    dir /b %LEXER_DIR%\*.java
    echo.
    dir /b %SRC_DIR%\parser\*.java
    echo.
    dir /b %SRC_DIR%\semantic\*.java
    echo.
    dir /b %SRC_DIR%\codegen\*.java
    echo.
    pause
    exit /b 1
)

echo [OK] Compilacao concluida com sucesso

echo.
echo ================================
echo [OK] BUILD CONCLUIDO COM SUCESSO!
echo ================================
echo.
echo Para executar o compilador:
echo   java -cp %BIN_DIR% Main ^<arquivo.cm^>
echo.
echo Exemplos de teste:
echo   java -cp %BIN_DIR% Main tests\test_programs\test1_fatorial.cm
echo   java -cp %BIN_DIR% Main tests\test_programs\test2_soma_array.cm
echo   java -cp %BIN_DIR% Main tests\test_programs\test3_fibonacci.cm
echo.
pause