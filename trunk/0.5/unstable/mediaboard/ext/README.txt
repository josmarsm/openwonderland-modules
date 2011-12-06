Contained in this directory are two files: civil.dll and lti-civil.jar

*************************************
*
*
* Installation Notes
*
*
*************************************

* civil.dll - this needs to be put in wonderland/core/ext/win32

* lti-civil.jar - this needs to be put in wonderland/core/ext/common

* After both files are placed in the correct wonderland directories, it's time to edit core/build-tools/build-scripts/classpath.xml.
** Add the following line at the end of the <!-- jme --> section but before the <!-- jogl --> section:
<path location="${core.common.dir}/lti-civil.jar"/>

After both files are placed in the correct wonderland directories, the wonderland codebase needs to be rebuilt. After the code has been rebuilt. The server needs to be restarted.

