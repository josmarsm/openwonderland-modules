/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.programmingdemo.client.script.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;

/**
 * Cache classes we need for the classloader
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ClassScannerCache {

    private final Set<String> scannedJars;
    private final Map<String, List<JavaFileObject>> classList;

    public ClassScannerCache() {
        scannedJars = new TreeSet<String>();
        classList = new HashMap<String, List<JavaFileObject>>();
    }

    public void scanJar(URL jarURL) throws IOException {
        // check if we have already scanned this jar
        if (scannedJars.contains(jarURL.toExternalForm())) {
            return;
        }

        // open a connection to the jar file
        JarFile jar = ((JarURLConnection) jarURL.openConnection()).getJarFile();

        // if we haven't, go ahead and scan it
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = entries.nextElement();

            // determine the package name
            int lastSlash = je.getName().lastIndexOf("/");
            if (lastSlash == -1) {
                continue;
            }
            String pkg = je.getName().substring(0, lastSlash);

            if (je.getName().endsWith(".class")) {
                // add it to the class list
                List<JavaFileObject> pkgList = classList.get(pkg);
                if (pkgList == null) {
                    pkgList = new ArrayList<JavaFileObject>();
                    classList.put(pkg, pkgList);
                }

                pkgList.add(getJavaFileObject(je, jarURL));
            }
        }
    }

    public void getJavaFileObjects(String packageName, List<JavaFileObject> result) {
        List<JavaFileObject> pkgList = classList.get(packageName);
        if (pkgList != null) {
            result.addAll(pkgList);
        }
    }

    private JavaFileObject getJavaFileObject(JarEntry je, URL jarURL)
            throws IOException {
        try {
            URL dataURL = new URL(jarURL, "/" + je.getName());

            // convert to a class name
            String binaryName = je.getName().replaceAll("/", ".");
            binaryName = binaryName.replaceAll(".class$", "");
            URI nameURI = new URI(null, null, binaryName, null);

            return new JarJavaFileObject(nameURI, dataURL, Kind.CLASS);
        } catch (URISyntaxException use) {
            throw new IOException(use);
        }
    }

    static class JarJavaFileObject extends SimpleJavaFileObject {
        private URL dataURL;

        public JarJavaFileObject(URI nameURI, URL dataURL,
                                 JavaFileObject.Kind kind)
        {
            super (nameURI, kind);

            this.dataURL = dataURL;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return dataURL.openStream();
        }
    }
}
