/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */


package org.jdesktop.wonderland.modules.eventplayer.common;

import java.io.Serializable;

public class Tape implements Serializable, Comparable {

    private String tapeName;
    private boolean isFresh;
    
    public Tape(String filename) {
        isFresh = true;
        this.tapeName = filename;
    }

    public void setUsed() {
        isFresh = false;
    }

    @Override
    public String toString() {
        return tapeName;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public int compareTo(Object o) {
        Tape t = (Tape) o;
        return tapeName.compareToIgnoreCase(t.tapeName);
    }
    
    public String getTapeName() {
        return tapeName;
    }
}