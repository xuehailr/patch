@echo off  
set patch_home=%cd%
set /p version=当前分支(如:1.5.255.1):
svn checkout https://172.16.49.100:8443/svn/jk/weidai/branches/bxloan-r%version%
cd bxloan-r%version%
svn update
call mvn clean package
cd bxloan-web\target
copy bxloan.war %patch_home%
cd %patch_home%
java -jar patch.jar %patch_home% %version%
echo 打包完成
pause