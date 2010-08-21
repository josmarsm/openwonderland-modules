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


package org.jdesktop.wonderland.modules.audiorecorder.common;

import java.io.Serializable;
import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Bernard Horan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( namespace="audiorecorder" )
public class Tape implements Serializable, Comparable<Tape> {

    private String tapeName;
    private boolean isFresh;
    private String url;

    public Tape() {
        isFresh = true;
    }
    
    public Tape(String filename) {
        this();
        this.tapeName = filename;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setUsed() {
        isFresh = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[" + getClass().getSimpleName() +"]");
        sb.append(" tapeName=");
        sb.append(tapeName);
        sb.append(" isFresh=");
        sb.append(isFresh);
        sb.append(" url=");
        sb.append(url);
        return sb.toString();
    }

    public boolean isFresh() {
        return isFresh;
    }

    public int compareTo(Tape t) {
        return tapeName.compareToIgnoreCase(t.tapeName);
    }
    
    public String getTapeName() {
        return tapeName;
    }

    public String getURL() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (this.tapeName != null ? this.tapeName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        //I am the same as some other tape iff my name is the same, my freshness is the same and my url is the same
        if (o instanceof Tape) {
            boolean sameName = ((Tape)o).tapeName.equals(tapeName);
            boolean sameFreshness = ((Tape)o).isFresh == isFresh;
            String otherURL = ((Tape)o).url;
            boolean sameURL = false;
            if (otherURL != null) {
                sameURL = ((Tape)o).url.equals(url);
            } else {
                sameURL = url == null;
            }
            return sameName && sameFreshness && sameURL;
        }
        return false;
    }
}
