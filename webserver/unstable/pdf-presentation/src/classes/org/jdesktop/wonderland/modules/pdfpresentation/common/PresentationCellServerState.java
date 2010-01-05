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

package org.jdesktop.wonderland.modules.pdfpresentation.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
@XmlRootElement(name="presentation-cell")
@ServerState
public class PresentationCellServerState extends CellServerState {

    @XmlElement(name="cur-slide")
    private int curSlide;

    @XmlElement(name="pdfURI")
    private String pdfURI = null;

    @XmlElement(name="creator-name")
    private String creatorName;

    @XmlElement(name="num-pages")
    private int numPages;

    @XmlElement(name="layout")
    private PresentationLayout layout;

    
    public PresentationCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.pdfpresentation.server.PresentationCellMO";
    }

    @XmlTransient public int getCurSlide() { return this.curSlide; }
    public void setCurslide(int curSlide) {
        this.curSlide = curSlide;
    }

        @XmlTransient public String getSourceURI() { return this.pdfURI; }
    public void setSourceURI(String uri) {
        this.pdfURI = uri;
    }

    @XmlTransient public PresentationLayout getLayout() { return this.layout; }
    public void setLayout(PresentationLayout layout) {
        this.layout = layout;
    }

    @XmlTransient public String getCreatorName() { return this.creatorName; }
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @XmlTransient public int getNumPages() { return this.numPages; }
    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }
}
