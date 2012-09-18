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

package org.jdesktop.wonderland.modules.pdfpresentation.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformCellChangeMessage extends CellMessage {

    private float platformWidth;
    private float platformHeight;

    public MovingPlatformCellChangeMessage() {
    }

    public MovingPlatformCellChangeMessage(float width, float height) {
        platformWidth = width;
        platformHeight = height;
    }

    public float getPlatformHeight() {
        return platformHeight;
    }

    public void setPlatformHeight(float platformHeight) {
        this.platformHeight = platformHeight;
    }

    public float getPlatformWidth() {
        return platformWidth;
    }

    public void setPlatformWidth(float platformWidth) {
        this.platformWidth = platformWidth;
    }

}
