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
package org.jdesktop.wonderland.modules.layout.api.common;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An interface that objects like cells can implement to indicate that
 * they return a custom type of layout participant.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public interface LayoutParticipantProvider {
    /**
     * Get the configured layout participant.
     * @return the configured layout participant.
     */
    public LayoutParticipant getLayoutParticipant();
}
