/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.*;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.FriendlyErrorInfoSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.EventBridge;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyJavaErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.errorinfo.DefaultFriendlyJavascriptErrorInfo;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.BridgeGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.MethodGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.ReturnableMethodGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.loaders.OptimizedLoader;

/**
 *
 * @author JagWire
 */
public class ScriptManager {
//    INSTANCE;

    private ScriptEditorPanel scriptEditor;
    private JDialog dialog;
    private ScriptEditorPanel panel;
    private ScriptEngineManager engineManager;// = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngine scriptEngine = null;
    private Bindings scriptBindings = null;
    private static final Logger logger = Logger.getLogger(ScriptManager.class.getName());
    //utilities
    private Map<String, CellID> stringToCellID;
    private static ScriptManager instance;

    public static ScriptManager getInstance() {
        if (instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

//    public static ScriptManager getInstance() {
//        return INSTANCE;
//    }
    private ScriptManager() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog = new JDialog();
                panel = new ScriptEditorPanel(dialog);
                dialog.setTitle("Script Editor - Wonderland Client");
                //2. Optional: What happens when the frame closes?
                dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

                //3. Create component and put them in the frame.
                dialog.setContentPane(panel);

                //4. Size the frame.
                dialog.pack();
            }
        });


        //Next, acquire the scripting magicry
//        engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
//        scriptEngine = engineManager.getEngineByName("JavaScript");
//       / scriptBindings = scriptEngine.createBindings();

        //Add the necessary script bindings
//        scriptBindings.put("Client", ClientContextJME.getClientMain());

        stringToCellID = new HashMap<String, CellID>();

        OptimizedLoader loader = new OptimizedLoader();
        loader.loadBindings();
        scriptBindings = loader.getBindings();
        scriptEngine = loader.getEngine();
//        scriptBindings.putAll(dao().getClientBindings());

//        scriptEngine = dao().getClientScriptEngine();
//        scriptEngine.setBindings(scriptBindings, ScriptContext.ENGINE_SCOPE);
//        INSTANCE.logger.warning("manager bindings size: "+scriptBindings.size());
        generateDocumentation();
    }

    private void generateDocumentation() {
        generateNonVoidDocumentation();
        generateVoidDocumentation();
        generateCellFactoryDocumentation();
    }

    private void generateNonVoidDocumentation() {

        for (final ReturnableScriptMethodSPI returnable : dao().getReturnables()) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(returnable);
                }
            });
        }
    }

    private void generateCellFactoryDocumentation() {

        for (CellFactorySPI factory : dao().getCellFactories()) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(returnable);
                }
            });
        }
    }

    private void generateVoidDocumentation() {

        for (final ScriptMethodSPI method : dao().getVoids()) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(method);
                }
            });
        }
    }

    private ScriptedObjectDataSource dao() {
//        ScriptedObjectDataSource.INSTANCE.initialize();

        return ScriptedObjectDataSource.INSTANCE;
    }

    private void bindScript(String script) {
        try {
            scriptEngine.eval(script, scriptBindings);
        } catch (ScriptException ex) {
            Logger.getLogger(ScriptManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void evaluate(String script) {
        try {
            scriptEngine.eval(script, scriptBindings);
        } catch (ScriptException ex) {
            processException(ex);
            ex.printStackTrace();
        }
    }

    public void processException(Exception e) {

        final ErrorWindow window;
        FriendlyErrorInfoSPI info = null;
        if (e.getMessage().contains("WrappedException")) {
            //WrappedException we = (WrappedException) e.getCause();
            //java issue
            info = new DefaultFriendlyJavaErrorInfo("Wonderland Client");

        } else if (e.getMessage().contains("EcmaError")) {
            info = new DefaultFriendlyJavascriptErrorInfo("Wonderland Client");
        } else {
            info = new DefaultFriendlyErrorInfo("Wonderland Client");
        }
        window = new ErrorWindow(info.getSummary(), info.getSolutions());
        TextAreaOutputStream output = new TextAreaOutputStream(window.getDetailsArea());
        e.printStackTrace(new PrintStream(output));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                HUDComponent component = mainHUD.createComponent(window);
                window.setHUDComponent(component);
                component.setDecoratable(true);
                component.setPreferredLocation(Layout.CENTER);

                mainHUD.addComponent(component);

                component.setVisible(true);
            }
        });
        logger.warning("Error in evaluation()!");
    }

    public void showScriptEditor() {
        dialog.setVisible(true);
    }

    public void addCell(Cell cell) {
        if (!stringToCellID.containsKey(cell.getName())) {
            stringToCellID.put(cell.getName(), cell.getCellID());
        } else {
            return; //return gracefully.
        }
    }

    public CellID getCellID(String name) {
        if (stringToCellID.containsKey(name)) {
            return stringToCellID.get(name);
        } else {
            return null;
        }
    }

    public void removeCell(Cell cell) {
        if (stringToCellID.containsKey(cell.getName())) {
            stringToCellID.remove(cell.getName());
        } else {
            return; //return gracefully
        }
    }

    public Collection<String> getRegisteredCellNames() {
        return stringToCellID.keySet();
    }
}