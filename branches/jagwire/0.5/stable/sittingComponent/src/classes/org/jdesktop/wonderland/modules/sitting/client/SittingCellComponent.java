/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.sitting.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import imi.character.avatar.AvatarContext;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoSit;
import imi.character.statemachine.GameContext;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.sitting.common.SittingCellComponentClientState;

/**
 * Client-side sitting cell component
 * 
 * @author Morris Ford
 */
public class SittingCellComponent extends CellComponent
    {

    private static Logger logger = Logger.getLogger(SittingCellComponent.class.getName());
    private MouseEventListener myListener = null;
    private WlAvatarCharacter myAvatar;
    private CellRendererJME ret = null;
    private Node localNode = null;
    private float heading = 0.1f;
    private float offset = 0.1f;
    private String myMouse = "Left Mouse";
    private boolean mouseEnable = false;

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/sitting/client/resources/Bundle");

    public SittingCellComponent(Cell cell)
        {
        super(cell);
        }

    public void testMouse(MouseButtonEvent3D mbe)
        {
        MouseEvent awt = (MouseEvent) mbe.getAwtEvent();
        if(awt.getID() != MouseEvent.MOUSE_PRESSED)
            {
            return;
            }

        int mask = 77;
        mask = awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK;

        if((awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0)
            {
            ButtonId butt = mbe.getButton();
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) && myMouse.equals("Shift Left Mouse"))
                {
                goSit();
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) && myMouse.equals("Shift Middle Mouse"))
                {
                goSit();
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) && myMouse.equals("Shift Right Mouse"))
                {
                goSit();
                }
            }
        else if((awt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0)
            {
            ButtonId butt = mbe.getButton();
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) && myMouse.equals("Control Left Mouse"))
                {
                goSit();
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) && myMouse.equals("Control Middle Mouse"))
                {
                goSit();
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) && myMouse.equals("Control Right Mouse"))
                {
                goSit();
                }
            }
        else
            {
            ButtonId butt = mbe.getButton();
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1))
                {
                if(myMouse.equals("Left Mouse"))
                    {
                    goSit();
                    }
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) && myMouse.equals("Middle Mouse"))
                {
                goSit();
                }
            if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) && myMouse.equals("Right Mouse"))
                {
                goSit();
                }
            }
        }

    public void goSit()
        {
        ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity mye = ret.getEntity();
        RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
        localNode = rc.getSceneRoot();

        Vector3f v3f = localNode.getLocalTranslation();
        Quaternion quat = localNode.getLocalRotation();

        Vector3f v3fa = localNode.getWorldTranslation();
        Quaternion quata = localNode.getWorldRotation();
        //System.out.println("Model local - trans = " + v3f + " - local rot = " + quat);
        //System.out.println("Model world - trans = " + v3fa + " - world rot = " + quata);

        Vector3f axis = new Vector3f();
        float angle;
//        angle = quat.toAngleAxis(axis);
        angle = quata.toAngleAxis(axis);

        if(axis.y < 0.0f)
            angle = -angle;

        float xRotationInc = (float)Math.sin(angle + ((heading / 180) * 3.14159)) * 10;
        float zRotationInc = (float)Math.cos(angle + ((heading / 180) * 3.14159)) * 10;

        float xSittingInc = (float)Math.sin(angle + ((heading / 180) * 3.14159)) * offset;
        float zSittingInc = (float)Math.cos(angle + ((heading / 180) * 3.14159)) * offset;

//        SittingChair ac = new SittingChair(new Vector3f(v3f.x + xSittingInc, 0.0f, v3f.z + zSittingInc), new Vector3f(xRotationInc, 0.0f, zRotationInc));
        SittingChair ac = new SittingChair(new Vector3f(v3fa.x + xSittingInc, v3fa.y, v3fa.z + zSittingInc), new Vector3f(xRotationInc, 0.0f, zRotationInc));

        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();

        if (logger.isLoggable(Level.INFO)) {
            logger.info(" avatar X = " + myAvatar.getPositionRef().getX() + " - Y = " + myAvatar.getPositionRef().getY() + " - Z = " + myAvatar.getPositionRef().getZ());
        }

        GameContext context = myAvatar.getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(new GoSit(ac, (AvatarContext) context));
        }

    @Override
    public void setClientState(CellComponentClientState clientState)
        {
        super.setClientState(clientState);
        
        mouseEnable = ((SittingCellComponentClientState)clientState).getMouseEnable();
        heading = ((SittingCellComponentClientState)clientState).getHeading();
        offset = ((SittingCellComponentClientState)clientState).getOffset();
        myMouse = ((SittingCellComponentClientState)clientState).getMouse();
        }

    @Override
    protected void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        logger.info("Setting status on SittingCellComponent to " + status);
        switch(status)
            {
            case DISK:
                {
                if(logger.isLoggable(Level.FINE))
                    {
                    logger.fine("SittingComponent - DISK - increasing = " + increasing);
                    }
                break;
                }
            case INACTIVE:
                {
                if(logger.isLoggable(Level.FINE))
                    {
                    logger.fine("SittingComponent - INACTIVE - increasing = " + increasing);
                    }
                break;
                }
            case VISIBLE:
                {
                if(logger.isLoggable(Level.FINE))
                    {
                    logger.fine("SittingComponent - VISIBLE - increasing = " + increasing);
                    }
                break;
                }
            case RENDERING:
                {
/* Get local node */
                if(increasing)
                    {
                    if(logger.isLoggable(Level.FINE))
                        {
                        logger.fine("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = RENDERING - increasing ");
                        }
                    if(myListener == null)
                        {
                        ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                        Entity mye = ret.getEntity();

                        myListener = new MouseEventListener();
                        myListener.addToEntity(mye);
                        }
                    if (menuFactory == null)
                        {
                        final MenuItemListener l = new MenuItemListener();
                        menuFactory = new ContextMenuFactorySPI()
                            {
                            public ContextMenuItem[] getContextMenuItems(ContextEvent event)
                                {
                                return new ContextMenuItem[]
                                    {
                                    new SimpleContextMenuItem(BUNDLE.getString("Sit_Here"), l)
                                    };
                                }
                            };
                        contextComp.addContextMenuFactory(menuFactory);
                        }
                    }
                else
                    {
                    if(myListener != null)
                        {
                        ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                        Entity mye = ret.getEntity();
                        
                        myListener.removeFromEntity(mye);
                        myListener = null;
                        }
                    if (menuFactory != null)
                        {
                        contextComp.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                        }
//                    BasicRenderer br = (BasicRenderer) cell.getCellRenderer(RendererType.RENDERER_JME);
//                    br.setCollisionEnabled(true);
                    }
                break;
                }
            case ACTIVE:
                {
                if(logger.isLoggable(Level.FINE))
                    {
                    logger.fine("SittingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE - increasing = " + increasing);
                    }
                }
            default:
                {
                if(logger.isLoggable(Level.FINE))
                    {
                    logger.fine("SittingComponent : Cell " + cell.getCellID() + " : In default for setStatus - status other than ACTIVE");
                    }
                }
            }

        }
    class MouseEventListener extends EventClassListener
        {
        @Override
        public Class[] eventClassesToConsume()
            {
            return new Class[]{MouseButtonEvent3D.class};
            }

        @Override
        public void commitEvent(Event event)
            {
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for mouse event");
            }

        @Override
        public void computeEvent(Event event)
            {
            if(mouseEnable)
                {
                MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
                testMouse(mbe);
                }
            }
        }

    class MenuItemListener implements ContextMenuActionListener
        {
        public void actionPerformed(ContextMenuItemEvent event)
            {
            goSit();
            }
        }


    }
