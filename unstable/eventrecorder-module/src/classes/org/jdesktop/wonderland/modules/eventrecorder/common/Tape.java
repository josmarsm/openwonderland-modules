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



package org.jdesktop.wonderland.modules.eventrecorder.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Bernard Horan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( namespace="eventrecorder" )
public class Tape implements Serializable, Comparable<Tape> {

    private String tapeName;
    private boolean isFresh;
    
    public Tape() {
        isFresh = true;
    }

    public Tape(String filename) {
        this();
        this.tapeName = filename;
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

    /**
     * Return true if the name of the tape is equal and
     * the freshness is identical
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tape)) {
            return false;
        } else {
            Tape tape = (Tape) o;
            if (tape.isFresh() != isFresh) {
                return false;
            }
            if (!tape.getTapeName().equals(tapeName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.tapeName != null ? this.tapeName.hashCode() : 0);
        hash = 59 * hash + (this.isFresh ? 1 : 0);
        return hash;
    }

    
}
