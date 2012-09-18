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
package org.jdesktop.wonderland.modules.connectionsample.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A CellMO for the colored cube example.  The CellMO does very little -- it is
 * mainly a container for the SharedStateComponentMO, which manages the state
 * of the component.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */

// declare a dependecy on the SharedStateComponentMO.  This will cause the
// component to be automatically included in the client cell as well.
@DependsOnCellComponentMO(SharedStateComponentMO.class)
public class ColoredCubeCellMO extends CellMO {
    /**
     * Default constructor, used when cell is created programatically
     */
    public ColoredCubeCellMO() {
        // set up the bounds and transform for this cell.  Necessary since
        // the cell is being created programatically.
        super (new BoundingBox(new Vector3f(0f, 0f, 0f), 2f, 2f, 2f),
               new CellTransform(new Quaternion(), new Vector3f(0f, 0f, 0f)));
    }

    /** Return the classname of the client cell */
    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.connectionsample.client.ColoredCubeCell";
    }
}
