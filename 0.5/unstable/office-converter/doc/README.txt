CONFIGURING THE OPENOFFICE SERVER
---------------------------------

Once the office-converter module is installed in OpenWonderland, open the web administration
UI to the "Manage Server" page and do the following:

1. Click on the "edit" link next to "Server Components".
2. Select "Add Component" at the bottom of the screen
3. Enter the following values:

Component Name:  OpenOffice Server
Component Class: org.jdesktop.wonderland.modules.officeconverter.weblib.OpenOfficeRunner
Location:        Local

4. Click OK, then Save.

At this point, the OpenOffice server should be visible on the "Manage Server" page.
You can start and stop it using the controls on the page, and view the log
using the "log" link. 

CONFIGURING THE PATHS TO OPENOFFICE
-----------------------------------

This module assumes that the executables for OpenOffice are as described below. If your installation differs from these, please edit the appropriate files AFTER they are deployed. They will be in ~/.wonderland-server/0.5-dev/run/openoffice_server/run, or equivalent.

Windows:
Used in the DOS BATch file startWinOO.bat
"C:\Program Files\OpenOffice.org 3\program\soffice.exe"

Unix (Ubuntu)
Used in the shell script startUnixOO.sh
/usr/lib/openoffice/program/soffice.bin

Mac OS X
Used in the shell script startMacOO.sh
/Applications/OpenOffice.org.app/Contents/MacOS/soffice