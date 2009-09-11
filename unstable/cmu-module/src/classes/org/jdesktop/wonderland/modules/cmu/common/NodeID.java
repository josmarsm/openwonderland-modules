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
package org.jdesktop.wonderland.modules.cmu.common;

import java.io.Serializable;

/**
 * ID for a CMU visual node.  Unique IDs can be generated using generateNewID().
 * @author kevin
 */
public class NodeID implements Serializable {

    private static long numGenerated = 0;
    private final long id;

    /**
     * Generate a unique ID, in the sense that a different ID (based on the
     * equals() method) will be generated each time this method is called
     * in a single class loader.
     * @return A unique NodeID instance
     */
    public static synchronized NodeID generateNewID() {
        NodeID toReturn = new NodeID(numGenerated);
        numGenerated++;
        return toReturn;
    }

    /**
     * Constructor is private, since the type defining the ID is
     * implementation-specific.
     * @param id A long defining this NodeID
     */
    private NodeID(long id) {
        this.id = id;
    }

    /**
     * Copy constructor.
     * @param toCopy The NodeID to duplicate
     */
    public NodeID(NodeID toCopy) {
        this.id = toCopy.id;
    }

    /**
     * Compare this to another object.
     * @param obj The object to compare
     * @return True if the objects are the same identifier, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof NodeID) {
            if (((NodeID) obj).id == this.id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate a hash code for this NodeID.
     * @return Generated hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
