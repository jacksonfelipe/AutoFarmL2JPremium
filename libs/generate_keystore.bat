@echo off
REM Gera um keystore para HTTPS (Java)
REM Altere "changeit" para uma senha segura
REM Altere "l2jpremium" para o nome desejado

set KEYSTORE=keystore.jks
set STOREPASS=changeit
set DNAME="CN=l2jpremium.com, OU=Premium, O=L2JPremium, L=City, S=State, C=BR"

keytool -genkeypair -alias l2jpremium -keyalg RSA -keysize 2048 -keystore %KEYSTORE% -storepass %STOREPASS% -validity 365 -dname %DNAME%

echo Keystore gerado: %KEYSTORE%
echo Senha: %STOREPASS%
pause
