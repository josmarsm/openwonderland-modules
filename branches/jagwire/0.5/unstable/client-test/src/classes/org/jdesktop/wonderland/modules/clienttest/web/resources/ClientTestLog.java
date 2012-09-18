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
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Log of a client test
 */
@XmlRootElement(name="client-test-log")
public class ClientTestLog {
    private final String id;
    private final String creator;
    private final Date timeStamp;
    private final InputStream content;
    
    public ClientTestLog() {
        this (null, null, null, null);
    }
    
    public ClientTestLog(String id, String creator, 
                         Date timeStamp, InputStream content) 
    {
        this.id = id;
        this.creator = creator;
        this.timeStamp = timeStamp;
        this.content = content;
    }
    
    public ClientTestLog(String id, String creator, File content) throws IOException {
        this (id, creator, new Date(content.lastModified()), 
              new FileInputStream(content));
    }

    @XmlElement
    public String getID() {
        return id;
    }
    
    @XmlElement
    public String getCreator() {
        return creator;
    }
    
    @XmlElement
    public Date getTimeStamp() {
        return timeStamp;
    }
    
    @XmlTransient
    public InputStream getContent() {
        return content;
    }
}
