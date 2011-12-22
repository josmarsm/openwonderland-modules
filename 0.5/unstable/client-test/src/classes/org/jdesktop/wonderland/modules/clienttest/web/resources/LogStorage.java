/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.clienttest.web.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 *
 * @author jkaplan
 */
public enum LogStorage {
    INSTANCE;
   
    private static final File BASE_DIR = new File(RunUtil.getRunDir(), 
                                                  "client-test-reports");
    private static final String CREATOR_PROP = "Creator";
   
    public ClientTestLog store(String creator, InputStream log) throws IOException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        File dir = new File(BASE_DIR, format.format(new Date()));
        if (dir.exists()) {
            throw new IllegalStateException("Directory exists: " + dir);
        }
        dir.mkdirs();
       
        // store properties
        Properties props = new Properties();
        props.put(CREATOR_PROP, creator);
        File creatorFile = new File(dir, "properties");
        props.store(new FileOutputStream(creatorFile), "");
   
        // store data
        File dataFile = new File(dir, "log.txt");
        RunUtil.writeToFile(log, dataFile);
        
        return new ClientTestLog(dir.getName(), creator, dataFile);
    }
   
   public List<ClientTestLog> list() throws IOException {
       List<ClientTestLog> out = new ArrayList<ClientTestLog>();
       
       for (File childDir : BASE_DIR.listFiles()) {
           ClientTestLog log = get(childDir);
           if (log != null) {
               out.add(log);
           }
       }
       
       return out;
   }
   
   public ClientTestLog get(String id) throws IOException {
       return get(new File(BASE_DIR, id));
   }
   
   protected ClientTestLog get(File dir) throws IOException {
       if (!dir.isDirectory()) {
           return null;
       }

       File propsFile = new File(dir, "properties");
       if (!propsFile.exists()) {
           return null;
       }

       Properties props = new Properties();
       props.load(new FileInputStream(propsFile));
       String creator = props.getProperty(CREATOR_PROP);
       if (creator == null) {
           return null;
       }

       File dataFile = new File(dir, "log.txt");
       if (!dataFile.exists()) {
           return null;
       }

       return new ClientTestLog(dir.getName(), creator, dataFile);
   }
}
