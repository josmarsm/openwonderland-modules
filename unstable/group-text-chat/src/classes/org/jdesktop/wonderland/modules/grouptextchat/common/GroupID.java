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

package org.jdesktop.wonderland.modules.grouptextchat.common;

import java.io.Serializable;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * Wrapper for GroupIDs used in defining TextChat groups. Basically just wraps a
 * long for purposes of distinguishing groups.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class GroupID implements Serializable {

    private long id;

    private transient String str = null;

    private String label = "";

    public static final long FIRST_GROUP_ID = 1;

    public static final long GLOBAL_GROUP_ID = 0;

    public static final long INVALID_GROUP_ID = -1;

    /**
     * Creates a new instance of GroupID. 
     */
    @InternalAPI
    public GroupID(long id) {
        this.id = id;
    }


    public GroupID() {
        this.id = INVALID_GROUP_ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupID)
            if (((GroupID) obj).id==id)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public String toString() {
        if (str==null)
            str = Long.toString(id);

        return str;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
