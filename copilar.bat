@echo off
setlocal enabledelayedexpansion

:: Definir cores para o terminal (apenas para Windows 10+)
set "GREEN=[32m"
set "RED=[31m"
set "YELLOW=[33m"
set "BLUE=[34m"
set "RESET=[0m"

:: Título da janela
title AutoFarm Premium - Compilador Customizado

echo.
echo  [1m%BLUE%========================================%RESET%
echo  [1m%BLUE%      AUTOFARM PREMIUM COMPILER       %RESET%
echo  [1m%BLUE%========================================%RESET%
echo.

:: Solicitar o nome do arquivo ao usuário
echo %YELLOW%Digite o nome desejado para o arquivo JAR%RESET%
echo %YELLOW%(O sufixo .ext.jar sera adicionado automaticamente)%RESET%
set /p "USER_JAR_NAME=Nome (pressione Enter para 'L2jPremiumAutofarm'): "

:: Definir nome padrão caso o usuário não digite nada
if "!USER_JAR_NAME!"=="" (
    set "USER_JAR_NAME=L2jPremiumAutofarm"
)

set "OUTPUT_JAR=!USER_JAR_NAME!.ext.jar"

echo.
echo %BLUE%Arquivo de saida sera: !OUTPUT_JAR!%RESET%
echo.

:: Verificar se o Java está no PATH
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%Erro: 'javac' nao encontrado. Certifique-se de que o JDK esta instalado e no PATH.%RESET%
    pause
    exit /b 1
)

:: Criar diretório bin se não existir
if not exist bin (
    echo %YELLOW%Criando diretorio bin...%RESET%
    mkdir bin
)

:: Configurações
set "LIBS=libs/*"
set "SOURCES=*.java"

echo %YELLOW%Limpando arquivos antigos...%RESET%
del /q bin\*.class 2>nul

echo %YELLOW%Compilando arquivos fonte...%RESET%
:: Usamos UTF-8 para garantir que caracteres especiais nos logs e strings sejam mantidos
javac -encoding UTF-8 -d bin -cp "%LIBS%;." %SOURCES%

if %errorlevel% neq 0 (
    echo.
    echo %RED%Erro: A compilacao falhou! Verifique os erros acima.%RESET%
    pause
    exit /b %errorlevel%
)

echo %GREEN%Compilacao concluida com sucesso!%RESET%
echo.

echo %YELLOW%Gerando arquivo JAR: !OUTPUT_JAR!...%RESET%
jar cvf "!OUTPUT_JAR!" -C bin .

if %errorlevel% neq 0 (
    echo.
    echo %RED%Erro: Falha ao gerar o JAR!%RESET%
    pause
    exit /b %errorlevel%
)

echo.
echo %GREEN%========================================%RESET%
echo %GREEN%      BUILD COMPLETED SUCCESSFULLY!   %RESET%
echo %GREEN%      Gerado: !OUTPUT_JAR!            %RESET%
echo %GREEN%========================================%RESET%
echo.

pause
