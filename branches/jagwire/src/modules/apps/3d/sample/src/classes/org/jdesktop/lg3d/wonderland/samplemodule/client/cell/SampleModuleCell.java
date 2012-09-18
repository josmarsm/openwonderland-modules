/**
 * Project Looking Glass
 * 
 * $RCSfile: SampleModuleCell.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.2 $
 * $Date: 2008/01/23 18:48:44 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.samplemodule.client.cell;

import com.sun.j3d.utils.geometry.Box;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3f;
import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.MouseButtonEvent3D;
import org.jdesktop.lg3d.wg.event.MouseEnteredEvent3D;
import org.jdesktop.lg3d.wg.internal.j3d.j3dnodes.J3dLgBranchGroup;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleCellMessage;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleCellSetup;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 *
 * @author jkaplan
 */
public class SampleModuleCell extends Cell
        implements ExtendedClientChannelListener {

    private static final Logger logger =
            Logger.getLogger(SampleModuleCell.class.getName());
    private static final Appearance DEFAULT_APPEARANCE = new Appearance();
    private static final Appearance HOVER_APPEARANCE = new Appearance();
    private static final Appearance HOVER_OTHER_APPEARANCE = new Appearance();
    private static final Appearance SELECTED_APPEARANCE = new Appearance();
    private List<SelectableBox> boxes;

    static {
        DEFAULT_APPEARANCE.setColoringAttributes(new ColoringAttributes(0.8f, 0.8f, 0.8f,
                ColoringAttributes.SHADE_GOURAUD));
        HOVER_APPEARANCE.setColoringAttributes(new ColoringAttributes(0, 0, 1,
                ColoringAttributes.SHADE_GOURAUD));
        HOVER_OTHER_APPEARANCE.setColoringAttributes(new ColoringAttributes(0, 1, 0,
                ColoringAttributes.SHADE_GOURAUD));
        SELECTED_APPEARANCE.setColoringAttributes(new ColoringAttributes(1, 0, 0,
                ColoringAttributes.SHADE_GOURAUD));
    }

    public SampleModuleCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setup(CellSetup setup) {
        // Create the spheres and add them to the scene
        boxes = new ArrayList<SelectableBox>();
        boxes.add(createBox("one", new Vector3f(-0.5f, 0.0f, 0.0f)));
        boxes.add(createBox("two", new Vector3f(0.0f, 0.0f, 0.0f)));
        boxes.add(createBox("three", new Vector3f(0.5f, 0.0f, 0.0f)));

        // handle initial selection
        handleSelection(((SampleModuleCellSetup) setup).getSelectionID());
    }

    private SelectableBox createBox(String id, Vector3f location) {
        J3dLgBranchGroup bg = new J3dLgBranchGroup();
        SelectableBox box = new SelectableBox(id, 0.2f);
        box.setAppearance(DEFAULT_APPEARANCE);
        addMouseEvents(bg, box);

        TransformGroup trans = new TransformGroup();
        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D pos1 = new Transform3D();
        pos1.setTranslation(location);
        trans.setTransform(pos1);

        trans.addChild(box);
        bg.addChild(trans);
        SceneGraphUtil.setCapabilitiesGraph(bg, false);
        cellLocal.addChild(bg);

        return box;
    }

    private void addMouseEvents(J3dLgBranchGroup bg, SelectableBox box) {
        bg.setCapabilities();
        bg.setMouseEventEnabled(true);
        bg.setMouseEventSource(MouseEnteredEvent3D.class, true);
        bg.setMouseEventSource(MouseButtonEvent3D.class, true);

        bg.addListener(new MouseSelectionListener(box));
    }

    private void handleSelection(String selection) {
        for (SelectableBox box : boxes) {
            if (box.getId().equalsIgnoreCase(selection)) {
                box.setSelected(true);
            } else {
                box.setSelected(false);
            }
        }
    }

    private void handleHoverOther(String selection, boolean hover) {
        for (SelectableBox box : boxes) {
            if (box.getId().equalsIgnoreCase(selection)) {
                box.setHoverOther(hover);
            }
        }
    }

    public void receivedMessage(ClientChannel client, SessionId session,
            byte[] data) {
        SampleModuleMessage message =
                Message.extractMessage(data, SampleModuleMessage.class);

        switch (message.getAction()) {
            case SELECT:
                handleSelection(message.getSelectionID());
                break;
            case START_HOVER:
                handleHoverOther(message.getSelectionID(), true);
                break;
            case STOP_HOVER:
                handleHoverOther(message.getSelectionID(), false);
                break;
        }
    }

    public void leftChannel(ClientChannel arg0) {
    // ignore
    }

    class MouseSelectionListener implements LgEventListener {

        private SelectableBox box;

        public MouseSelectionListener(SelectableBox box) {
            this.box = box;
        }

        public void processEvent(LgEvent evt) {
            if (evt instanceof MouseEnteredEvent3D) {
                // react to mouse enter/exit events
                MouseEnteredEvent3D mevt = (MouseEnteredEvent3D) evt;
                SampleModuleMessage msg;

                if (mevt.isEntered()) {
                    box.setHover(true);
                    msg = new SampleModuleMessage(box.getId(), SampleModuleMessage.Action.START_HOVER);
                } else {
                    box.setHover(false);
                    msg = new SampleModuleMessage(box.getId(), SampleModuleMessage.Action.STOP_HOVER);
                }

                try {
                    channel.send(msg.getBytes());
                } catch (IOException ioe) {
                    logger.log(Level.WARNING, "Error sending message", ioe);
                }
            } else if (evt instanceof MouseButtonEvent3D) {
                // react to button presses
                MouseButtonEvent3D mevt = (MouseButtonEvent3D) evt;
                SampleModuleCellMessage msg;

                if (mevt.isClicked()) {
                    if (box.isSelected()) {
                        handleSelection(null);
                        msg = new SampleModuleCellMessage(getCellID(), null);
                    } else {
                        handleSelection(box.getId());
                        msg = new SampleModuleCellMessage(getCellID(), box.getId());
                    }

                    // Send a message to the server indicating the new selection.
                    // The server will repeat it out to all other clients.
                    ChannelController.getController().sendMessage(msg);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public Class<LgEvent>[] getTargetEventClasses() {
            return new Class[]{MouseEnteredEvent3D.class,
                MouseButtonEvent3D.class
            };
        }
    }

    static class SelectableBox extends Box {

        private String id;
        private boolean selected;
        private boolean hover;
        private boolean hoverOther;

        public SelectableBox(String id, float size) {
            super(size, size, size, Box.ENABLE_APPEARANCE_MODIFY, null);

            this.id = id;
        }

        public String getId() {
            return id;
        }

        public boolean isHover() {
            return hover;
        }

        public void setHover(boolean hover) {
            this.hover = hover;
            updateAppearance();
        }

        public boolean isHoverOther() {
            return hoverOther;
        }

        public void setHoverOther(boolean hoverOther) {
            this.hoverOther = hoverOther;
            updateAppearance();
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateAppearance();
        }

        private void updateAppearance() {
            if (selected) {
                setAppearance(SELECTED_APPEARANCE);
            } else if (hover) {
                setAppearance(HOVER_APPEARANCE);
            } else if (hoverOther) {
                setAppearance(HOVER_OTHER_APPEARANCE);
            } else {
                setAppearance(DEFAULT_APPEARANCE);
            }
        }
    }
}
