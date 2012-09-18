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
package org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.client;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.help.WebBrowserLauncher;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentClientState;

/**
 * ClickableLinkComponents allow you to set a URL and open them with a single
 * click from inside the world
 * 
 * @author Ben (shavnir)
 * @author Bernard Horan
 * 
 */
public class ClickableLinkComponent extends CellComponent {

    private static final Logger LOGGER = Logger.getLogger(
            ClickableLinkComponent.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/clickablelink/"
            + "client/Bundle");
    /** The listener for the mouse clicks */
    private MouseEventListener listener = null;
    /** The URL to travel to */
    private String url;

    /** The constructor */
    public ClickableLinkComponent(Cell cell) {
        super(cell);

    }

    /**
     * Sets the local properties from a given ClientState.
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        if (clientState instanceof ClickableLinkComponentClientState) {
            this.url =
                    ((ClickableLinkComponentClientState) clientState).getLinkURL();
        } else {
            LOGGER.severe(
                    "ClickableLinkComponent got the wrong type of ClientState."
                    + clientState.getClass().getName());
            url = "http://www.google.com";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {

        super.setStatus(status, increasing);

        switch (status) {
            case DISK:
                //Cleanup aisle 3
                if (listener != null) {
                    CellRenderer cellRenderer =
                            cell.getCellRenderer(RendererType.RENDERER_JME);
                    CellRendererJME renderer = (CellRendererJME) cellRenderer;
                    Entity entity = renderer.getEntity();
                    listener.removeFromEntity(entity);
                    listener = null;
                    url = null;
                }
                break;

            case RENDERING:
                if (listener == null) {
                    try {
                        //Attach a click listener
                        CellRenderer cellRenderer =
                                cell.getCellRenderer(RendererType.RENDERER_JME);
                        CellRendererJME renderer =
                                (CellRendererJME) cellRenderer;
                        Entity entity = renderer.getEntity();
                        listener = new MouseEventListener();
                        listener.addToEntity(entity);
                    } catch (NullPointerException npe) {
                        LOGGER.log(Level.WARNING,
                                "could not attach click listener", npe);
                    }
                }
                break;

            default:
                break;

        }
    }

    /**
     * This is where a lot of cool stuff happens, the event listener dealing
     * with mouse clicks
     * @author bmjohnst
     *
     */
    class MouseEventListener extends EventClassListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        /**
         * This method is where the click turns into an opened browser.
         * Incredible!
         */
        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            //Make sure its a click!
            if ((mbe.isClicked() == false)
                    || (mbe.getButton() != ButtonId.BUTTON1)) {
                return;
            }

            // Slight fix because a lot of people like to put
            // "www.google.com" instead of "http://www.google.com"
            if (!url.contains("://")) {
                url = "http://" + url;
            }

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    String confirmMessage =
                            BUNDLE.getString("Confirm_Open_Message");
                    confirmMessage = MessageFormat.format(
                            confirmMessage, cell.getName(), url);
                    int selection = JOptionPane.showConfirmDialog(
                            JmeClientMain.getFrame().getFrame(), confirmMessage,
                            BUNDLE.getString("Confirm_Open_Title"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (selection == JOptionPane.YES_OPTION) {
                        try {
                            WebBrowserLauncher.openURL(url);
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE,
                                    "Failed to open URL: " + url, ex);
                        }
                    }
                }
            });
        }
    }
}
