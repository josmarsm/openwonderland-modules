/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

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

package org.jdesktop.wonderland.modules.EZClick.client;

import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager.WindowSwingEventConsumer;
import org.jdesktop.wonderland.client.input.InputManager.WindowSwingEventConsumer.EventAction;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 * Take/release control of application with a single click
 * 
 * @author JagWire
 */
public class EZClickComponent extends CellComponent implements CapabilityBridge {

    private static final Logger logger = Logger.getLogger(EZClickComponent.class.getName());
    private EZWindowSwingEventConsumer eventConsumer;
    private Map<View2D, WindowSwingEventConsumer> priorConsumers = new HashMap<View2D, WindowSwingEventConsumer>();
    EZClickMouseListener mouseListener=null;

    public EZClickMouseListener getMouseEventListener() {
        return mouseListener;
    }
    
    public EZClickComponent(Cell cell) {
        super(cell);
    }

    public Entity getCellEntity() {
        BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        r.setCollisionEnabled(true);
        r.setPickingEnabled(true);
        return r.getEntity();
    }

    public Node getCellSceneRoot() {
        BasicRenderer r =(BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        return r.getSceneRoot();
    }

    /**
     *  Grab a list of candidate views.
     * @return
     */
    public List<View2D> getView2D() {
        List<View2D> candidateViews = new ArrayList();
        if(((App2DCell)cell).getApp() == null) {
            
            logger.info("* App is null! *");
            return candidateViews;
        }
        else if(((App2DCell)cell).getApp().getPrimaryWindow() == null) {
            logger.info("* Primary window is null! *");
        }

        Iterator<Window2D> i = ((App2DCell)cell).getApp().getWindows();
        if(i.hasNext() == false) {
            logger.info("* No windows! *");
        }
        else {
            //loop through all windows
            while(i.hasNext()) {
               logger.info("* Candidate window! *");
               Iterator<View2D> vs = i.next().getViews();
               //loop through all views in this window
               while(vs.hasNext()) {
                   View2D view = vs.next();
                   if(view != null) {
                        logger.info("* Candidate view! *");
                        candidateViews.add(view);
                    //return view;
                    }
                    else {
                        logger.info("* View is null! *");
                    }
               }
            }

        }
        
        if(candidateViews.isEmpty()) {
            candidateViews.add(((App2DCell)cell).getApp().getPrimaryWindow().getView((App2DCell)cell));
        }
        
        logger.log(Level.INFO, "{0} XXX candidate views found!", candidateViews.size());
        return candidateViews;
        
    }

    public App2D getApp2D() {
        return ((App2DCell)cell).getApp();
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if(status == CellStatus.INACTIVE && increasing == false) {
            
            if (mouseListener != null) {
                CellRenderer cellRenderer =
                        cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                CellRendererJME renderer = (CellRendererJME) cellRenderer;
                Entity entity = renderer.getEntity();
                mouseListener.removeFromEntity(entity);
                mouseListener = null;
            }
            if(eventConsumer != null) {

                logger.info("Removing Event Consumer.");
                //for every view2D in this cell...                               
//                for(View2D view: getView2D()) {
//                    //remove both our consumer and the WindowSwingEventConsumer...
//                    view.removeEntityComponent(EZWindowSwingEventConsumer.class);
//                    view.removeEntityComponent(WindowSwingEventConsumer.class);
//                    
//                    //retrieve any old consumer that EZCick replaced for this
//                    //view2d and put it back in place to cover out tracks.
//                    WindowSwingEventConsumer tmp = priorConsumers.get(view);
//                    
//                    //apparently tmp can be null...check to make sure.
//                    if(tmp != null) {
//                        view.addEntityComponent(WindowSwingEventConsumer.class, tmp);
//                    }                                        
//                }
//
//
////                System.out.println("SwingEventConsumer removed.");
//                if(eventConsumer.getInternal() != null) {
//                    InputManager3D.getInputManager().removeGlobalEventListener(
//                            eventConsumer.getInternal());
//                }

                //eventConsumer = null;
                mouseListener = null;
            }
        }
        else if (status == CellStatus.RENDERING && increasing == true) {

            if(eventConsumer == null) {

                logger.info("Attaching Event consumer.");

//                eventConsumer = new EZWindowSwingEventConsumer(getApp2D());
//                //for every view2d in this cell...
//                for(View2D view: getView2D()) {
//                    //remove and store any existing event consumers for reattachment
//                    //when this component is removed.
//                    
//                    priorConsumers.put(view, ((View2DCell)view).getEntity().getComponent(WindowSwingEventConsumer.class));
//                    view.removeEntityComponent(WindowSwingEventConsumer.class);
//                    view.addEntityComponent(WindowSwingEventConsumer.class,
//                                        eventConsumer);
////                    System.out.println("SwingEventConsumer added.");
//                }           
                
                
                //Attach a click listener
                if(mouseListener == null) {
                    CellRenderer cellRenderer =
                            cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    CellRendererJME renderer =
                            (CellRendererJME) cellRenderer;
                    Entity entity = renderer.getEntity();
                    mouseListener = new EZClickMouseListener();
                    mouseListener.addToEntity(entity);
                }
            }
        }

//        logger.info("Setting status on EZClickComponent to " + status);
    }
    /**
     * Class to check for
     */
    class EZClickMouseListener extends EventClassListener {

        private EZClickMouseListenerInternal internal;
        public EZClickMouseListener() {
            internal = new EZClickMouseListenerInternal();
        }
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            logger.info("Event triggered");
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            
            if(mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            logger.info("Candidate mouse event received");
            //only if a shared app
            if(cell instanceof App2DCell){
                logger.info("Taking control of app.");
                App2DCell mycell = (App2DCell)cell;
                
                /* Improved Check */
                ControlArb controlArb = mycell.getApp().getControlArb();
                if(controlArb != null){
                    if(controlArb.hasControl()){
                        controlArb.releaseControl();
                    }else{
                        ControlArb.releaseControlAll();
                        controlArb.takeControl();
                        internal = new EZClickMouseListenerInternal();
                        InputManager3D.getInputManager().addGlobalEventListener(
                            internal);
                       
                    }
                }
                 
                //remove highlight if object has
                mycell.getApp().getControlArb().addListener(new ControlArb.ControlChangeListener() {
                    public void updateControl(ControlArb ca) {
                        if(ca.hasControl()) {
                            removehighlight(cell);
                        }
                    }
                });
                
                /*
                mycell.getApp().getPrimaryWindow().getView(mycell);
                mycell.getApp().getControlArb().takeControl();
                this.removeFromEntity(mycell.getApp().getFocusEntity());
                logger.info("Adding global event listener");
                InputManager3D.getInputManager().addGlobalEventListener(
                    internal);
                logger.info("Listener for taking control added");
                */
            }
            else{
                //do nothing.
            }
        }

        public EZClickMouseListenerInternal getInternal() {
            return internal;
        }
    }

    
    /**
     * Class to check for mouse clicks not in the current application
     */
    class EZClickMouseListenerInternal extends EventClassListener {
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class,MouseMovedEvent3D.class };

        }
        public EZClickMouseListenerInternal getThisInstance() {
            return this;
        }
                
