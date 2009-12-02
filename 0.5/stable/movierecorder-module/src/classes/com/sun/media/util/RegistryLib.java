/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package com.sun.media.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A version of the JMF RegistryLib file that reads a jmf.properties file
 * in the root directory of the JMF jar file.  This replaces the RegistryLib
 * in JMF.jar that reads static data.
 * @author jkaplan
 */
public class RegistryLib {
    /** 
     * Read the data from JMF.properties
     */
    public static byte[] getData() throws IOException {
        //System.out.println("RegistryLib.getData()");
        
        // get jmf.properties from the root of the current jar file
        InputStream is = RegistryLib.class.getResourceAsStream("/jmf.properties");
        BufferedInputStream in = new BufferedInputStream(is);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        byte[] buf = new byte[4096];
        int count;
        while((count = in.read(buf)) != -1) {
            out.write(buf, 0, count);
        }
        
        out.close();
        byte[] res = out.toByteArray();
        
        //System.out.println("RegistryLib.getData() returning " + res.length + " bytes.");
        
        return res;
    }
}
