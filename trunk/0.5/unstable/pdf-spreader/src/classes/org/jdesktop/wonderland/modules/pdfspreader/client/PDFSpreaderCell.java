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

package org.jdesktop.wonderland.modules.pdfspreader.client;

import com.sun.pdfview.PDFFile;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.pdfspreader.client.jme.cell.PDFSpreaderCellRenderer;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellClientState;

public class PDFSpreaderCell extends Cell {

    private PDFSpreaderCellRenderer renderer = null;

    private MouseEventListener listener = null;

    private static final Logger logger =
            Logger.getLogger(PDFSpreaderCell.class.getName());

    private int numAvatarsInZone = 0;

    // It's a single space because an empty string causes the label node
    // to barf. TODO fix the label node so it can more gracefully handle
    // empty strings as labels. 
    private String label = " ";
    private String pdfURI;

    private PDFFile pdfDocument;

    public PDFSpreaderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
  
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        this.pdfURI = ((PDFSpreaderCellClientState)state).getPdfURI();
        
//        this.numAvatarsInZone = ((PDFSpreaderCellClientState)state).getNumAvatarsInZone();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);


        if(status==CellStatus.ACTIVE && increasing) {

//            listener = new MouseEventListener(labelDialog);
//            listener.addToEntity(renderer.getEntity());

            channel.addMessageReceiver(PDFSpreaderCellChangeMessage.class, new PDFSpreaderCellMessageReceiver());

        } else if (status==CellStatus.DISK && !increasing) {
//            listener.removeFromEntity(renderer.getEntity());
//            listener = null;
            
        } else if (status==CellStatus.RENDERING&& !increasing) {
            // As we're falling down the status chain, try removing the listener
            // earlier. It seems to be gone by the time we get to DISK.
            channel.removeMessageReceiver(PDFSpreaderCellChangeMessage.class);
        }

    }

    public String getSourceURI() {
        return this.pdfURI;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new PDFSpreaderCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }
 
    class PDFSpreaderCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            PDFSpreaderCellChangeMessage msg = (PDFSpreaderCellChangeMessage)message;

            switch(msg.getAction()) {
                default:
                    logger.warning("Received unknown message type in client: " + msg.getAction());
                    break;
            }
        }
    }

    class MouseEventListener extends EventClassListener {

        private JFrame labelDialog;

        public MouseEventListener (JFrame d) {
            super();

            labelDialog = d;
            setSwingSafe(true);
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;

            // Filter out right mouse clicks.
            if(mbe.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                logger.info("Got click! " + event);
            }
        }

    }




    public PDFFile getDocument() {
        return this.pdfDocument;
    }

    private void setDocument(PDFFile document) {
        this.pdfDocument = document;
    }
}