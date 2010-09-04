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

package org.jdesktop.wonderland.modules.sitting.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for sitting cell component
 *
 * @author Morris Ford
 */
public class SittingCellComponentClientState extends CellComponentClientState {

    private float heading = 0.1f;
    private float offset = 0.1f;
    private String mouse = "Left Mouse";
    private boolean mouseEnable = false;

    /** Default constructor */
    public SittingCellComponentClientState() {
    }

    public float getHeading()
        {
        return heading;
        }

    public void setHeading(float Heading)
        {
        heading = Heading;
        }

    public float getOffset()
        {
        return offset;
        }

    public void setOffset(float Offset)
        {
        offset = Offset;
        }

    public boolean getMouseEnable()
        {
        return mouseEnable;
        }

    public void setMouseEnable(boolean enable)
        {
        this.mouseEnable = enable;
        }

    public String getMouse()
        {
        return mouse;
        }

    public void setMouse(String Mouse)
        {
        this.mouse = Mouse;
        }
}
