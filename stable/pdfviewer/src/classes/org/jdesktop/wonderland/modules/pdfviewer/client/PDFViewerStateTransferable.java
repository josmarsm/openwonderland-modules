/*
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
package org.jdesktop.wonderland.modules.pdfviewer.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A data transfer type for transferring the URI of a PDF document
 * in a drag and drop operation.
 *
 * @author nsimpson
 */
public class PDFViewerStateTransferable implements Transferable {

    private static final Logger logger = Logger.getLogger(PDFViewerStateTransferable.class.getName());
    private Set<DataFlavor> flavors = new HashSet();
    private String documentURI;

    public PDFViewerStateTransferable(String documentURI) {
        this.documentURI = documentURI;

        try {
            flavors.add(new DataFlavor("text/uri-list;class=java.lang.String"));
        } catch (ClassNotFoundException e) {
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[]{});
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavors.contains(flavor) == false) {
            logger.warning("drag and drop: flavor: " + flavor + " not supported");
            throw new UnsupportedFlavorException(flavor);
        }

        return documentURI + "\r\n";
    }
}
