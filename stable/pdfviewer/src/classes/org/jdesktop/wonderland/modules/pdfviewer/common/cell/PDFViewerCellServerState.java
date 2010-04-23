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
package org.jdesktop.wonderland.modules.pdfviewer.common.cell;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for PDFViewerCellMO
 * 
 * @author nsimpson
 */
@XmlRootElement(name = "pdfviewer-cell")
@ServerState
public class PDFViewerCellServerState extends App2DCellServerState implements Serializable {

    // the URI of the PDF document
    @XmlElement(name = "documentURI")
    public String documentURI = "http://wonderlandvw.files.wordpress.com/2010/04/owl-extensiblevwtoolkit.pdf";
    // the current page (default to the first page)
    @XmlElement(name = "currentPage")
    public int currentPage = 1;
    // the preferred width of the PDF viewer (default to 4:3 aspect ratio)
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 640;
    // the preferred height of the PDF viewer
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 480;
    // whether to decorate the window with a frame
    @XmlElement(name = "decorated")
    public boolean decorated = true;

    public PDFViewerCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.pdfviewer.server.cell.PDFViewerCellMO";
    }

    public void setDocumentURI(String documentURI) {
        this.documentURI = documentURI;
    }

    @XmlTransient
    public String getDocumentURI() {
        return documentURI;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @XmlTransient
    public int getCurrentPage() {
        return currentPage;
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

    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    @XmlTransient
    public boolean getDecorated() {
        return decorated;
    }
}
