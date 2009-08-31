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
package org.jdesktop.wonderland.modules.shape.client;

import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.shape.client.jme.cellrenderer.ShapeCellRenderer;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.shape.common.ShapeCellChangeMessage;
import org.jdesktop.wonderland.modules.shape.common.ShapeCellClientState;

public class ShapeCell extends Cell {
    
    private String shapeType = null;
    private String textureURI = null;
    private ShapeCellRenderer renderer = null;
    private MouseEventListener listener = null;

    public ShapeCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        this.shapeType = ((ShapeCellClientState)state).getShapeType();
        this.textureURI = ((ShapeCellClientState)state).getTextureURI();
    }

    public String getShapeType() {
        return this.shapeType;
    }

    public String getTextureURI() {
        return textureURI;
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.DISK && increasing == false) {
            listener.removeFromEntity(renderer.getEntity());
            listener = null;

            ChannelComponent channel = getComponent(ChannelComponent.class);
            channel.removeMessageReceiver(ShapeCellChangeMessage.class);
        }
        else if (status == CellStatus.RENDERING && increasing == true) {
            listener = new MouseEventListener();
            listener.addToEntity(renderer.getEntity());

            ShapeCellMessageReceiver recv = new ShapeCellMessageReceiver();
            ChannelComponent channel = getComponent(ChannelComponent.class);
            channel.addMessageReceiver(ShapeCellChangeMessage.class, recv);
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new ShapeCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            shapeType = (shapeType.equals("BOX") == true) ? "SPHERE" : "BOX";
            renderer.updateShape();

            ShapeCellChangeMessage msg = new ShapeCellChangeMessage(getCellID(), shapeType);
            sendCellMessage(msg);
        }
    }

    class ShapeCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            ShapeCellChangeMessage sccm = (ShapeCellChangeMessage)message;
            if (sccm.getSenderID().equals(getCellCache().getSession().getID()) == false) {
                shapeType = sccm.getShapeType();
                renderer.updateShape();
            }
        }
    }
}
