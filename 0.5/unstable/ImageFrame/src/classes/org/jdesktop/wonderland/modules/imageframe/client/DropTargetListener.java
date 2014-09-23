/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellSelectionRegistry;
import org.jdesktop.wonderland.client.cell.utils.spi.CellSelectionSPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI.ImportResultListener;
import org.jdesktop.wonderland.client.jme.input.DropTargetDragEnterEvent3D;
import org.jdesktop.wonderland.client.jme.input.DropTargetDragExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.DropTargetDropEvent3D;
import org.jdesktop.wonderland.client.jme.input.DropTargetEvent3D;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateRequestMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateResponseMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import static org.jdesktop.wonderland.modules.imageframe.client.DropTargetListener.getFileName;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 * this file is to handle drag and drop event
 *
 */
public class DropTargetListener extends EventClassListener {

    public String extension;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/jme/content/Bundle");
    private static final Logger LOGGER
            = Logger.getLogger(DropTargetListener.class.getName());
    public Node root;
    public Quad quad;
    public Spatial.CullHint cullHint;
    public ImageFrameCell parentCell;

    private static final ColorRGBA HIGHLIGHT_COLOR = new ColorRGBA(ColorRGBA.yellow);
    private static final Vector3f HIGHLIGHT_SCALE = new Vector3f(1.05f, 1.05f, 1.05f);

