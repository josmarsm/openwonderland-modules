#!/bin/sh
if [ -d /tmp/OfficeConverter ]
then
echo deleting /tmp/OfficeConverter
rm -r /tmp/OfficeConverter
fi
echo creating /tmp/OfficeConverter
mkdir /tmp/OfficeConverter
echo Starting OpenOffice
/usr/lib/openoffice/program/soffice.bin -env:UserInstallation=file:///tmp/OfficeConverter -headless -nofirststartwizard -accept="socket,host=localhost,port=8100;urp;StarOffice.Service"