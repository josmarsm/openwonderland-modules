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

package org.jdesktop.wonderland.modules.presentationbase.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationCellChangeMessage extends CellMessage {

    /**
     * +1 to go to next slide, -1 to go to previous slide.
     */
    private int slideIncrement;
    
    public int getSlideIncrement() {
        return slideIncrement;
    }

    public void setSlideIncrement(int slideIncrement) {
        this.slideIncrement = slideIncrement;
    }

    
}
