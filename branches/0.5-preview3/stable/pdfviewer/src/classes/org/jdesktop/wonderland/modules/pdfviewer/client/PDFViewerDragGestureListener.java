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

import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.logging.Logger;

/**
 * A listener for drag events.
 *
 * @author nsimpson
 */
public class PDFViewerDragGestureListener implements DragGestureListener {

    private static final Logger logger = Logger.getLogger(PDFViewerDragGestureListener.class.getName());
    public Image previewImage = null;
    private PDFViewerWindow window;

    public PDFViewerDragGestureListener(PDFViewerWindow window) {
        super();
        this.window = window;
    }

    /**
     * {@inheritDoc}
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        logger.fine("drag started: " + dge);

        Point dragOrigin = dge.getDragOrigin();
        dragOrigin.setLocation(-dragOrigin.x, -dragOrigin.y);

        Transferable t = new PDFViewerStateTransferable(window.getDocumentURI());
        dge.startDrag(DragSource.DefaultCopyNoDrop, previewImage, dragOrigin, t, null);
    }
}
