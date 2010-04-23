/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import java.io.Serializable;

/**
 * Simple class to categorize messages from CMU players to clients.
 * @author kevin
 */
public abstract class CMUClientMessage implements Serializable {

    /**
     * Get the String representation of this message, containing
     * debugging information.
     * @return Message as String
     */
    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
