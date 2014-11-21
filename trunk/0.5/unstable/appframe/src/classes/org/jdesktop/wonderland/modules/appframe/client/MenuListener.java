/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.math.Vector3f;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameApp;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameConstants;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;

/**
 *
 * @author nilang
 */
//this file is to handle menu events for add document ,add app and properties
public abstract class MenuListener implements ActionListener {

    JFrame AddApp;
    public JFrame properties;
    public AppFrame parentCell;

    public MenuListener(Cell parentCell) {
        this.parentCell = (AppFrame) parentCell;
        AddApp = new JFrame();
        AddApp.setTitle("Add App");
    }

    public void action(final ActionEvent e) {
        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (e.getActionCommand().equals("Properties")) {
                        properties = new JFrame();
                        properties.setTitle("AppFrameProperties");
                        JPanel panel = new AppFrameProperties(parentCell, properties);
                        final AppFrameProperties propertyPane = (AppFrameProperties) panel;
                        properties.add(propertyPane);
                        parentCell.appFrameProperties = propertyPane;
                        properties.addWindowListener(new WindowAdapter() {

                            @Override
                            public void windowClosing(WindowEvent windowEvent) {
                                int answer = JOptionPane.showConfirmDialog(properties, "Do you want to apply the properties before closing?", "SaveChanges", JOptionPane.YES_NO_OPTION);
                                if (answer == JOptionPane.YES_OPTION) {
                                    propertyPane.ok();
                                    properties.setVisible(false);
                                } else if (answer == JOptionPane.NO_OPTION) {
                                    propertyPane.cancel();
                                    properties.setVisible(false);
                                }
                            }
                        });
                        properties.setLocation(0, 0);
                        properties.setResizable(false);
                        properties.pack();
                        properties.setVisible(true);
                    } else if (e.getActionCommand().equals("Add App")) {
                        Set<CellFactorySPI> cellFactoryList = CellRegistry.getCellRegistry().getAllCellFactories();
                        JPanel contextPanel = new JPanel();
                        contextPanel.setLayout(new BoxLayout(contextPanel,
                                BoxLayout.Y_AXIS));
                        AddApp.add(contextPanel);
                        AddApp.setLocation(MouseInfo.getPointerInfo().getLocation());
                        JMenuItem[] menus = new JMenuItem[10];
                        int i = 0;
                        MenuListener.AddAppMenuListner aaml = new MenuListener.AddAppMenuListner();
                        for (String ext : AppFrameConstants.extension) {
                            Iterator<CellFactorySPI> iterator = CellRegistry.getCellRegistry().getCellFactoriesByExtension(ext).iterator();
                            while (iterator.hasNext()) {
                                CellFactorySPI factorySPI = iterator.next();
                                menus[i] = new JMenuItem(factorySPI.getDisplayName());
                                menus[i].addActionListener(aaml);
                                contextPanel.add(menus[i++]);
                            }
                        }
                        for (CellFactorySPI cfs : cellFactoryList) {
                            if (cfs.getDisplayName() != null) {
                                if (cfs.getDisplayName().equals("Webcam Viewer")) {
                                    menus[i] = new JMenuItem(cfs.getDisplayName());
                                    menus[i].addActionListener(aaml);
                                    contextPanel.add(menus[i++]);
                                }
                            }
                        }
                        for (CellFactorySPI cfs : cellFactoryList) {
                            if (cfs.getDisplayName() != null) {
                                    if (cfs.getDisplayName().equals("Text Editor")) {
                                        menus[i] = new JMenuItem(cfs.getDisplayName());
                                        menus[i].addActionListener(aaml);
                                        contextPanel.add(menus[i++]);
                                    }
                                }
                            }
                        for (CellFactorySPI cfs : cellFactoryList) {
                            if (cfs.getDisplayName() != null) {
                                if (cfs.getDisplayName().equals("Screen Sharer")) {
                                    menus[i] = new JMenuItem(cfs.getDisplayName());
                                    menus[i].addActionListener(aaml);
                                    contextPanel.add(menus[i++]);
                                }
                            }
                        }
                        AddApp.setResizable(false);
                        AddApp.pack();
                        AddApp.setVisible(true);
                    } else if (e.getActionCommand().equals("Add Document")) {
                        final JFrame AddDocument = new JFrame();
                        AddDocument.setTitle("Add Document");
                        JPanel contextPanel2 = new AddServerDocument(AddDocument, parentCell);
                        AddDocument.add(contextPanel2);
                        parentCell.addDocument = "open";
                        AddDocument.setResizable(false);
                        AddDocument.pack();
                        AddDocument.setVisible(true);
                    }
                }
            });
        } catch (Exception ei) {
            ei.printStackTrace();
        }
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
    public class JaxbCharacterEscapeHandler implements com.sun.xml.bind.marshaller.CharacterEscapeHandler {

        public void escape(char[] buf, int start, int len, boolean isAttValue,
                        Writer out) throws IOException {
            for (int i = start; i < start + len; i++) {
                    char ch = buf[i];
                    out.write(ch);
            }
        }
    }
    private class AddAppMenuListner implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {    
                parentCell.savePrevious();
                AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                parentCell.dirtyMap.putBoolean("dirty", false);
                AddApp.setVisible(false);
                String cell = e.getActionCommand();
                Set<CellFactorySPI> cfs = CellRegistry.getCellRegistry().getAllCellFactories();
                CellServerState serverState = null;
                for (CellFactorySPI cfspi : cfs) {

                    if (cfspi.getDisplayName() != null) {
                        if (cfspi.getDisplayName().equals(cell)) {
                            serverState = cfspi.getDefaultCellServerState(null);
                            if(serverState.getName()==null) {
                                serverState.setName(cfspi.getDisplayName());
                            }
                        }
                    }
                }
                PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                if (pcss == null) {
                    pcss = new PositionComponentServerState();
                    serverState.addComponentServerState(pcss);
                }
                
                pcss.setTranslation(new Vector3f(0f, 0f, 0.02f));
                ServerSessionManager manager = LoginManager.getPrimary();
                WonderlandSession session = manager.getPrimarySession();
                CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
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
                    ex.printStackTrace();
                }

                CellCreateMessage msg = new CellCreateMessage(parentCell.getCellID(), serverState);
                connection.send(msg);
            } catch (Exception ei) {
                ei.printStackTrace();
            }
        }
    }
}
