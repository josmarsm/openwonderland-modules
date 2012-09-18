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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell;

import java.awt.Color;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.StickyNoteApp;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.StickyNoteComponent;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.StickyNoteWindow;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;

/**
 * Client cell for the sticky note
 * @author Ryan (mymegabyte)
 */
@ExperimentalAPI
public class StickyNoteCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger LOGGER = Logger.getLogger(StickyNoteCell.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/Bundle");

    /** The (singleton) window created by the Swing test app */
    private StickyNoteWindow window;
    /** The cell client state message received from the server cell */
    private StickyNoteCellClientState clientState;
    /** The communications component used to communicate with the server */
    private StickyNoteComponent commComponent;
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    /**
     * Create an instance of StickyNoteCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public StickyNoteCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (StickyNoteCellClientState) state;
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                if (increasing) {
                    commComponent = getComponent(StickyNoteComponent.class);
                    StickyNoteApp stApp = new StickyNoteApp(
                            BUNDLE.getString("Post-it_Note"),
                            clientState.getPixelScale());
                    setApp(stApp);

                    // Tell the app to be displayed in this cell.
                    stApp.addDisplayer(this);

                    // This app has only one window, so it is always top-level
                    try {
                        window = new StickyNoteWindow(this, stApp, clientState.getPreferredWidth(),
                                clientState.getPreferredHeight(), true, pixelScale, clientState);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    //Create the new menu item
                    if (menuFactory == null) {
                        menuFactory = new ContextMenuFactorySPI() {

                            public ContextMenuItem[] getContextMenuItems(
                                    ContextEvent event) {
                                return new ContextMenuItem[]{new SimpleContextMenuItem(BUNDLE.getString("Change_Color"),
                                            new ContextMenuActionListener() {

                                                public void actionPerformed(ContextMenuItemEvent event) {
                                                    Color newColor = getUserSelectedColor();
                                                    if(newColor == null || clientState == null) {
                                                        return;
                                                    }
                                                    clientState.setNoteColor(newColor.getRed() + ":" + newColor.getGreen() + ":"+ newColor.getBlue());
                                                    sendSyncMessage(clientState);
                                                }
                                            })};
                            }
                        };
                        contextComp.addContextMenuFactory(menuFactory);
                    }

                    // Both the app and the user want this window to be visible
                    window.setVisibleApp(true);
                    window.setVisibleUser(this, true);

                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    window.setVisibleApp(false);
                    window = null;
                    removeComponent(StickyNoteComponent.class);
                    commComponent = null;
                }
                break;
        }
    }

    public void sendSyncMessage(StickyNoteCellClientState newState) {
        StickyNoteSyncMessage m = new StickyNoteSyncMessage(newState);
        commComponent.sendMessage(m);
        //clientState.copyLocal(newState);
    }

    public void processMessage(final StickyNoteSyncMessage m) {
        WonderlandSession session = getCellCache().getSession();
        if (!session.getID().equals(m.getSenderID())) {
            window.getStickynoteParentPanel().getChild().processMessage(m);
        } else {
            // Color messages are special
            SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                window.getStickynoteParentPanel().getChild().setColor(m.getState().getNoteColor());
            }});
            
        }
        clientState.copyLocal(m.getState());
    }

    public static Color parseColorString(String colorString) {
        String[] c = colorString.split(":");
        if(c.length < 3) {
            LOGGER.severe("Improperly formatted color string passed: " + colorString);
            return null;
        }
        Integer r = Integer.parseInt(c[0]);
        Integer g = Integer.parseInt(c[1]);
        Integer b = Integer.parseInt(c[2]);
        Color newColor = new Color(r,g,b);
        return newColor;
    }

    public Color getUserSelectedColor() {
        Color oldColor = parseColorString(clientState.getNoteColor());
        Color newColor = JColorChooser.showDialog(
                JmeClientMain.getFrame().getFrame(),
                BUNDLE.getString("Post-it_Color"), oldColor);
        return newColor;
    }
}
