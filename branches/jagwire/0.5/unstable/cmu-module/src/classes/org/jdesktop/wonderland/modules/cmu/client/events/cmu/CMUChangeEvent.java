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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;

/**
 * Event representing a change in a particular CMU scene (i.e. a particular
 * CMU cell).  In general these are changes that should be processed
 * client-side, and are not the result of permanent changes to the CMU scene
 * data - e.g. changes in the scene title, visibility of a particular visual
 * type, etc.
 * @author kevin
 */
public abstract class CMUChangeEvent {

    private CMUCell cell;

    /**
     * Standard constructor.
     * @param cell The CMU cell which has changed
     */
    public CMUChangeEvent(CMUCell cell) {
        setCell(cell);
    }

    /**
     * Get the cell which has changed.
     * @return The CMU cell which has changed
     */
    public CMUCell getCell() {
        return cell;
    }

    /**
     * Set the cell which has changed.
     * @param cell The CMU cell which has changed
     */
    public void setCell(CMUCell cell) {
        this.cell = cell;
    }

}
