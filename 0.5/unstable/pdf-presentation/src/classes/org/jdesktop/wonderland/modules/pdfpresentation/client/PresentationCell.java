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

package org.jdesktop.wonderland.modules.pdfpresentation.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.sun.pdfview.PDFFile;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.pdf.client.PDFDeployer;
import org.jdesktop.wonderland.modules.pdfpresentation.client.jme.cell.MovingPlatformCellRenderer;
import org.jdesktop.wonderland.modules.pdfpresentation.client.jme.cell.PresentationCellRenderer;
import org.jdesktop.wonderland.modules.pdfpresentation.client.jme.cell.PresentationCellRenderer;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellClientState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellChangeMessage.MessageType;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellServerState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout.LayoutType;

/**
 * This cell encapsulates the PDF spreading functionality, as well as managing
 * the presentation platform.
 * 
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationCell extends Cell implements ProximityListener, ActionListener {
    
    private MovingPlatformCellRenderer platformRenderer = null;

    private static final Logger logger =
            Logger.getLogger(PresentationCell.class.getName());

    @UsesCellComponent
    private ProximityComponent prox;

    private JButton nextSlideButton;
    private JButton prevSlideButton;

    protected static final String NEXT_SLIDE_ACTION = "next_slide";
    protected static final String PREV_SLIDE_ACTION = "prev_slide";

    private MovingPlatformCell platform;

    // These are the fields pulled from the PDF Spreader.
    private String pdfURI;

    private PDFFile pdfDocument;

    private PresentationLayout layout;

    private String creatorName;

    private PresentationCellRenderer pdfRenderer = null;


    public PresentationCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        // Register the platform with the PresentationManager
        // on status changes.
        if(status==CellStatus.ACTIVE && increasing) {

//            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, 10.0f, 20.0f, 10.0f));

            logger.warning("About to init proximity listener to bounds: " + this.getLocalBounds());
            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds().clone(null)};
            prox.addProximityListener(this, bounds);
            logger.warning("Added proximity listener.");

            // register with the global list of presentation cells.
            PresentationsManager.getPresentationsManager().addPresentationCell(this);

            // from the old PDF spreader cells, which did need to pass
            // messages around.
            channel.addMessageReceiver(PresentationCellChangeMessage.class, new PresentationCellMessageReceived());

        } else if (status==CellStatus.DISK && !increasing) {
            prox.removeProximityListener(this);
            PresentationsManager.getPresentationsManager().removePresentationCell(this);
            
            // from the old PDF spreader cells, which did need to pass
            // messages around.

            logger.warning("About to remove message receiver from channel: " + channel);
            if(channel!=null)
                channel.removeMessageReceiver(PresentationCellChangeMessage.class);
            else
                logger.warning("Found null channel whem trying to remove the message receiver.");
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.pdfRenderer = new PresentationCellRenderer(this);
            return this.pdfRenderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        logger.warning("view enter/exit. entered: " + entered);
        
        // Check to see if the avatar entering/exiting is the local one.
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                logger.warning("Local user in presentation space.");

                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

                // Add in next/previous slide buttons.
                if(nextSlideButton==null && prevSlideButton == null) {
                    nextSlideButton = new JButton("Next Slide");
                    nextSlideButton.setActionCommand(NEXT_SLIDE_ACTION);
                    nextSlideButton.addActionListener(this);

                    prevSlideButton = new JButton("Previous Slide");
                    prevSlideButton.setActionCommand(PREV_SLIDE_ACTION);
                    prevSlideButton.addActionListener(this);
                }

                   PresentationToolbarManager.getManager().addToolbarButton(nextSlideButton);
                   PresentationToolbarManager.getManager().addToolbarButton(prevSlideButton);
            } else {
                logger.warning("Local user out of presentation space.");

               AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);

               PresentationToolbarManager.getManager().removeToolbarButton(nextSlideButton);
               PresentationToolbarManager.getManager().removeToolbarButton(prevSlideButton);
               
            }
        }


    }

//    public static void createPresentationSpace(Cell slidesCell) {
//
//        // Do a bunch of exciting things now to do this setup, including
//        // getting layout information from the slidesCell.
//
//        logger.warning("Setting up a presentation space for slidesCell: " + slidesCell);
//
//        // Overall steps:
//        //
//        // 0. Put a toolbar up for everyone that gives them next/previous controls.
//        //     (eventually this should be just for the username that created
//        //      the file, but it's not clear to me how to do that since this
//        //      object contains only local state and isn't synced at all.)
//        // ------------------ deferring this until the platform is in place ---
//
//        // 1. Create a new PresentationCell, put it in the right place, and
//        //    reparent the PDF cell into it. Size it so it contains the PDF cell
//        //    plus tons of extra space in front of the slides for the platform.
//
//        // Get a reference to the connection we'll use to send these messages.
//        ClientConnection sender = LoginManager.getPrimary().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE);
//
//        PresentationCellServerState state = new PresentationCellServerState();
//
//        state.setSlidesCellID(slidesCell.getCellID());
//        state.setInitialized(false);
//
//
//        CellCreateMessage createPresentationCell = new CellCreateMessage(null, state);
//
//        LoginManager.getPrimary().getPrimarySession().getConnection(CellEditConnectionType.CLIENT_TYPE).getSession().send(sender, createPresentationCell);
//
//        // This setup process continues on the server side, where the cell
//        // figures out how big it is and where it should go in the
//        // setServerState method on the just-created new cell.
//    }

    public void actionPerformed(ActionEvent arg0) {
        if(arg0.getActionCommand().equals(NEXT_SLIDE_ACTION)) {
            // Send a message to the server, teling it to increment the slide count.
            PresentationCellChangeMessage msg = new PresentationCellChangeMessage(MessageType.SLIDE_CHANGE);
            msg.setSlideIncrement(+1);
            this.sendCellMessage(msg);
            
        } else if(arg0.getActionCommand().equals(PREV_SLIDE_ACTION)) {
            PresentationCellChangeMessage msg = new PresentationCellChangeMessage(MessageType.SLIDE_CHANGE);
            msg.setSlideIncrement(-1);
            this.sendCellMessage(msg);
        }
    }

    /**
     *
     * @param pos The position to test for a potential moving platform parent. Specified in world coordinates.
     * @return A MovingPlatformCell object that contains the specified point, or null if this PresentationCell's MovingPlatformCell doesn't contain that position.
     */
    public Cell getParentCellByPosition(Vector3f pos) {
        // This may have troubles until issue 497 is resolved, but I assume
        // a setLocalTransform will get called.

        if(this.platform==null)
            return null;

        logger.warning("Checking to see if " + pos + " is within our platform cell: " + this.platform.getWorldBounds());

        logger.warning("Alternatively, is it in our presentation cell? " + this.getWorldBounds());

        if(this.platform.getWorldBounds().contains(pos))
            return this.platform;
        else {
            if(this.getWorldBounds().contains(pos))
                return this;
            return null;
        }
    }

    public void setPlatformCell(MovingPlatformCell platform) {
        this.platform = platform;
    }


    //*********************************//
    // METHODS FROM PDF SPREADER       //
    //*********************************//
     protected void updateLayout() {

        try {
            // On updateLayout, trigger a refresh based on current settings.
            this.layout.setSlides(PDFLayoutHelper.generateLayoutMetadata(this.layout.getLayout(), PDFDeployer.loadDeployedPDF(pdfURI), this.layout.getSpacing()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(PresentationCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PresentationCell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(PresentationCell.class.getName()).log(Level.SEVERE, null, ex);
        }

        pdfRenderer.layoutUpdated();
    }

    protected void sendCurrentLayoutToServer() {
        // This ONLY gets called by the HUD panel, so when we get this call
        // we know that a local change has occured that we need to send
        // to the server.
        PresentationCellChangeMessage msg = new PresentationCellChangeMessage(MessageType.LAYOUT);

        msg.setLayout(layout);

        this.sendCellMessage(msg);

        // Also, trigger a local relayout operation here.
        this.updateLayout();
        logger.info("just sent cell message to server: " + msg);
    }

    @Override
    public void setClientState(CellClientState state) {
        logger.warning("IN SET CLIENT STATE");
        logger.warning("ClientState class: " + state.getName() + " (" + state.getClass().getName() + ")");

        super.setClientState(state);

        this.pdfURI = ((PresentationCellClientState)state).getPdfURI();
        this.layout = ((PresentationCellClientState)state).getLayout();
        this.creatorName = ((PresentationCellClientState)state).getCreatorName();
    }

    public PresentationLayout getLayout() {
        return layout;
    }

    public void setLayout(PresentationLayout layout) {
        logger.finer("Setting layout to: " + layout);
        this.layout = layout;

        // This gets called only when messages come in from other clients
        // who have presumably caused a relayout operation.
        // This means we should update the positions (but not textures
        // or node properties) of all our slides.
    }

    public void setLayoutType(LayoutType layout) {
        this.layout.setLayout(layout);
    }

    public float getScale() {
        return this.layout.getScale();
    }

    public void setScale(float scale) {
        this.layout.setScale(scale);
        logger.finer("Setting scale to: " + scale);
    }

    public float getSpacing() {
        return this.layout.getSpacing();
    }

    public void setSpacing(float spacing) {
        logger.finer("Setting spacing to: " + spacing);
        this.layout.setSpacing(spacing);
    }

    public String getSourceURI() {
        return this.pdfURI;
    }


    public String getCreatorName() {
        return this.creatorName;
    }

    class PresentationCellMessageReceived implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            PresentationCellChangeMessage msg = (PresentationCellChangeMessage)message;

            // if we got a message, grab the layout data and push it into the cell.
            logger.info("Received a new layout: " + msg.getLayout());
            setLayout(msg.getLayout());
            updateLayout();
        }
    }
}
