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

//    private static GroupID invalidCellID = new CellID(Long.MIN_VALUE);

    private static long firstGroupID = 1;

//    private static long nextGroupID = firstGroupID;

    public static GroupID globalGroup = null;

//    public static long GLOBAL_GROUP_ID = 0;

    /**
     * Creates a new instance of GroupID. 
     */
    @InternalAPI
    public GroupID(long id) {
        this.id = id;
    }

    /**
     *
     * @return The groupID representin the global chat channel.
     */
    public static GroupID getGlobalGroupID() {
        if(globalGroup==null) {
            globalGroup = new GroupID(0);
        }

        return globalGroup;
    }

//    public static GroupID getNewGroupID() {
//        return new GroupID(nextGroupID++);
//    }

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

    /**
     * Get the first cell ID that should be assigned to cells
     * @return
     */
    public static long getFirstGroupID() {
        return firstGroupID;
    }

    /**
     * Returns a cellID that represents an invalid cell
     * @return
     */
//    public static GroupID getInvalidGroupID() {
//        return invalidCellID;
//    }
}