        @Override
        public void commitEvent(Event event) {
            logger.info("EZClick : Internal Listener for releasing control");
            
            //remove highlight if still it's there
            if(event instanceof MouseMovedEvent3D) {
                removehighlight(cell);
                return;
            }
            
            final MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Cell otherCell = SceneManager.getCellForEntity(mbe.getEntity());
                    if( otherCell == null){
                        App2DCell appcell = (App2DCell)cell;
                        logger.info("Releasing control");
                        if(appcell == null || appcell.getApp() == null) {
                            //die gracefully.
                            InputManager3D.getInputManager().removeGlobalEventListener(getThisInstance());
                            return;
                        }
                        appcell.getApp().getControlArb().releaseControl();
                        InputManager3D.getInputManager().removeGlobalEventListener(getThisInstance());
                        //System.out.println("Reattaching EZClickMouseListener");
                    }
                    else if( otherCell.getCellID() != cell.getCellID()) {
                        App2DCell appcell = (App2DCell)cell;
                        logger.info("Releasing control");

                        if(appcell == null || appcell.getApp() == null) {
                            //die gracefully.
                            InputManager3D.getInputManager().removeGlobalEventListener(getThisInstance());
                            return;
                        }
                        appcell.getApp().getControlArb().releaseControl();
                        InputManager3D.getInputManager().removeGlobalEventListener(getThisInstance());
                        //System.out.println("Reattaching EZClickMouseListener");
                    // listener.addToEntity(appcell.getApp().getFocusEntity());
                    }
                }
            });
            
        }
    }

    public WindowSwingEventConsumer getEventConsumer() {
        return eventConsumer;
    }
    
    class EZWindowSwingEventConsumer extends WindowSwingEventConsumer {

        private App2D app;
        private EZClickMouseListenerInternal internal;
        private EZWindowSwingEventConsumer (App2D app) {
            this.app = app;
        }

        public EZClickMouseListenerInternal getInternal() {
            return internal;
        }
        public boolean isEZClickChangeControlEvent(MouseEvent me) {

            return me.getID() == MouseEvent.MOUSE_PRESSED &&
//                me.getButton() == MouseEvent.BUTTON1;//  &&
                  //FIX FOR EZMOVE
                  me.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK;
                    
                    
                    
                    
              // (me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
            //return false;
        }
        
        public EventAction consumesEvent (MouseEvent3D me3d) {
            return EventAction.CONSUME_2D;
        }
        
        /*public EventAction consumesEvent (MouseEvent3D me3d) {
            
            
            
            
            if (app == null) return EventAction.DISCARD;

            MouseEvent awtEvent = (MouseEvent) me3d.getAwtEvent();
           
            if (InputManager3D.entityHasFocus(me3d, app.getFocusEntity())) {
    
                return EventAction.CONSUME_2D;
            }
       
            if (isEZClickChangeControlEvent(awtEvent)) {
                System.out.println("EZClick : consumesEvent");
                System.out.println("Is Change Control Event");

                // Perform the control toggle immediately
                ControlArb controlArb = app.getControlArb();
                if (controlArb != null) {
                    if (controlArb.hasControl()) {
                        controlArb.releaseControl();
                    } else {
                        ControlArb.releaseControlAll();
                        controlArb.takeControl();
                        System.out.println("Adding global event listener");
                        internal = new EZClickMouseListenerInternal();
                        InputManager3D.getInputManager().addGlobalEventListener(
                            internal);
                    }
                }
                return EventAction.DISCARD;
            }

            if (app.getControlArb() == null || !app.getControlArb().hasControl()) {
 
                return EventAction.DISCARD;
            }
  
            return EventAction.DISCARD;
        }*/
    }
    
    //remove highlight if glow is enable
    public void removehighlight(final Cell cell) {
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        Entity entity = r.getEntity();
        RenderComponent rc = entity.getComponent(RenderComponent.class);
        if (rc == null) {
            return;
        }
        TreeScan.findNode(rc.getSceneRoot(), Geometry.class, new ProcessNodeInterface() {
            public boolean processNode(final Spatial s) {
                if(s.isGlowEnabled()) {
                    s.setGlowEnabled(false);
                }
                ClientContextJME.getWorldManager().addToUpdateList(s);
                return true;
            }
        }, false, false);
    } 
    
}
