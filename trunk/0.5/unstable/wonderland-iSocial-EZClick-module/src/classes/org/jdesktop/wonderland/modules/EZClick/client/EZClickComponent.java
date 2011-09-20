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

import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager.WindowSwingEventConsumer;
import org.jdesktop.wonderland.client.input.InputManager.WindowSwingEventConsumer.EventAction;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.InputManager3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
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
public class EZClickComponent extends CellComponent {

    private static Logger logger = Logger.getLogger(EZClickComponent.class.getName());
    //private EZClickMouseListener listener = null;
    private EZWindowSwingEventConsumer eventConsumer;
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
            
            System.out.println("* App is null! *");
            return candidateViews;
        }
        else if(((App2DCell)cell).getApp().getPrimaryWindow() == null) {
            System.out.println("* Primary window is null! *");
        }

        Iterator<Window2D> i = ((App2DCell)cell).getApp().getWindows();
        if(i.hasNext() == false) {
            System.out.println("* No windows! *");
        }
        else {
            //loop through all windows
            while(i.hasNext()) {
               System.out.println("* Candidate window! *");
               Iterator<View2D> vs = i.next().getViews();
               //loop through all views in this window
               while(vs.hasNext()) {
                   View2D view = vs.next();
                   if(view != null) {
                        System.out.println("* Candidate view! *");
                        candidateViews.add(view);
                    //return view;
                    }
                    else {
                        System.out.println("* View is null! *");
                    }
               }
            }

        }
        
        if(candidateViews.size() == 0) {
            candidateViews.add(((App2DCell)cell).getApp().getPrimaryWindow().getView((App2DCell)cell));
        }
        
        System.out.println(candidateViews.size() + " XXX candidate views found!");
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
            if(eventConsumer != null) {
                System.out.println("Removing EZClickMouseListener");
                //getView2D().removeEntityComponent(EZWindowSwingEventConsumer.class);
                for(View2D view: getView2D()) {
                    view.removeEntityComponent(EZWindowSwingEventConsumer.class);
                }


                System.out.println("SwingEventConsumer removed.");
                if(eventConsumer.getInternal() != null) {
                    InputManager3D.getInputManager().removeGlobalEventListener(
                            eventConsumer.getInternal());
                }

                eventConsumer = null;
            }
        }
        else if (status == CellStatus.RENDERING && increasing == true) {
        //else if(status == CellStatus.ACTIVE && increasing == true) {
            if(eventConsumer == null) {
                System.out.println("Attaching EZClickMouseListener");

                //listener = new EZClickMouseListener();
                eventConsumer = new EZWindowSwingEventConsumer(getApp2D());
                for(View2D view: getView2D()) {
                    //remove any already in place event consumers...
                    view.removeEntityComponent(WindowSwingEventConsumer.class);
                    view.addEntityComponent(WindowSwingEventConsumer.class,
                                        eventConsumer);
                    System.out.println("SwingEventConsumer added.");
                }                                                            
            }
        }

        logger.warning("Setting status on EZClickComponent to " + status);
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
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            if(mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            System.out.println("Candidate mouse event received");
            //only if a shared app
            if(cell instanceof App2DCell) {
                System.out.println("Taking control of app.");
                App2DCell mycell = (App2DCell)cell;
                mycell.getApp().getPrimaryWindow().getView(mycell);
                mycell.getApp().getControlArb().takeControl();
                this.removeFromEntity(mycell.getApp().getFocusEntity());

                System.out.println("Adding global event listener");
                InputManager3D.getInputManager().addGlobalEventListener(
                    internal);
            }
            else {
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
            return new Class[] { MouseButtonEvent3D.class };

        }
        public EZClickMouseListenerInternal getThisInstance() {
            return this;
        }
        @Override
        public void commitEvent(Event event) {
            final MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Cell otherCell = SceneManager.getCellForEntity(mbe.getEntity());
                    if( otherCell == null){
                        App2DCell appcell = (App2DCell)cell;
                        System.out.println("Releasing control");
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
                        System.out.println("Releasing control");

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
                me.getButton() == MouseEvent.BUTTON1;//  &&
              // (me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
            //return false;
        }

        public EventAction consumesEvent (MouseEvent3D me3d) {
            if (app == null) return EventAction.DISCARD;

            MouseEvent awtEvent = (MouseEvent) me3d.getAwtEvent();
           // System.out.println("WS.consumesEvent: " + awtEvent);
            // If app has control and focus, send the event to Swing
            if (InputManager3D.entityHasFocus(me3d, app.getFocusEntity())) {
      //          System.out.println("App entity has focus");
                return EventAction.CONSUME_2D;
            }
        //    logger.fine("App entity doesn't have focus");


            if (isEZClickChangeControlEvent(awtEvent)) {
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
//            System.out.println("Isn't change control event " + awtEvent);

            // If app doesn't have control, ignore the event

            /*
             * Jagwire Note: Is the below clause needed?
             */

            if (app.getControlArb() == null || !app.getControlArb().hasControl()) {
  //              System.out.println("Doesn't have control");
                return EventAction.DISCARD;
            }
    //        System.out.println("Has control");
                                 
            return EventAction.DISCARD;
        }
    }
}
