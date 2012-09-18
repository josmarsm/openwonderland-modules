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
package org.jdesktop.wonderland.modules.proximitytest.common;

import com.jme.bounding.BoundingVolume;
import java.util.List;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * Cell client state for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProximityCellClientState extends CellClientState {
    private List<BoundingVolume> clientBounds;
    private List<BoundingVolume> serverBounds;

    /** Default constructor */
    public ProximityCellClientState() {
    }

    public List<BoundingVolume> getClientBounds() {
        return clientBounds;
    }

    public void setClientBounds(List<BoundingVolume> clientBounds) {
        this.clientBounds = clientBounds;
    }

    public List<BoundingVolume> getServerBounds() {
        return serverBounds;
    }

    public void setServerBounds(List<BoundingVolume> serverBounds) {
        this.serverBounds = serverBounds;
    }
}
