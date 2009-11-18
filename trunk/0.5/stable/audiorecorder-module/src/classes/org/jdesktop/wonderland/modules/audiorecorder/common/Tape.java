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
    private URL url;

    public Tape() {
        isFresh = true;
    }
    
    public Tape(String filename) {
        this();
        this.tapeName = filename;
    }

    public void setURL(URL url) {
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

    public URL getURL() {
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
        if (o instanceof Tape) {
            return ((Tape)o).tapeName.equals(tapeName);
        }
        return false;
    }
}
