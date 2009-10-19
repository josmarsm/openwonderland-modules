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
package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * MetadataID provides a unique id for metadata objects.
 * Implementation essentially a copy of CellID
 *
 * @author mabonner
 */
public class MetadataID implements Serializable {

    private long id;
    private transient String str=null;

    private static MetadataID invalidMetadataID = new MetadataID(Long.MIN_VALUE);

    private static long firstCellID = 0;

    /**
     * Creates a new instance of MetadataID.
     */
    @InternalAPI
    public MetadataID(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetadataID)
            if (((MetadataID) obj).id==id)
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
     * Returns a cellID that represents an invalid cell
     * @return
     */
    public static MetadataID getInvalidMetadataID() {
        return invalidMetadataID;
    }
}
