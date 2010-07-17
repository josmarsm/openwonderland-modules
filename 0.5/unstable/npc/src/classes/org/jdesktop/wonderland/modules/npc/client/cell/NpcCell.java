/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.npc.client.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.GeometricUpdateListener;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameContextListener;
import imi.character.statemachine.GameState;
import imi.character.statemachine.corestates.CycleActionState;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME.AvatarChangedListener;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.PickGeometry;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.PickGeometry.PickBox;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 */
public class NpcCell extends Cell
        implements CellStatusChangeListener, AvatarChangedListener,
                   ContextMenuActionListener
{
    private AvatarImiJME renderer;

    @UsesCellComponent
    private MovableNpcComponent movableNpc;

    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    private final ContextMenuFactorySPI menuFactory;

    public NpcCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        menuFactory = new ContextMenuFactorySPI() {
            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                return new ContextMenuItem[] {
                    new SimpleContextMenuItem("Controls...", NpcCell.this),
                    //new SimpleContextMenuItem("Pick Geometry...", NpcCell.this)
                };
            }
        };
    }

    public void actionPerformed(ContextMenuItemEvent event) {
        if (event.getContextMenuItem().getLabel().equals("Controls...")) {
            NpcControllerFrame frame = new NpcControllerFrame(getControls());
            frame.pack();
            frame.setVisible(true);
        } else if (event.getContextMenuItem().getLabel().equals("Pick Geometry...")) {
            PickGeometry pg = renderer.getPickGeometry();
            for (Spatial s : pg.getChildren()) {
                PickGeometryEditor editor = new PickGeometryEditor(pg, (PickBox) s, renderer.getAvatarCharacter());
                editor.pack();
                editor.setVisible(true);
            }
        }
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        // If the Cell is being made active and increasing, then add the menu
        // item. Also add the proximity listener
        if (status == CellStatus.ACTIVE && increasing == true) {
            contextMenu.addContextMenuFactory(menuFactory);
            addStatusChangeListener(this);
            return;
        }

        // if the Cell is being brought back down through the ACTIVE state,
        // then remove the menu item
        if (status == CellStatus.ACTIVE && increasing == false) {
            contextMenu.removeContextMenuFactory(menuFactory);
            removeStatusChangeListener(this);
            return;
        }
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
        if (status == CellStatus.ACTIVE) {
            // do this in a status change listener to ensure that the renderer
            // is created at the time we want to add a listener
            renderer.addAvatarChangedListener(this);
            if (renderer.getAvatarCharacter() != null) {
                avatarChanged(renderer.getAvatarCharacter());
            }
        }
    }

    public void avatarChanged(Avatar avatar) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                attachEditorGeometry();
                attachCellLocationUpdater();
                attachAnimationListener();
            }
        });
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch (rendererType) {
            case RENDERER_2D:
                // No 2D Renderer yet
                break;
            case RENDERER_JME:
                try {
                    ServerSessionManager session = getCellCache().getSession().getSessionManager();
                    ret = ClientContextJME.getAvatarRenderManager().createRenderer(session, this);

                    if (ret instanceof AvatarImiJME) {
                        renderer = (AvatarImiJME) ret;
                    }
                } catch (RendererUnavailable ex) {
                    Logger.getLogger(NpcCell.class.getName()).log(Level.SEVERE, null, ex);
                    ret = new AvatarJME(this);
                }
                break;
        }

        return ret;
    }

    public NpcControls getControls() {
        return new NpcControls() {
            public void triggerActionStart(TriggerNames trigger) {
                renderer.getAvatarCharacter().triggerActionStart(trigger);
            }

            public void triggerActionStop(TriggerNames trigger) {
                renderer.getAvatarCharacter().triggerActionStop(trigger);
            }

            public Iterable<String> getAnimations() {
                return renderer.getAvatarCharacter().getAnimationNames();
            }

            public void playAnimation(String animation) {
                renderer.getAvatarCharacter().playAnimation(animation);
            }

            public void goTo(float x, float y, float z) {
                move(x, y, z);
            }
        };
    }

    protected void move(float x, float y, float z) {
        GameContext context = renderer.getAvatarCharacter().getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(new GoTo(new Vector3f(x, y, z), context));
    }

    private void attachEditorGeometry() {
        Entity e = renderer.getEntity();
        if (e.getComponent(RenderComponent.class) == null) {
            Box b = new Box("Avatar editor", Vector3f.ZERO, 0.4f, 0.95f, 0.1f);
            b.setLocalTranslation(new Vector3f(0f, 1f, 0f));
            b.setModelBound(new BoundingBox(Vector3f.ZERO, b.getXExtent(),
                                            b.getYExtent(), b.getZExtent()));
            b.updateGeometricState(0, true);
            b.setCullHint(Spatial.CullHint.Always);

            Node n = new Node("Avatar editor");
            n.attachChild(b);

            RenderManager rm =  ClientContextJME.getWorldManager().getRenderManager();
            RenderComponent rc = rm.createRenderComponent(n);
            rc.setAttachPoint(renderer.getAvatarCharacter().getJScene().getExternalKidsRoot());

            e.addComponent(RenderComponent.class, rc);
            ClientContextJME.getWorldManager().addToUpdateList(n);
        }
    }

    private void attachCellLocationUpdater() {
        Node extKids = renderer.getAvatarCharacter().getJScene().getExternalKidsRoot();
        extKids.addGeometricUpdateListener(new GeometricUpdateListener() {
            public void geometricDataChanged(Spatial sptl) {
                CellTransform xform = new CellTransform(sptl.getWorldRotation(),
                                                        sptl.getWorldTranslation(),
                                                        sptl.getWorldScale().x);

                movableNpc.geometryChanged(xform);
            }
        });
    }

    private void attachAnimationListener() {
        final WlAvatarCharacter character = renderer.getAvatarCharacter();
        character.getContext().addGameContextListener(new GameContextListener() {

            public void trigger(boolean pressed, int trigger, Vector3f translation, Quaternion rotation) {
                GameState state = character.getContext().getCurrentState();
                String animationName=null;
                if (state instanceof CycleActionState) {
                    animationName = character.getContext().getState(CycleActionState.class).getAnimationName();
                }

                movableNpc.localMoveRequest(new CellTransform(rotation, translation), trigger, pressed, animationName, null);
            }
        });
    }
}
