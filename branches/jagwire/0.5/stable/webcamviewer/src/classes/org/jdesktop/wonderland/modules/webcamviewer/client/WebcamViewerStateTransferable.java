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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * A data transfer type for transferring the URI of a webcam
 * in a drag and drop operation.
 *
 * @author nsimpson
 */
public class WebcamViewerStateTransferable implements Transferable {

    private static final Logger logger = Logger.getLogger(WebcamViewerStateTransferable.class.getName());
    private Set<DataFlavor> flavors = new HashSet();
    private Image snapshot;

    public WebcamViewerStateTransferable(Image snapshot) {
        this.snapshot = snapshot;

        try {
            flavors.add(new DataFlavor("image/jpg;class=javax.swing.ImageIcon"));
        } catch (ClassNotFoundException e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[]{});
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    /**
     * {@inheritDoc}
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavors.contains(flavor) == false) {
            logger.warning("drag and drop: flavor: " + flavor + " not supported");
            throw new UnsupportedFlavorException(flavor);
        }
        return new ImageIcon(snapshot);
    }
}
