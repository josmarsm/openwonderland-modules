/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.sitting.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for sitting cell component
 *
 * @author Morris Ford
 */
@XmlRootElement(name="sitting-cell-component")
@ServerState
public class SittingCellComponentServerState extends CellComponentServerState
    {

    @XmlElement(name="mouse-enable")
    private boolean mouseEnable;
    @XmlElement(name="heading")
    private float heading;
    @XmlElement(name="offset")
    private float offset;
    @XmlElement(name="mouse")
    private String mouse = "Left Mouse";

    /** Default constructor */
    public SittingCellComponentServerState()
        {
        }

    @Override
    public String getServerComponentClassName()
        {
        return "org.jdesktop.wonderland.modules.sitting.server.SittingCellComponentMO";
        }

    @XmlTransient public float getHeading()
        {
        return heading;
        }

    public void setHeading(float Heading)
        {
        heading = Heading;
        }

    @XmlTransient public float getOffset()
        {
        return offset;
        }

    public void setOffset(float Offset)
        {
        offset = Offset;
        }

    @XmlTransient public boolean getMouseEnable()
        {
        return mouseEnable;
        }

    public void setMouseEnable(boolean enable)
        {
        this.mouseEnable = enable;
        }

    @XmlTransient public String getMouse()
        {
        return mouse;
        }

    public void setMouse(String Mouse)
        {
        this.mouse = Mouse;
        }
    }
