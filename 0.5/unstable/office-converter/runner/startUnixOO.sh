#!/bin/sh
if [ -d /tmp/OfficeConverter ]
then
echo deleting /tmp/OfficeConverter
rm -r /tmp/OfficeConverter
fi
echo creating /tmp/OfficeConverter
mkdir /tmp/OfficeConverter
echo Starting OpenOffice
soffice -env:UserInstallation=file:///tmp/OfficeConverter -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
