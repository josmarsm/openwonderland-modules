/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import java.io.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
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
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateRequestMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateResponseMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameApp;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameConstants;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 *
 * @author nilang
 */
//this file is to handle drag and drop event
public class DropTargetListener extends EventClassListener {

    public String extension;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/jme/content/Bundle");
    private static final Logger LOGGER =
            Logger.getLogger(DropTargetListener.class.getName());
    public Node root;
    public Quad quad;
    public AppFrame parentCell;
    public SharedMapCli historyMap;

    public DropTargetListener(Cell parentCell) {
        this.parentCell = (AppFrame) parentCell;
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
        //  Logger.warning("Recevied event: " + event.toString());
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
            Spatial backPanel = sceneRoot.getChild("App Frame BackGround");
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
                Spatial backPanel = sceneRoot.getChild("App Frame BackGround");
                Spatial s = backPanel.getParent();
                s.setLocalTranslation(0f, 0f, 0.2f);
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                });

                //parent.addEntity(e6);
            } else if (event instanceof DropTargetDragExitEvent3D) {
                CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
                Entity parent = ((CellRendererJME) renderer).getEntity();
                RenderComponent rc = parent.getComponent(RenderComponent.class);
                final Node sceneRoot = rc.getSceneRoot();
                Spatial backPanel = sceneRoot.getChild("App Frame BackGround");
                Spatial s = backPanel.getParent();
                s.setLocalTranslation(0f, 0f, 0f);
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                    }
                });
            } else if (event instanceof DropTargetDropEvent3D) {
                if (parentCell.dirtyMap.getBoolean("dirty")) {
                    Object[] options = {"Save",
                        "Don't Save"};
                    int n = JOptionPane.showOptionDialog(null, "Do you want to save before closing?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (n == JOptionPane.YES_OPTION) {
                        CellServerState css = getServerState(parentCell.getChildren().iterator().next());
                        String name = getFileExtension(css.getName());
                        if (name == null) {
                            String s = (String) JOptionPane.showInputDialog(null, "Enter File Name:", "File Name", JOptionPane.PLAIN_MESSAGE, null, null, "Enter Name");
                            css.setName(s);
                        }
                        SharedStateComponent ssc = parentCell.getComponent(SharedStateComponent.class);
                        historyMap = ssc.get(AppFrameConstants.History_MAP);
                        try {
                            if (historyMap.get(css.getName()) != null) {
                                AppFrameApp afa = (AppFrameApp) historyMap.get(css.getName());
                                String sss = encodeState(css);
                                afa.setState(sss);
                                afa.setContentURI(parentCell.getContentURI(sss));
                                afa.setLastUsed(new Date());
                                historyMap.put(css.getName(), afa);
                            } else {
                                if (historyMap.size() >= 20) {
                                    parentCell.dropItem();
                                } else {
                                }
                                String sss = encodeState(css);
                                historyMap.put(css.getName(), new AppFrameApp(sss, new Date(), getSession().getUserID().getUsername()
                                        , new Date(),parentCell.getContentURI(sss)));
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(DropTargetListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                    }
                }
                parentCell.dirtyMap.putBoolean("dirty", false);
                CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
                Entity parent = ((CellRendererJME) renderer).getEntity();
                RenderComponent rc = parent.getComponent(RenderComponent.class);
                final Node sceneRoot = rc.getSceneRoot();
                Spatial backPanel = sceneRoot.getChild("App Frame BackGround");
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
                            if(ssc!=null) {
                            historyMap = ssc.get(AppFrameConstants.History_MAP);
                            try {
                                if (historyMap.size() >= 20 && historyMap.get(css.getName()) == null) {
                                    parentCell.dropItem();
                                } else {
                                }
                                String sss = encodeState(css);
                                historyMap.put(css.getName(), new AppFrameApp(sss, new Date(), getSession().getUserID().getUsername()
                                        , new Date(),parentCell.getContentURI(sss)));
                                
                            } catch (IOException ex) {
                                Logger.getLogger(DropTargetListener.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            }
                        }
                    }

                    public void importFailure(Throwable cause, String message) {
                        LOGGER.log(Level.WARNING, message, cause);
                    }
                });
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
        CellServerStateResponseMessage stateMessage =
                (CellServerStateResponseMessage) rm;
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

//            CellSelectionSPI simpleCellSelection =  CellSelectionRegistry.getCellSelectionSPI();
//            CellSelectionRegistry.unregister(simpleCellSelection);
            
            AppFrameCellSelectionSPI afcs = new AppFrameCellSelectionSPI();
//            CellSelectionRegistry.register(afcs);
            
            // First look for the SPI that tells us which Cell to use. If there
            // is none, then it is a fairly big error. (There should be at least
            // one registered in the system).
//            CellSelectionSPI spi =  CellSelectionRegistry.getCellSelectionSPI();
//            System.out.println("spi : "+spi);
            if (afcs == null) {
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
                factory =  afcs.getCellSelection(extension);
                
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
            CellServerState state = (factory).getDefaultCellServerState(props);
            state.setName(getFileName(uri));
            // Create the new cell at a distance away from the avatar
            try {
                // the parent, so only a small offset in the Z dimension is needed
                PositionComponentServerState pcss = (PositionComponentServerState) state.getComponentServerState(PositionComponentServerState.class);
                if (pcss == null) {
                    pcss = new PositionComponentServerState();
                    state.addComponentServerState(pcss);
                }
                pcss.setTranslation(new Vector3f(0f, 0f, 0.02f));
                pcss.setScaling(new Vector3f(1f, 1f, 1f));

                state.addComponentServerState(pcss);
                WonderlandSession session = parentCell.getSession();
                CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                CellCreateMessage msg = new CellCreateMessage(parentCell.getCellID(), state);
                connection.sendAndWait(msg);
                parentCell.flag=1;
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
     * Utility routine to fetch the file extension from the URI, or null if
     * none can be found.
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
        ScannedClassLoader loader =
                getSession().getSessionManager().getClassloader();
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = CellServerStateFactory.getMarshaller(loader);
            mystate.encode(sw, marshaller);
            String s ="<![CDATA["+sw.toString()+"]]>";  
            //String s =sw.toString();  
            sw.close();
            return s;
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
    
    public class AppFrameCellSelectionSPI implements CellSelectionSPI {

    /**
     * @{@inheritDoc}
     */
    public CellFactorySPI getCellSelection(String extension) throws CellCreationException {
            // Find the list of Cells that support the given extension. If none,
            // then throw an exception.
            CellRegistry registry = CellRegistry.getCellRegistry();
            Set<CellFactorySPI> factorySet = registry.getCellFactoriesByExtension(extension);
            if (factorySet == null || factorySet.size() == 0) {
                throw new CellCreationException();
            }

            // If there is only one, then just return it, since there is no choices
            // to be made.
            if (factorySet.size() == 1) {
                return factorySet.iterator().next();
            } else {
                Iterator itr = factorySet.iterator();
                while(itr.hasNext()) {
                    CellFactorySPI cfs = (CellFactorySPI) itr.next();
                    if(cfs.getDisplayName()!=null && 
                            cfs.getDisplayName().equalsIgnoreCase("PDF Viewer")) {
                        return cfs;
                    }
                }
            }
            return null;
        }
    }

}
