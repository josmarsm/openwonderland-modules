/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import com.jme.renderer.ColorRGBA;
import java.awt.Canvas;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.SceneWorker;

/**
 *
 * @author jkaplan
 */
public enum GraphicsUtils {
    INSTANCE (320, 320);
        
    private final WorldManager worldManager;
    private final RenderBuffer rb;
    private final Canvas canvas;
    private final WorkProcessor workProcessor;   
    
    private ModelTestFrame frame;
    
    GraphicsUtils(int width, int height) {
        worldManager = new WorldManager("ClientTest");

        // The Rendering Canvas
        rb = worldManager.getRenderManager().createRenderBuffer(
                RenderBuffer.Target.ONSCREEN, width, height);
        rb.setBackgroundColor(ColorRGBA.white);
                
        worldManager.getRenderManager().addRenderBuffer(rb);
            
        canvas = ((OnscreenRenderBuffer)rb).getCanvas();
        canvas.setVisible(true);
        canvas.setBounds(0, 0, width, height);
        
        workProcessor = new WorkProcessor("ClientTestWorkProcessor", worldManager);
        Entity e = new Entity("ClientTestWorkProcessor");
        e.addComponent(WorkProcessor.class, workProcessor);
        worldManager.addEntity(e);
    }
    
    public static WorldManager getWorldManager() {
        return INSTANCE.worldManager;
    }
    
    public static Canvas getCanvas() {
        return INSTANCE.canvas;
    }
    
    public static RenderBuffer getRenderBuffer() {
        return INSTANCE.rb;
    }
    
    public static void workCommit(WorkCommit work) {
        INSTANCE.workProcessor.addWorker(work);
    }
    
    public static ModelTestFrame getFrame() {
        return INSTANCE.getFrameInternal();
    }
    
    private synchronized ModelTestFrame getFrameInternal() {
        if (frame == null) {
            Runnable createFrame = new Runnable() {
                public void run() {
                    frame = new ModelTestFrame();
                    frame.setTestPane(getCanvas());
                    frame.pack();
                
                    synchronized (GraphicsUtils.this) {
                        GraphicsUtils.this.notifyAll();
                    }
                }
            };
            
            // are we on the AWT event thread already?
            if (SwingUtilities.isEventDispatchThread()) {
                createFrame.run();
            } else {
                // jump onto AWT event thread
                SwingUtilities.invokeLater(createFrame);
            
                try {
                    while (frame == null) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                }
            }
        }
         
        return frame;
    }
}
