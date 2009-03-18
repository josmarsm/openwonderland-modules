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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.svg.SVGDocument;

/**
 * A data transfer type for transferring the contents of a whiteboard
 * as an SVG XML string in a drag and drop operation.
 *
 * @author nsimpson
 */
public class WhiteboardStateTransferable implements Transferable {

    private static final Logger logger = Logger.getLogger(WhiteboardStateTransferable.class.getName());
    private static DataFlavor dataFlavor = DataFlavor.javaFileListFlavor;
    private SVGDocument document;

    public WhiteboardStateTransferable(SVGDocument document) {
        this.document = document;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{dataFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return dataFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(dataFlavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        List fileList = new LinkedList();
        try {
            File tmpFile = File.createTempFile("whiteboard", ".svg");
            DataOutputStream dstream = new DataOutputStream(new FileOutputStream(tmpFile));
            dstream.writeBytes(WhiteboardUtils.documentToXMLString(document));
            dstream.close();
            fileList.add(tmpFile);
        } catch (IOException e) {
            logger.warning("failed to create drop file for whiteboard: " + e);
        }
        return fileList;
    }
}
