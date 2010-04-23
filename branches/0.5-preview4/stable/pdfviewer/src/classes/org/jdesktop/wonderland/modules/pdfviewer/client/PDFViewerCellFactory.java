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
package org.jdesktop.wonderland.modules.pdfviewer.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.modules.pdfviewer.common.cell.PDFViewerCellServerState;

/**
 * The cell factory for the PDF viewer
 * 
 * @author nsimpson
 */
@CellFactory
public class PDFViewerCellFactory implements CellFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/pdfviewer/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{"pdf"};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        PDFViewerCellServerState state =  new PDFViewerCellServerState();
        state.setName("PDFViewer");
        
        // Look for the content-uri field and set if so
        if (props != null) {
           String uri = props.getProperty("content-uri");
           if (uri != null) {
               state.setDocumentURI(uri);
           }
       }
        
       return (T) state;
    }

    public String getDisplayName() {
        return BUNDLE.getString("PDF_Viewer");
    }

    public Image getPreviewImage() {
        URL url = PDFViewerCellFactory.class.getResource("resources/PDFviewerApp128x128.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
