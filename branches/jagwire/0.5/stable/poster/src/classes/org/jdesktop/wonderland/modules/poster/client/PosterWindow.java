/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.poster.client;

import com.jme.math.Vector2f;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 *
 * The window for the poster app.
 * @author Bernard Horan
 * @author Jon Kaplan
 *
 */
public class PosterWindow extends WindowSwing {

    /** The cell in which this window is displayed. */
    private final PosterCell cell;

    /** The panel displayed within this window. */
    private PosterPanel posterPanel;

    /** The poster node to render the label */
    private final PosterNode posterNode;

    /**
     * Create a new instance of PollWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @throws InstantiationException
     */
    public PosterWindow(final PosterCell cell, App2D app, int width,
            int height, boolean decorated, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, decorated, pixelScale);
        this.cell = cell;

        setTitle("Poster");
        View2D view = getView(cell);
        posterNode = new PosterNode(view);
        view.setGeometryNode(posterNode);

        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    // This must be invoked on the AWT Event Dispatch Thread
                    posterPanel = new PosterPanel(cell);
                    
                    // Parent to Wonderland main window for proper focus handling
                    JmeClientMain.getFrame().getCanvas3DPanel().add(posterPanel);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        setComponent(posterPanel);
    }

    @Override
    protected void viewInit(View2D view) {
        super.viewInit(view);
        view.addEventListener(new FakeMouseEventListener(this, view));
    }
    
    void updateLabel() {
        posterNode.setBillboard(cell.getBillboardMode());
        posterPanel.updateLabel();
    }

    /**
     * Listener that passes fake mouse events to the poster pane. This
     * allows the poster to react to mouseovers and clicks even though
     * we don't have control of the poster
     */
    private static class FakeMouseEventListener extends EventClassListener {
        private final PosterWindow window;
        private final View2D view;
        
        public FakeMouseEventListener(PosterWindow window, View2D view) {
            this.window = window;
            this.view = view;
}
        
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseEvent3D.class };
        }

        @Override
        public boolean consumesEvent(Event event) {
            if (!super.consumesEvent(event)) {
                return false;
            }

            MouseEvent3D me = (MouseEvent3D) event;
            return me.getID() == MouseEvent.MOUSE_MOVED  ||
                   me.getID() == MouseEvent.MOUSE_EXITED ||
                   me.getButton() == MouseEvent3D.ButtonId.BUTTON1;
        }

        @Override
        public void computeEvent(Event event) {
        }

        @Override
        public void commitEvent(Event event) {
            MouseEvent3D me = (MouseEvent3D) event;
            MouseEvent e = (MouseEvent) me.getAwtEvent();
            int id = e.getID();
            
            
            Point p = new Point(-1, -1);
            if (e.getID() != MouseEvent.MOUSE_EXITED) {
                // for a click or move event, use the real coordinates
                p = view.calcPositionInPixelCoordinates(me.getIntersectionPointWorld(), 
                                                        true);
            } else {
                // for an exit event, create a fake point outside the bounds
                // of the panel. We change the type to a MOUSE_MOVED event
                // since the listener in HTMLEditorKit only pays attention
                // to mouse movement.
                id = MouseEvent.MOUSE_MOVED;
                p = new Point(-1, -1);
            }
            
            // create a fake mouse event at the correct pixel coordinates, and
            // send it to the JEditorPane for processing
            final MouseEvent fake = new MouseEvent(
                    window.posterPanel.getPosterPane(),
                    id, e.getWhen(), e.getModifiers(),
                    (int) p.getX(), (int) p.getY(), e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
                        
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    window.posterPanel.getPosterPane().dispatchEvent(fake);
                }
            });
        }
    }
}
