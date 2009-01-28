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
package org.jdesktop.wonderland.modules.whiteboard.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * The WFS server state class for WhiteboardCellMO.
 * 
 * @author deronj
 * @author nsimpson
 */
@XmlRootElement(name = "whiteboard-cell-svg")
@ServerState
public class WhiteboardSVGCellServerState extends CellServerState implements Serializable {

    /** The user's preferred width of the whiteboard window. */
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 1024;
    /** The user's preferred height of the whiteboard window. */
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 768;
    /** The X pixel scale of the whiteboard window. */
    @XmlElement(name = "pixelScaleX")
    public float pixelScaleX = 0.01f;
    /** The Y pixel scale of the whiteboard window. */
    @XmlElement(name = "pixelScaleY")
    public float pixelScaleY = 0.01f;
    @XmlElement(name = "svgDocumentXML")
    public String svgDocumentXML = "";

    /** Default constructor */
    public WhiteboardSVGCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.whiteboard.server.WhiteboardCellMO";
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    @XmlTransient
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    @XmlTransient
    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPixelScaleX(float pixelScaleX) {
        this.pixelScaleX = pixelScaleX;
    }

    @XmlTransient
    public float getPixelScaleX() {
        return pixelScaleX;
    }

    public void setPixelScaleY(float pixelScaleY) {
        this.pixelScaleY = pixelScaleY;
    }

    @XmlTransient
    public float getPixelScaleY() {
        return pixelScaleY;
    }

    public void setSVGDocumentXML(String svgDocumentXML) {
        this.svgDocumentXML = svgDocumentXML;
    }

    @XmlTransient
    public String getSVGDocumentXML() {
        return svgDocumentXML;
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The server state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [WhiteboardSVGCellServerState]: " +
                "preferredWidth=" + preferredWidth + "," +
                "preferredHeight=" + preferredHeight + "," +
                "pixelScaleX=" + pixelScaleX + "," +
                "pixelScaleY=" + pixelScaleY + ", " +
                "svgDocumentXML=" + svgDocumentXML;
    }
}
