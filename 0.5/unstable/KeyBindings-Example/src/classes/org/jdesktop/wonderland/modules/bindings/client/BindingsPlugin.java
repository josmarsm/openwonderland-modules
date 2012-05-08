/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.bindings.client;

import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.MainFrameImpl;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.bindings.ActionBindingContext;
import org.jdesktop.wonderland.client.jme.input.bindings.Binder;
import org.jdesktop.wonderland.client.jme.input.bindings.ContextFactory;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;

/**
 *
 * @author JagWire
 */
@Plugin
public class BindingsPlugin extends BaseClientPlugin {

    private static final Logger logger = Logger.getLogger(BindingsPlugin.class.getName());

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
                MainFrameImpl mainFrame = (MainFrameImpl) JmeClientMain.getFrame();
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



        binder.bindKeyStroke(KeyStroke.getKeyStroke("pressed P"), "start forward");

        binder.bindAction("start forward", new ActionSPI() {

            public String getName() {
                return "move forward";
            }

            public <T extends InputEvent3D> void performAction(T event) {
                logger.warning("starting to move forward!");
                ViewCell cell = ViewManager.getViewManager().getPrimaryViewCell();
                AvatarImiJME r = (AvatarImiJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                r.getAvatarCharacter().getContext().triggerPressed(TriggerNames.Move_Forward.ordinal());

            }
        });

        binder.bindKeyStroke(KeyStroke.getKeyStroke("released P"), "stop forward");

        binder.bindAction("stop forward", new ActionSPI() {

            public String getName() {
                return "stop moving forward";
            }

            public <T extends InputEvent3D> void performAction(T event) {
                logger.warning("stopping forward movement");
                ViewCell cell = ViewManager.getViewManager().getPrimaryViewCell();
                AvatarImiJME r = (AvatarImiJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                r.getAvatarCharacter().getContext().triggerReleased(TriggerNames.Move_Forward.ordinal());
            }
        });

    }

    @Override
    public void deactivate() {
    }
}
