#!/bin/sh
if [ -d /tmp/OfficeConverter ]
then
echo deleting /tmp/OfficeConverter
rm -r /tmp/OfficeConverter
fi
echo creating /tmp/OfficeConverter
mkdir /tmp/OfficeConverter
echo Starting OpenOffice
/Applications/OpenOffice.org.app/Contents/MacOS/soffice -env:UserInstallation=file:///tmp/OfficeConverter -headless -nofirststartwizard -accept="socket,host=127.0.0.1,port=8100;urp;StarOffice.Service" 
