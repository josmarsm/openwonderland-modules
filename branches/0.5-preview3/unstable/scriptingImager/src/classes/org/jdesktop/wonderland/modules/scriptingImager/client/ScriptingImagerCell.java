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
package org.jdesktop.wonderland.modules.scriptingImager.client;

import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.scriptingImager.client.jme.cellrenderer.ScriptingImagerCellRenderer;
import org.jdesktop.wonderland.modules.scriptingImager.common.ScriptingImagerCellClientState;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingActionClass;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingRunnable;
/**
 * Client Cell for a whiteboard shared application.
 *
 * @author nsimpson,deronj
 */
@ExperimentalAPI
public class ScriptingImagerCell extends Cell {
    
    @UsesCellComponent
    private ScriptingComponent scriptingComponent;

    /* The image uri to use */
    private String imageURI = null;
    private ScriptingImagerCellRenderer  icr = null;
//    private MouseEventListener listener = null;
    private CellID  cellID;
    /**
     * Create an instance of ImageViewerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public ScriptingImagerCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        this.cellID = cellID;
    }

    /**
     * Initialize the imager with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        imageURI = ((ScriptingImagerCellClientState)clientState).getImageURI();
    }

    /**
     * Returns the image uri
     */
    public String getImageURI() {
        return imageURI;
    }



    @Override
    public void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        switch (status)
            {
            case DISK:
//                if (listener != null)
//                    {
//                    listener.removeFromEntity(icr.getEntity());
//                    listener = null;
//                    }
                break;

            case ACTIVE:
                {
//                if (listener == null)
//                    {
//                    listener = new MouseEventListener();
//                    listener.addToEntity(icr.getEntity());
                System.out.println("Inside ACTIVE in scriptingImager");
//                    }
                if(increasing == true)
                    {
                    ScriptingActionClass sac = new ScriptingActionClass();
                    sac.setName("Imager");
                    sac.insertCmdMap("testit", testitRun);

                    sac.insertCmdMap("exposeImage", exposeImageRun);
                    sac.insertCmdMap("exposeNext", exposeNextRun);
                    sac.insertCmdMap("exposePrevious", exposePreviousRun);
/*
                sac.insertCmdMap("startBack", avatarStartBackRun);
            sac.insertCmdMap("stopBack", avatarStopBackRun);
            sac.insertCmdMap("startLeft", avatarStartLeftRun);
            sac.insertCmdMap("stopLeft", avatarStopLeftRun);
            sac.insertCmdMap("startRight", avatarStartRightRun);
            sac.insertCmdMap("stopRight", avatarStopRightRun);
            sac.insertCmdMap("startUp", avatarStartUpRun);
            sac.insertCmdMap("stopUp", avatarStopUpRun);
            sac.insertCmdMap("startDown", avatarStartDownRun);
            sac.insertCmdMap("stopDown", avatarStopDownRun);
 */     
                    scriptingComponent.putActionObject(sac);
                    }
                break;
                }
            default:
                break;
            }
        }

    public void testit(int a)
        {
        System.out.println("testit a = " + a);
        }

    ScriptingRunnable testitRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            testit(a);
            System.out.println("ScriptingActionClass - enter testit");
            }
        };
    

    public void exposeImage(int a)
        {
        System.out.println("exposeImage a = " + a);
        icr.exposeImage(a);
        }

    ScriptingRunnable exposeImageRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            exposeImage(a);
            }
        };


    public void exposeNext()
        {
        System.out.println("exposeNext");
        icr.exposeNext();
        }

    ScriptingRunnable exposeNextRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            exposeNext();
            }
        };


    public void exposePrevious()
        {
        System.out.println("exposePrevious");
        icr.exposePrevious();
        }

    ScriptingRunnable exposePreviousRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            exposePrevious();
            }
        };

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType)
        {
        if (rendererType == RendererType.RENDERER_JME) 
            {
            icr = new ScriptingImagerCellRenderer(this);
            return icr;
            }
        else
            {
            return super.createCellRenderer(rendererType);
            }
        }
/*
    class MouseEventListener extends EventClassListener
        {
        @Override
        public Class[] eventClassesToConsume()
            {
            return new Class[]{MouseButtonEvent3D.class};
            }

        @Override
        public void computeEvent(Event event)
            {
            System.out.println("Enter computeEvent");
            }
        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event)
            {

            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            MouseEvent awt = (MouseEvent) mbe.getAwtEvent();
            if(awt.getID() != MouseEvent.MOUSE_PRESSED)
                {
                return;
                }


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
//                    System.out.println("**********    Event for Mouse 1");
                    icr.exposeNext();
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON2) )
                    {
//                    System.out.println("**********    Event for Mouse 2");
                    }
                if (awt.getID()== MouseEvent.MOUSE_PRESSED && (butt == ButtonId.BUTTON3) )
                    {
                    icr.exposePrevious();
//                    System.out.println("**********    Event for Mouse 3");
                    }
                }
           }

        }
*/
    }
