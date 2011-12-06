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
package org.jdesktop.wonderland.modules.mediaboard.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.w3c.dom.svg.SVGDocument;

/**
 * A data transfer type for transferring the contents of a whiteboard
 * as an SVG XML string in a drag and drop operation.
 *
 * @author nsimpson
 */
public class WhiteboardStateTransferable implements Transferable {

    private static final Logger logger = Logger.getLogger(WhiteboardStateTransferable.class.getName());
    private Set<DataFlavor> flavors = new HashSet();
    private SVGDocument document;

    public WhiteboardStateTransferable(SVGDocument document) {
        this.document = document;

        try {
            flavors.add(DataFlavor.javaFileListFlavor);
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
        Object data = null;

        if (flavors.contains(flavor) == false) {
            logger.warning("drag and drop: flavor: " + flavor + " not supported");
            throw new UnsupportedFlavorException(flavor);
        }

        try {
            File tmpFile = File.createTempFile("whiteboard", ".svg");
            DataOutputStream dstream = new DataOutputStream(new FileOutputStream(tmpFile));
            dstream.writeBytes(WhiteboardUtils.documentToXMLString(document));
            dstream.close();

            if (flavor.equals(DataFlavor.javaFileListFlavor) == true) {
                List<File> fileList = new LinkedList();
                fileList.add(tmpFile);
                data = fileList;
            } else {
                data = tmpFile.toURI() + "\r\n";
            }
        } catch (IOException e) {
            logger.warning("failed to create drop file for whiteboard: " + e);
        }

        return data;
    }
}
