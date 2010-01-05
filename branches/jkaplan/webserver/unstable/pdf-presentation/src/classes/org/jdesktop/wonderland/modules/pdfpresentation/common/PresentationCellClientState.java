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

import java.util.List;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

public class PresentationCellClientState extends CellClientState {

    private String pdfURI;
    private String creatorName;

    private PresentationLayout layout;

    public PresentationCellClientState() {
        
    }

    public String getPdfURI() {
        return pdfURI;
    }

    public void setPdfURI(String pdfURI) {
        this.pdfURI = pdfURI;
    }

    public PresentationLayout getLayout() {
        return layout;
    }

    public void setLayout(PresentationLayout layout) {
        this.layout = layout;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}