    public DropTargetListener(Cell parentCell) {
        this.parentCell = (ImageFrameCell) parentCell;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean consumesEvent(Event event) {
        if (!super.consumesEvent(event)) {
            return false;
        }
        //event is an instance of DropTargetEvent3D
        if (event instanceof DropTargetDragEnterEvent3D
                || event instanceof DropTargetDragExitEvent3D) {
            return true;
        }
        DragAndDropManager dnd = DragAndDropManager.getDragAndDropManager();
        extension = dnd.getFileExtension((DropTargetEvent3D) event);
        if (extension != null) {
            return true;
        } else {
            CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
            Entity parent = ((CellRendererJME) renderer).getEntity();
            RenderComponent rc = parent.getComponent(RenderComponent.class);
            final Node sceneRoot = rc.getSceneRoot();
            Spatial backPanel = sceneRoot.getChild("Image Frame BackGround");
            //backPanel.setCullHint(Spatial.CullHint.Never);
            Spatial s = backPanel.getParent();
            s.setLocalTranslation(0f, 0f, 0f);
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                }
            });
            return false;
        }

    }

    public void highlight(final Entity entity, final boolean enabled) {
        RenderComponent rc = entity.getComponent(RenderComponent.class);
        TreeScan.findNode(rc.getSceneRoot(), new ProcessNodeInterface() {

            public boolean processNode(final Spatial s) {
                if (s.getName().equals("Image Frame quad")) {
                    s.setGlowEnabled(enabled);
                    s.setGlowColor(HIGHLIGHT_COLOR);
                    s.setGlowScale(HIGHLIGHT_SCALE);
                    ClientContextJME.getWorldManager().addToUpdateList(s);
                }
                return true;
            }
        });
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{
            DropTargetDropEvent3D.class, DropTargetDragEnterEvent3D.class, DropTargetDragExitEvent3D.class
        };
    }

    @Override
    public boolean propagatesToParent(Event event) {
        return false;
    }

    @Override
    public void computeEvent(Event event) {
        try {
            if (event instanceof DropTargetDragEnterEvent3D) {
                CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
                Entity parent = ((CellRendererJME) renderer).getEntity();
                RenderComponent rc = parent.getComponent(RenderComponent.class);
                final Node sceneRoot = rc.getSceneRoot();
                Quaternion rot = new Quaternion();
                rot.fromAngles(0f, 0f, 0f);
                Spatial backPanel = sceneRoot.getChild("Image Frame BackGround");
                cullHint = backPanel.getCullHint();
                backPanel.setCullHint(Spatial.CullHint.Never);
                Spatial s = backPanel.getParent();

                s.setLocalTranslation(0f, 0f, 0.2f);
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                });
                highlight(event.getEntity(), true);
            } else if (event instanceof DropTargetDragExitEvent3D) {
                CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
                Entity parent = ((CellRendererJME) renderer).getEntity();
                RenderComponent rc = parent.getComponent(RenderComponent.class);
                final Node sceneRoot = rc.getSceneRoot();
                Spatial backPanel = sceneRoot.getChild("Image Frame BackGround");
                backPanel.setCullHint(cullHint);
                Spatial s = backPanel.getParent();
                s.setLocalTranslation(0f, 0f, 0f);
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                });
                highlight(event.getEntity(), false);
            } else if (event instanceof DropTargetDropEvent3D) {

                CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
                Entity parent = ((CellRendererJME) renderer).getEntity();
                RenderComponent rc = parent.getComponent(RenderComponent.class);
                final Node sceneRoot = rc.getSceneRoot();
                Spatial backPanel = sceneRoot.getChild("Image Frame BackGround");
                //backPanel.setCullHint(Spatial.CullHint.Always);

                Spatial s = backPanel.getParent();
                s.setLocalTranslation(0f, 0f, 0f);
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                });
                DragAndDropManager dnd = DragAndDropManager.getDragAndDropManager();
                dnd.importContent((DropTargetDropEvent3D) event, new ImportResultListener() {

                    public void importSuccess(String uri) {
                        CellServerState css = createCell(uri);
                        if (css != null) {
                            SharedStateComponent ssc = parentCell.getComponent(SharedStateComponent.class);

                        }
                    }

                    public void importFailure(Throwable cause, String message) {
                        LOGGER.log(Level.WARNING, message, cause);
                    }
                });
                highlight(event.getEntity(), false);
            }
        } catch (Exception ei) {
            ei.printStackTrace();
        }
    }

    private CellServerState getServerState(Cell cell) {
        ResponseMessage rm = cell.sendCellMessageAndWait(
                new CellServerStateRequestMessage(cell.getCellID()));
        if (rm == null) {
            return null;
        }
        CellServerStateResponseMessage stateMessage
                = (CellServerStateResponseMessage) rm;
        CellServerState state = stateMessage.getCellServerState();
        return state;
    }

    public CellServerState createCell(String uri) {
        // Figure out what the file extension is from the uri, looking for
        // the final '.'.
        try {
            String extension = getFileExtension(uri);

            if (extension == null) {
                //  LOGGER.warning("Could not find extension for " + uri);
                return null;
            }

            // First look for the SPI that tells us which Cell to use. If there
            // is none, then it is a fairly big error. (There should be at least
            // one registered in the system).
            CellSelectionSPI spi = CellSelectionRegistry.getCellSelectionSPI();
            if (spi == null) {
                final JFrame frame = JmeClientMain.getFrame().getFrame();
                //  LOGGER.warning("Could not find the CellSelectionSPI factory");
                String message = BUNDLE.getString("Launch_Failed_Message");
                message = MessageFormat.format(message, uri);
                JOptionPane.showMessageDialog(frame, message,
                        BUNDLE.getString("Launch_Failed"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // Next look for a cell type that handles content with this file
            // extension and create a new cell with it.
            CellFactorySPI factory = null;
            try {
                factory = spi.getCellSelection(extension);
            } catch (CellCreationException excp) {
                final JFrame frame = JmeClientMain.getFrame().getFrame();
                LOGGER.log(Level.WARNING,
                        "Could not find cell factory for " + extension);
                String message = BUNDLE.getString("Launch_Failed_Message");
                message = MessageFormat.format(message, uri);
                JOptionPane.showMessageDialog(frame, message,
                        BUNDLE.getString("Launch_Failed"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // If the returned factory is null, it means that the user has cancelled
            // the action, so we just return
            if (factory == null) {
                return null;
            }

            // Get the cell server state, injecting the content URI into it via
            // the properties
            Properties props = new Properties();
            props.put("content-uri", uri);
            CellServerState state = factory.getDefaultCellServerState(props);
            state.setName(getFileName(uri));
            // Create the new cell at a distance away from the avatar
            try {
                // the parent, so only a small offset in the Z dimension is needed
                PositionComponentServerState pcss = (PositionComponentServerState) state.getComponentServerState(PositionComponentServerState.class);
                if (pcss == null) {
                    pcss = new PositionComponentServerState();
                    state.addComponentServerState(pcss);
                }
                pcss.setTranslation(new Vector3f(0f, 0f, 0f));
                WonderlandSession session = parentCell.getSession();
                CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                CellCreateMessage msg = new CellCreateMessage(parentCell.getCellID(), state);
                connection.send(msg);
                return state;
            } catch (Exception excp) {
                LOGGER.log(Level.WARNING, "Unable to create cell for uri " + uri, excp);
            }

        } catch (Exception ei) {
            ei.printStackTrace();
        }
        return null;
    }

    /**
     * Utility routine to fetch the file extension from the URI, or null if none
     * can be found.
     */
    private static String getFileExtension(String uri) {
        // Figure out what the file extension is from the uri, looking for
        // the final '.'.
        int index = uri.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return uri.substring(index + 1);
    }

    public WonderlandSession getSession() {
        return parentCell.getCellCache().getSession();
    }

    public String encodeState(CellServerState mystate) throws IOException {
        ScannedClassLoader loader
                = getSession().getSessionManager().getClassloader();
        try {
            StringWriter sw = new StringWriter();
            mystate.encode(sw, CellServerStateFactory.getMarshaller(loader));
            sw.close();
            return sw.toString();
        } catch (JAXBException je) {
            throw new RuntimeException(je);
        }
    }

    public static String getFileName(String uri) { // Check to see if there is a final '/'. We always use a forward-slash
        // regardless of platform, because it is typically a wlcontent URI.
        int index = uri.lastIndexOf("/");
        if (index == -1) {
            return uri;
        }
        return uri.substring(index + 1);
    }
}
