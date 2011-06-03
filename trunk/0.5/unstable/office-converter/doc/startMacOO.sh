#!/bin/sh
if [ -d /tmp/OfficeConverter ]
then
echo deleting /tmp/OfficeConverter
rm -r /tmp/OfficeConverter
fi
mkdir /tmp/OfficeConverter
/Applications/OpenOffice.org.app/Contents/MacOS/soffice -env:UserInstallation=file:///tmp/OfficeConverter -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
