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

package org.jdesktop.wonderland.modules.cmu.player;

import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;

/**
 * Interface to listen for updates to a Node's transformation, visual properties,
 * etc.
 * @author kevin
 */
public interface NodeUpdateListener {
    /**
     * Called when a listened-to transformation has been updated.
     * @param message The updated transformation
     */
    public void modelUpdated(NodeUpdateMessage message);
}
