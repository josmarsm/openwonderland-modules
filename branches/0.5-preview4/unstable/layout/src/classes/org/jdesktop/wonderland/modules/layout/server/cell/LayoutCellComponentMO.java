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
package org.jdesktop.wonderland.modules.layout.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.containercell.server.ContainerComponentMO;
import org.jdesktop.wonderland.modules.layout.api.common.Layout;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipantProvider;
import org.jdesktop.wonderland.modules.layout.common.LayoutCellComponentClientState;
import org.jdesktop.wonderland.modules.layout.common.LayoutCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellChildrenChangeListenerSrv;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side Cell Component for the layout.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class LayoutCellComponentMO extends ContainerComponentMO {

    // The error logger
    private static Logger LOGGER =
            Logger.getLogger(LayoutCellComponentMO.class.getName());

    // The current layout in use
    private Layout layout = null;

    // The listener for changes in the children of the Cell associated with this
    // Cell Component
    private ChildListener childListener = null;

    // A Map of child Cell ID's to their layout participants. We need this map
    // so that we may remove the layout participant when the child Cell is
    // removed.
    private Map<CellID, LayoutParticipant> participantMap =
            new HashMap<CellID, LayoutParticipant>();

    /**
     * Constructor.
     * @param cell The CellMO to which this component is attached
     */
    public LayoutCellComponentMO(CellMO cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.layout.client.cell.LayoutCellComponent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        // Create a new client state object, if we are not already passed one
        // in (perhaps by a superclass).
        if (state == null) {
            state = new LayoutCellComponentClientState();
        }

        // Set the layout configuration, obtained from the current layout
        if (layout != null) {
            ((LayoutCellComponentClientState)state).setLayoutConfig(
                    layout.getConfig());
        }
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState state) {

        // Create a new server state object, if we are not already passed one
        // in (perhaps by a superclass).
        if (state == null) {
            state = new LayoutCellComponentServerState();
        }

        // Set the layout configuration, obtained from the current layout
        if (layout != null) {
            ((LayoutCellComponentServerState)state).setLayoutConfig(
                    layout.getConfig());
        }
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

        // Update the layout, by creating a new layout based upon the layout
        // configuration.
        LayoutConfig layoutConfig =
                ((LayoutCellComponentServerState)state).getLayoutConfig();

        if (layoutConfig != null) {
            try {
                layout = layoutConfig.getLayoutClass().newInstance();
                layout.setConfig(layoutConfig);
            } catch (java.lang.Exception excp) {
                LOGGER.log(Level.WARNING, "Unable to create new layout class " +
                        "from layout config " +
                        layoutConfig.getClass().getName() + ", with layout " +
                        "class " + layoutConfig.getLayoutClass().getName(),
                        excp);
                layout = null;
            }
        }
        else {
            layout = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        // Either add or remove the listener for changes to the children to
        // this container Cell. Tell the layout of the add/remove.
        CellMO cellMO = cellRef.getForUpdate();
        if (live == true) {
            childListener = new ChildListener(this);
            cellMO.addChildrenChangeListener(childListener);
        }
        else {
            cellMO.removeChildrenChangeListener(childListener);
            childListener = null;
        }
    }

    /**
     * Used by subclasses to force a particular layout.
     * @param layout the layout to use
     */
    protected void setLayout(Layout layout) {
        this.layout = layout;
    }

    /**
     * Get the current layout
     * @return the current layout
     */
    protected Layout getLayout() {
        return layout;
    }

    /**
     * Inner class that listens for changes to the set of children on the Cell
     * associated with this Cell Component
     */
    private static class ChildListener implements CellChildrenChangeListenerSrv,
            Serializable {

        // A references to the layout component
        private ManagedReference<LayoutCellComponentMO> layoutRef = null;

        /**
         * Constructor, takes the layout cell component
         */
        public ChildListener(LayoutCellComponentMO layoutMO) {
            layoutRef = AppContext.getDataManager().createReference(layoutMO);
        }

        /**
         * {@inheritDoc}
         */
        public void childAdded(CellMO cell, CellMO child) {
            LayoutCellComponentMO layoutMO = layoutRef.getForUpdate();

            // If there is no layout, then log an error and return
            if (layoutMO.layout == null) {
                LOGGER.warning("No layout for child " + child.getName() +
                        " added to container " + cell.getName());
                return;
            }

            // There are two code paths here: (1) if the Cell has been just
            // created (or reparanted) in the live world, (2) the Cell has been
            // reloaded from WFS. In the first case, we want to add the Cell
            // to the layout such that is does the layout. In the second case,
            // we want to tell the layout of the child Cell, but not have it
            // do the layout, since the child Cell's position should already
            // be stored in the XML file.
            LayoutParticipantCellComponentMO participantMO =
                    child.getComponent(LayoutParticipantCellComponentMO.class);
            int index = -1;
            if (participantMO != null) {
                index = participantMO.getPosition();
            }

            LOGGER.warning("For child added, existing index of " + index);
            
            // Check to see if the new child cell offers a layout participant.
            // If so, then use that, otherwise, create a proxy for one.
            LayoutParticipant participant = null;
            if (child instanceof LayoutParticipantProvider) {
                LayoutParticipantProvider lpp = (LayoutParticipantProvider) child;
                participant = lpp.getLayoutParticipant();
            }
            else {
                participant = new CellMOLayoutParticipant(child);
            }

            // Final check to make sure the layout participant is not null
            if (participant == null) {
                LOGGER.warning("Layout participant is null for child " +
                        child.getName() + " added to container " +
                        cell.getName());
                return;
            }
            
            // Add the participant, in a different manner depending upon whether
            // we are just re-adding it from WFS or whether this is a brand new
            // Cell to the layout.
            if (index == -1) {
                index = layoutMO.layout.addParticipant(participant);
            }
            else {
                layoutMO.layout.setParticipantAt(participant, index);
            }
            layoutMO.participantMap.put(child.getCellID(), participant);

            // If the participant component on the child cell does not exist,
            // then let's add it.
            if (participantMO == null) {
                participantMO = new LayoutParticipantCellComponentMO(cell);
                participantMO.setPosition(index);
                child.addComponent(participantMO);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void childRemoved(CellMO cell, CellMO child) {
            LayoutCellComponentMO layoutMO = layoutRef.getForUpdate();

            // If there is no layout, then log an error and return
            if (layoutMO.layout == null) {
                LOGGER.warning("No layout for child " + child.getName() +
                        " added to container " + cell.getName());
                return;
            }

            // Using the Cell ID of the child, look up its participant in the
            // map
            CellID id = child.getCellID();
            LayoutParticipant participant = layoutMO.participantMap.get(id);

            // Make sure the participant is not null
            if (participant == null) {
                LOGGER.warning("Layout participant is null for child " +
                        child.getName() + " removed from container " +
                        cell.getName());
                return;
            }

            // Remove the participant and the participant component from the
            // child cell
            layoutMO.layout.removeParticipant(participant);
            layoutMO.participantMap.remove(id);
            LayoutParticipantCellComponentMO participantMO =
                    child.getComponent(LayoutParticipantCellComponentMO.class);
            if (participantMO != null) {
                child.removeComponent(participantMO);
            }
        }
    }
}
