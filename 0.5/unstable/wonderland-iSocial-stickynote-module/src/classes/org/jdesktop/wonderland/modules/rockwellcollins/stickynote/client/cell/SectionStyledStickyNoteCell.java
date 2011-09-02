/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
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
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.SectionStickyNoteApp;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.SectionStyledStickyNoteComponent;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.SectionStyledStickyNoteWindow;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.SectionStyledStickyNoteSyncMessage;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SelectedTextStyle;
/**
 * Client cell for the sticky note
 * @author Xiuzhen (mymegabyte)
 */
@ExperimentalAPI
public class SectionStyledStickyNoteCell extends App2DCell {

    /** The logger used by this class */
    private static final Logger LOGGER = Logger.getLogger(SectionStyledStickyNoteCell.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/Bundle");

    /** The (singleton) window created by the Swing test app */
    private SectionStyledStickyNoteWindow window;
    /** The cell client state message received from the server cell */
    private SectionStyledStickyNoteCellClientState clientState;
    /** The communications component used to communicate with the server */
    private SectionStyledStickyNoteComponent commComponent;
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    /**
     * Create an instance of StickyNoteCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public SectionStyledStickyNoteCell(CellID cellID, CellCache cellCache) {
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
        clientState = (SectionStyledStickyNoteCellClientState) state;
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
                    commComponent = getComponent(SectionStyledStickyNoteComponent.class);
                    SectionStickyNoteApp stApp = new SectionStickyNoteApp(
                            BUNDLE.getString("Section_styled_sticky_note"),
                            clientState.getPixelScale());
                    setApp(stApp);

                    // Tell the app to be displayed in this cell.
                    stApp.addDisplayer(this);

                    // This app has only one window, so it is always top-level
                    try {
                        window = new SectionStyledStickyNoteWindow(this, stApp, clientState.getPreferredWidth(),
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
                    removeComponent(SectionStyledStickyNoteComponent.class);
                    commComponent = null;
                }
                break;
        }
    }

    public void sendSyncMessage(SectionStyledStickyNoteCellClientState newState) {
        SectionStyledStickyNoteSyncMessage m = new SectionStyledStickyNoteSyncMessage(newState);
        //System.out.println("*******Sending in sendSyncMessage") ;
        commComponent.sendMessage(m);
        //clientState.copyLocal(newState);
    }

    public void processMessage(final SectionStyledStickyNoteSyncMessage m) {
        WonderlandSession session = getCellCache().getSession();
        if (!m.getSenderID().equals(session.getID())) {
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
