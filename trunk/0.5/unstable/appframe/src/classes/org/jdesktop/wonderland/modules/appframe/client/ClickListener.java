/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellSelectionRegistry;
import org.jdesktop.wonderland.client.cell.utils.spi.CellSelectionSPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import static org.jdesktop.wonderland.modules.appframe.client.ClickListener.getFileName;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameApp;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameConstants;
import org.jdesktop.wonderland.modules.appframe.common.AppFramePinToMenu;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author nilang
 */
// a listener that handles mouse clicks. The parent also handles
// highlighting the button when the mouse is over it
public class ClickListener extends MouseClickListener {

    public AppFrame parentCell;
    public JFrame contextMenu = new JFrame();
    public JPanel contextPanel;
    public JMenuItem item[];
    private Color WL_BLUE = new Color(7, 73, 165);
    private Color WL_LIGHT_BLUE = new Color(12, 104, 234);
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/client/jme/content/Bundle");
    private static final Logger LOGGER =
            Logger.getLogger(DropTargetListener.class.getName());

    public ClickListener(AppFrame parentCell) {
        this.parentCell = parentCell;
        AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
        Color borderColor = AppFrameProperties.parseColorString(afp.getBorderColor());
        this.HIGHLIGHT_COLOR = new ColorRGBA(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha());
    }

    private class MenuFocusListener implements FocusListener {

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            JFrame f = (JFrame) e.getComponent();
            f.setVisible(false);
        }
    }
