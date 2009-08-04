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
 * ID for a CMU visual node.
 * @author kevin
 */
public class NodeID implements Serializable {

    private final long id;

    public NodeID(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof NodeID) {
            if (((NodeID)other).id == this.id) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
