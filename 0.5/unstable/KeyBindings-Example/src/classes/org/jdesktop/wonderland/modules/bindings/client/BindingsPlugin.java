/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.bindings.client;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.MainFrameImpl;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.Binder;
import org.jdesktop.wonderland.client.jme.input.bindings.ContextFactory;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author JagWire
 */
@Plugin
public class BindingsPlugin extends BaseClientPlugin {
    
    
    @Override
    public void activate() {
        ActionBindingContext context = ContextFactory.INSTANCE.getContext("main");
                Binder binder = context.getBinder();
                
                binder.bindKeyStroke(KeyStroke.getKeyStroke('c'), "cycleCamera"); 
                binder.bindAction("cycleCamera", new ActionSPI() { 
                    public String getName() {
                        return "";
                    }

                    public <T extends InputEvent3D> void performAction(T event) {
                        MainFrameImpl mainFrame = (MainFrameImpl)JmeClientMain.getFrame();
//                        cameraButtonGroup.next();
                        mainFrame.cycleCamera();
                    }
                });

                binder.bindKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "resetCamera");
                binder.bindAction("resetCamera", new ActionSPI() { 


                    public String getName() {
                        return "resetCamera";
                    }

                    public <T extends InputEvent3D> void performAction(T event) {
                        ViewManager.getViewManager().setCameraController(
                                ViewManager.getDefaultCamera());
                    }
                });
    }
    
    @Override
    public void deactivate() {
        
    }
}