//this class is responisible to crate pinned document and pinned apps from menu label click

    private class LoadPinnedItemListner implements ActionListener {

        public AppFrame parentCell;

        public LoadPinnedItemListner(AppFrame parentCell) {
            this.parentCell = parentCell;
        }

        public void hideContextMenu() {
            contextMenu.setVisible(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                hideContextMenu();
                parentCell.contextMenu.setVisible(false);
                parentCell.contextMenu.setVisible(false);
                WonderlandSession session = parentCell.getSession();

                if (parentCell.getNumChildren() == 1) {
                    parentCell.savePrevious();
                    parentCell.remove();
                }
                AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                AppFramePinToMenu afPin = (AppFramePinToMenu) parentCell.pinToMenuMap.get(e.getActionCommand());
                String url = afPin.getFileURL();

                CellServerState serverState = null;
                if (url.equals("pinnedApp")) {
                    Set<CellFactorySPI> cellFactoryList = CellRegistry.getCellRegistry().getAllCellFactories();
                    for (CellFactorySPI cfs : cellFactoryList) {
                        if (cfs.getDisplayName() != null) {
                            if (cfs.getDisplayName().equals(e.getActionCommand())) {
                                serverState = cfs.getDefaultCellServerState(null);
                                if(serverState.getName()==null) {
                                    serverState.setName(cfs.getDisplayName());
                                }
                            }
                        }
                    }
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        try {
                            if (parentCell.historyMap.get(serverState.getName()) != null) {
                                AppFrameApp afa = (AppFrameApp) parentCell.historyMap.get(serverState.getName());
                                String sss = parentCell.encodeState(serverState);
                                afa.setState(sss);
                                afa.setContentURI(parentCell.getContentURI(sss));
                                afa.setLastUsed(new Date());
                                parentCell.historyMap.put(serverState.getName(), afa);
                            } else {
                                if (parentCell.historyMap.size() >= 20) {
                                    parentCell.dropItem();
                                } else {
                                }
                                String sss = parentCell.encodeState(serverState);
                                parentCell.historyMap.put(serverState.getName(), new AppFrameApp(sss, new Date()
                                        , session.getUserID().getUsername(), new Date()
                                        ,parentCell.getContentURI(sss)));
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ClickListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        pcss.setTranslation(new Vector3f(0f, 0f, 0.02f));

                        CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                        //parentCell.currentcellname=css.getName();


                        CellCreateMessage msg = new CellCreateMessage(parentCell.getCellID(), serverState);
                        connection.send(msg);

                    }

                } else {
                    DropTargetListener dtl = new DropTargetListener(parentCell);
                    CellServerState css = dtl.createCell(url);
                    if (css != null) {
                        SharedStateComponent ssc = parentCell.getComponent(SharedStateComponent.class);
                        SharedMapCli historyMap = ssc.get(AppFrameConstants.History_MAP);
                        try {
                            if (historyMap.get(css.getName()) != null) {

                                AppFrameApp afa = (AppFrameApp) historyMap.get(css.getName());
                                afa.setLastUsed(new Date());
                                String sss = parentCell.encodeState(css);
                                afa.setState(sss);
                                afa.setContentURI(parentCell.getContentURI(sss));
                                historyMap.put(css.getName(), afa);
                            } else {
                                if (parentCell.historyMap.size() >= 20) {
                                    parentCell.dropItem();
                                } else {
                                }
                                String sss = parentCell.encodeState(css);
                                historyMap.put(css.getName(), new AppFrameApp(sss, new Date(), parentCell.getSession()
                                        .getUserID().getUsername(), new Date()
                                        ,parentCell.getContentURI(sss)));
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Exception ei) {
                ei.printStackTrace();
            }
        }
    }
//this method s responsible for create all recent documents and apps from appmenu item click

    private class LoadListner implements ActionListener {

        public AppFrame parentCell;

        public LoadListner(AppFrame parentCell) {
            this.parentCell = parentCell;
        }

        public void hideContextMenu() {
            contextMenu.setVisible(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            hideContextMenu();
            load(e.getActionCommand());

        }

        public void load(String fileName) {
            try {
                AppFrameApp obj = (AppFrameApp) parentCell.historyMap.get(fileName);
                WonderlandSession session = parentCell.getSession();
                CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                if (parentCell.getNumChildren() == 1) {
                    parentCell.savePrevious();
                    parentCell.remove();
                }
                CellServerState css = parentCell.decodeState(obj.getState());
                if (css != null) {
                    SharedStateComponent ssc = parentCell.getComponent(SharedStateComponent.class);
                    SharedMapCli historyMap = ssc.get(AppFrameConstants.History_MAP);
                    try {
                        AppFrameApp afa = null;
                        afa = (AppFrameApp) historyMap.get(css.getName());
                        afa.setLastUsed(new Date());
                        String sss = parentCell.encodeState(css);
                        afa.setState(sss);
                        afa.setContentURI(parentCell.getContentURI(sss));
                        historyMap.put(css.getName(), afa);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                CellCreateMessage msg = new CellCreateMessage(parentCell.getCellID(), css);
                connection.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//this method is used to handle morehistory andsave item events

    private class ContextMenuListener extends MenuListener {

        public AppFrame parentCell;

        public ContextMenuListener(AppFrame parentCell) {
            super(parentCell);
            this.parentCell = parentCell;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                hideContextMenu();
                if (e.getActionCommand().equals("Save")) {
                    parentCell.store();
                    AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                    parentCell.dirtyMap.putBoolean("dirty", false);
                }
                if (e.getActionCommand().equals("More History")) {
                    JFrame contextMenu1 = new JFrame();
                    contextMenu1.setTitle("AppFrame History");
                    contextMenu1.setResizable(true);
                    contextMenu1.getContentPane().setLayout(new GridLayout(1, 1));
                    JPanel contextPanel1 = new JPanel();
                    contextMenu1.getContentPane().add(contextPanel1);
                    contextPanel1.setBorder(BorderFactory.createLineBorder(
                            WL_LIGHT_BLUE, 2));
                    contextPanel1.setLayout(new BoxLayout(contextPanel1,
                            BoxLayout.Y_AXIS));
                    parentCell.propertyMap = parentCell.sharedState.get(AppFrameConstants.Prop_MAP);
                    JPanel historyTable = new AppFrameHistory(parentCell.historyMap, contextMenu1, parentCell.propertyMap, parentCell);
                    contextPanel1.add(historyTable);
                    contextMenu1.pack();
                    contextMenu1.setVisible(true);
                    contextMenu1.toFront();
                    contextMenu1.repaint();
                    contextMenu1.addFocusListener(new ClickListener.MenuFocusListener());
                }
                super.action(e);
            } catch (Exception ei) {
                ei.printStackTrace();
            }

        }

        public void hideContextMenu() {
            contextMenu.setVisible(false);
        }
    }

    @Override
    public void computeEvent(final Event event) {
        try {
            if (event instanceof MouseButtonEvent3D) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        contextMenu = new JFrame();
                        AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                        item = new JMenuItem[Integer.parseInt(afp.getMaxHistory() + 1)];
                        contextMenu.setResizable(false);
                        contextMenu.setUndecorated(true);
                        contextMenu.getContentPane().setLayout(new GridLayout(1, 1));
                        contextPanel = new JPanel();
                        contextMenu.getContentPane().add(contextPanel);
                        contextPanel.setBorder(BorderFactory.createLineBorder(
                                WL_LIGHT_BLUE, 2));
                        contextPanel.setLayout(new BoxLayout(contextPanel,
                                BoxLayout.Y_AXIS));
                        JPanel titlePanel = new JPanel();
                        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                        titlePanel.setBackground(WL_BLUE);
                        JLabel title = new JLabel("<html><b>" + "AppFrame"
                                + "</b></html>");
                        title.setForeground(Color.WHITE);
                        title.setBackground(Color.GRAY);
                        titlePanel.add(title);
                        contextPanel.add(titlePanel);
                        JMenuItem item1 = new JMenuItem("Add Document");
                        JMenuItem item2 = new JMenuItem("Add App");
                        JMenuItem item4 = new JMenuItem("Properties");
                        ClickListener.ContextMenuListener ml = new ClickListener.ContextMenuListener(parentCell);
                        item1.addActionListener(ml);
                        item2.addActionListener(ml);
                        contextPanel.add(item1);
                        contextPanel.add(item2);
                        JMenuItem item3 = new JMenuItem("Save");
                        item3.addActionListener(ml);
                        contextPanel.add(item3);
                        if (parentCell.dirtyMap.getBoolean("dirty")) {
                        } else {
                            item3.setEnabled(false);
                        }
                        item4.addActionListener(ml);
                        contextPanel.add(item4);
                        refresh();
                        JMenuItem item6 = new JMenuItem("More History");
                        item6.addActionListener(ml);
                        contextPanel.add(item6);
                        contextMenu.pack();
                        contextMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
                        contextMenu.setVisible(true);
                        contextMenu.toFront();
                        contextMenu.repaint();
                    }

                    public void refresh() {
                        contextMenu.addFocusListener(new ClickListener.MenuFocusListener());
                        JSeparator js = new JSeparator();
                        JSeparator js1 = new JSeparator();
                        ClickListener.LoadPinnedItemListner itemListener = new ClickListener.LoadPinnedItemListner(parentCell);
                        int j = 0;
                        AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                        Set<String> pinnedItems =
                                parentCell.pinToMenuMap.keySet();
                        if (!pinnedItems.isEmpty()) {
                            contextPanel.add(js);
                            JMenuItem pinItem[] = new JMenuItem[pinnedItems.size() + 1];

                            for (String pinnedItem : pinnedItems) {
                                pinItem[j] = new JMenuItem(pinnedItem);
                                contextPanel.add(pinItem[j]);
                                pinItem[j++].addActionListener(itemListener);
                            }

                        }
                        int i = 0;
                        ClickListener.LoadListner loadItemListener = new ClickListener.LoadListner(parentCell);
                        if (parentCell != null) {
                            contextPanel.add(js1);
                            parentCell.historyMap = parentCell.findMap(parentCell, AppFrameConstants.History_MAP);
                            Set<Entry<String, SharedData>> histry = parentCell.historyMap.entrySet();
                            HashMap<Date, String> myMap = new HashMap<Date, String>();
                            ArrayList<Date> history = new ArrayList<Date>();
                            for (Map.Entry<String, SharedData> list : histry) {
                                String name1 = list.getKey();
                                AppFrameApp afa1 = (AppFrameApp) list.getValue();
                                Date date1 = afa1.getLastUsed();
                                history.add(date1);
                                myMap.put(date1, name1);
                            }
                            Date[] dates = new Date[parentCell.historyMap.size() + 1];
                            history.toArray(dates);
                            //   Arrays.sort(dates);

                            for (int im = 0; dates[im] != null; im++) {
                                for (int im2 = im + 1; dates[im2] != null; im2++) {
                                    if (dates[im].compareTo(dates[im2]) < 0) {
                                        Date temp = dates[im];
                                        dates[im] = dates[im2];
                                        dates[im2] = temp;
                                    }
                                }
                            }
                            for (int im = 0; dates[im] != null; im++) {
                                if (i < Integer.parseInt(afp.getMaxHistory())) {

                                    item[i] = new JMenuItem(myMap.get(dates[im]));
                                    item[i].addActionListener(loadItemListener);
                                    contextPanel.add(item[i++]);
                                }
                            }
                        }

                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                pcss.setTranslation(new Vector3f(0f, 0f, 0.02f));
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
    private static String getFileExtension(String uri) {
        // Figure out what the file extension is from the uri, looking for
        // the final '.'.
        int index = uri.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return uri.substring(index + 1);
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
