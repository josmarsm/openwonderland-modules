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

package org.jdesktop.wonderland.modules.sitting.client;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import imi.character.avatar.AvatarContext;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoSit;
import imi.character.statemachine.GameContext;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
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
    private String info = null;
    private int traceLevel = 5;
    private MouseEventListener myListener = null;
    private WlAvatarCharacter myAvatar;
    private   CellRendererJME ret = null;
    private Node localNode = null;

    public SittingCellComponent(Cell cell)
        {
        super(cell);
        }

    public void goSit()
        {
        ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity mye = ret.getEntity();
        RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
        localNode = rc.getSceneRoot();
        Vector3f v3f = localNode.getLocalTranslation();

        SittingChair ac = new SittingChair(new Vector3f(v3f.x, 0.0f, v3f.z), new Vector3f(1.0f, 0.0f, 1.0f));

        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();
        if(traceLevel > 3)
            {
            System.out.println(" avatar X = " + myAvatar.getPositionRef().getX() + " - Y = " + myAvatar.getPositionRef().getY() + " - Z = " + myAvatar.getPositionRef().getZ());
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
        info = ((SittingCellComponentClientState)clientState).getInfo();
        }

    @Override
    protected void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        logger.warning("Setting status on SittingCellComponent to " + status);
        switch(status)
            {
            case DISK:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("SittingComponent - DISK - increasing = " + increasing);
                    }
                break;
                }
            case INACTIVE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("SittingComponent - INACTIVE - increasing = " + increasing);
                    }
                break;
                }
            case VISIBLE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("SittingComponent - VISIBLE - increasing = " + increasing);
                    }
                break;
                }
            case RENDERING:
                {
/* Get local node */
                if(increasing)
                    {
                    if(traceLevel > 4)
                        {
                        System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : setStatus = RENDERING - increasing ");
                        }
                    if(myListener == null)
                        {
                        CellRendererJME ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                        Entity mye = ret.getEntity();

                        myListener = new MouseEventListener();
                        myListener.addToEntity(mye);
                        }
                    }
                else
                    {
                    if(myListener != null)
                        {
                        CellRendererJME ret = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                        Entity mye = ret.getEntity();
                        
                        myListener.removeFromEntity(mye);
                        myListener = null;
                        }
                    }
                break;
                }
            case ACTIVE:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("SittingComponent : Cell " + cell.getCellID() + " : setStatus = ACTIVE - increasing = " + increasing);
                    }
                }
            default:
                {
                if(traceLevel > 4)
                    {
                    System.out.println("SittingComponent : Cell " + cell.getCellID() + " : In default for setStatus - status other than ACTIVE");
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
//            System.out.println("ScriptingComponent : Cell " + cell.getCellID() + " : In commitEvent for mouse event");
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
//            if (mbe.isClicked() == false)
//                {
//                return;
//                }
            MouseEvent awt = (MouseEvent) mbe.getAwtEvent();
            if(awt.getID() != MouseEvent.MOUSE_PRESSED)
                {
                return;
                }

            int mask = 77;
            mask = awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK;
//            System.out.println("Condition = " + mask + " Button = " + mbe.getButton() + " awt.id = " + awt.getID());

            if((awt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the shift down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and shift");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and shift");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse3 and shift");
                    }
                }
            else if((awt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the control down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and control");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and control");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3 and control");
                    }
                }
            else if((awt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) > 0)
                {
//                System.out.println("Inside the alt down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
//                    System.out.println("**********    Event for Mouse 1 and alt");
//                    System.out.println("Inside button 1 test");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2 and alt");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3 and alt");
                    }
                }

            else
                {
//                System.out.println("Inside the no shift down mask test");
                ButtonId butt = mbe.getButton();
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON1) )
                    {
                    System.out.println("**********    Event for Mouse 1");
                    goSit();
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
//                    System.out.println("**********    Event for Mouse 3");
                    }
                }
           }
        }

    }
