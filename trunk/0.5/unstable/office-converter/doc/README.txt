PLATFORM INSTALLATION NOTES
---------------------------
This module requires the installation of OpenOffice3.
OpenOffice3 may be downloaded from http://www.openoffice.org/.

Windows
-------
(These instructions are for Windows7, other platforms may vary).
Download and install the NON-JAVA version of OpenOffice3.
Make sure that the installation does NOT include the JRE
(otherwise it will likely replace the JDK in your path).
For example, download from:
http://download.services.openoffice.org/files/stable/3.3.0/OOo_3.3.0_Win_x86_install_en-US.exe

Ensure that the path for the OpenOffice executable matches that in the file startWinOO.bat, i.e.
"C:\Program Files\OpenOffice.org 3\program\soffice.exe"
If your installation differs from this either edit the batch file BEFORE compiling and deploying,
or edit the batch file after deployment. You will find it in
~/.wonderland-server/0.5-dev/run/openoffice_server/run, or equivalent.

Unix
----
(These instructions are for Ubuntu, other distributions may vary).
The OpenOffice3 package is probably already installed.
If the package is missing, follow the instructions that result from executing 'soffice' on the command line.

Ensure that the path for the OpenOffice executable matches that in the shell script startUnixOO.sh, i.e.
/usr/lib/openoffice/program/soffice.bin
If your installation differs from this either edit the batch shell script BEFORE compiling and deploying,
or edit the shell script after deployment. You will find it in
~/.wonderland-server/0.5-dev/run/openoffice_server/run, or equivalent.

Mac OS X
--------
Download and install OpenOffice3.

Ensure that the path for the OpenOffice executable matches that in the shell script startMacOO.sh, i.e.
/Applications/OpenOffice.org.app/Contents/MacOS/soffice
If your installation differs from this either edit the batch shell script BEFORE compiling and deploying,
or edit the shell script after deployment. You will find it in
~/.wonderland-server/0.5-dev/run/openoffice_server/run, or equivalent.


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

===================================
Bernard Horan, June 2011