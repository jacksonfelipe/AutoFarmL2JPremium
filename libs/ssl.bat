@echo off
REM Requer Python, Certbot, OpenSSL e Keytool no PATH

REM 1. Gerar certificado SSL com Certbot (Let’s Encrypt)
REM Substitua email e domínio abaixo
set EMAIL=seuemail@dominio.com
set DOMAIN=seusite.com

certbot certonly --standalone --non-interactive --agree-tos --email %EMAIL% -d %DOMAIN%
if errorlevel 1 (
    echo Erro ao gerar certificado SSL.
    pause
    exit /b
)

REM 2. Converter para PKCS12
set CERTPATH=C:\Certbot\live\%DOMAIN%
set P12PASS=changeit
set P12NAME=keystore.p12

openssl pkcs12 -export -out %P12NAME% -inkey %CERTPATH%\privkey.pem -in %CERTPATH%\cert.pem -certfile %CERTPATH%\chain.pem -name l2jpremium -password pass:%P12PASS%
if errorlevel 1 (
    echo Erro ao converter para PKCS12.
    pause
    exit /b
)

REM 3. Converter PKCS12 para JKS
set JKSNAME=keystore.jks

keytool -importkeystore -srckeystore %P12NAME% -srcstoretype PKCS12 -srcstorepass %P12PASS% -destkeystore %JKSNAME% -deststoretype JKS -deststorepass %P12PASS% -alias l2jpremium
if errorlevel 1 (
    echo Erro ao converter para JKS.
    pause
    exit /b
)

REM 4. Mover keystore para pasta do projeto
move /Y %JKSNAME% ..\libs\keystore.jks

echo Keystore gerado e instalado com sucesso!
pause