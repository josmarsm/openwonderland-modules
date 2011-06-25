@ECHO OFF
IF EXIST %TEMP%\OfficeConverter (
ECHO deleting %TEMP%\OfficeConverter\
RMDIR %TEMP%\OfficeConverter\ /S /Q
)
ECHO creating %TEMP%\OfficeConverter
MD %TEMP%\OfficeConverter
ECHO Starting OpenOffice
"C:\Program Files\OpenOffice.org 3\program\soffice.exe" -env:UserInstallation=file:///%TEMP%/OfficeConverter -nofirststartwizard -headless -accept=socket,host=localhost,port=8100;urp;StarOffice.ServiceManager
