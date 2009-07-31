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

import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;

/**
 * Interface to listen for transformation changes.
 * @author kevin
 */
public interface TransformationMessageListener {
    /**
     * Called when a listened-to transformation has been updated.
     * @param message The updated transformation
     */
    public void transformationMessageChanged(TransformationMessage message);
}
