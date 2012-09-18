@ECHO OFF
IF EXIST %TEMP%\OfficeConverter (
ECHO deleting %TEMP%\OfficeConverter\
RMDIR %TEMP%\OfficeConverter\ /S /Q
)
ECHO creating %TEMP%\OfficeConverter
MD %TEMP%\OfficeConverter
REM Converting DOS path to URL-compatible path
REM This works on Windows7, other windows platforms may vary
FOR /f "tokens=1,2,3,4,5,6 delims=\" %%i in ('echo %TEMP%') do SET TEMP_FOLDER=%%i/%%j/%%k/%%l/%%m/%%n
ECHO TEMP_FOLDER: %TEMP_FOLDER%
ECHO Starting OpenOffice
"C:\Program Files\OpenOffice.org 3\program\soffice.exe" -env:UserInstallation=file:///%TEMP_FOLDER%/OfficeConverter -nofirststartwizard -headless -accept=socket,host=localhost,port=8100;urp;StarOffice.ServiceManager
