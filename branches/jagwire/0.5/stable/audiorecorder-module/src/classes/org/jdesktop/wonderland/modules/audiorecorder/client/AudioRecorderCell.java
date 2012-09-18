/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.audiorecorder.client;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellClientState;
import org.jdesktop.wonderland.modules.audiorecorder.common.Tape;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderCell extends Cell {
    private static final Logger audioRecorderLogger = Logger.getLogger(AudioRecorderCell.class.getName());
    private static final String AUDIO_RECORDINGS_DIRECTORY = "AudioRecordings";

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiorecorder/client/resources/Bundle");
    private ContextMenuFactorySPI menuFactory = null;

    private boolean isPlaying, isRecording;
    private String userName;
    private AudioRecorderCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private TapeListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;
    private Tape selectedTape = null;

    /** the message handler, or null if no message handler is registered */
    private AudioRecorderCellMessageReceiver receiver = null;

    public AudioRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isRecording = false;
        isPlaying = false;
    }

    private Tape getUntitledTape() {
        return new Tape(bundle.getString("UNTITLED_TAPE"));
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.RENDERING) {
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        setReelFormVisible(true);
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem(bundle.getString("OPEN_TAPE..."), l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
        if (increasing && status.equals(CellStatus.ACTIVE)) {
            //About to become visible, so add the message receiver
            if (receiver == null) {
                receiver = new AudioRecorderCellMessageReceiver();
                getChannel().addMessageReceiver(AudioRecorderCellChangeMessage.class, receiver);
            }
        }
        if (status.equals(CellStatus.DISK)) {
            //Cleanup
            if (getChannel() != null) {
                getChannel().removeMessageReceiver(AudioRecorderCellChangeMessage.class);
            }
            receiver = null;
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
        }
    }

    @Override
    public void setClientState(CellClientState cellClientState) {
        super.setClientState(cellClientState);

        isPlaying = ((AudioRecorderCellClientState) cellClientState).isPlaying();
        isRecording = ((AudioRecorderCellClientState) cellClientState).isRecording();
        userName = ((AudioRecorderCellClientState) cellClientState).getUserName();
        if (isPlaying | isRecording) {
            if (userName == null) {
                logger.warning("userName should not be null");
            }
        }
        if (!isPlaying & !isRecording) {
            if (userName != null) {
                logger.warning("userName should be null");
            }
        }
        String selectedTapeName = ((AudioRecorderCellClientState) cellClientState).getSelectedTapeName();
        initializeTapeModels(selectedTapeName);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                reelForm = new ReelForm(AudioRecorderCell.this);
            }
        });

    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    Tape addTape(String tapeName) {
        audioRecorderLogger.info("add " + tapeName);
        Tape newTape = new Tape(tapeName);
        tapeListModel.addElement(newTape);
        return newTape;
    }

    ListModel getTapeListModel() {
        return tapeListModel;
    }

    private void initializeTapeModels(String selectedTapeName) {
        audioRecorderLogger.info("selectedTape: " + selectedTapeName);
        List<Tape> sortedTapes = new ArrayList<Tape>(getTapes());
        Collections.sort(sortedTapes);
        tapeListModel = new DefaultListModel();
        for(Tape t: sortedTapes) {
            tapeListModel.addElement(t);
        }
        initializeSelectedTape(selectedTapeName);
        tapeSelectionModel = new TapeListSelectionModel();
        int selectionIndex = sortedTapes.indexOf(selectedTape);
        tapeSelectionModel.setSelectedTapeIndex(selectionIndex);
    }

    private void initializeSelectedTape(String selectedTapeName) {
        if (selectedTapeName != null) {
            audioRecorderLogger.info("selectedTapeName: " + selectedTapeName);
            //There is a selected tape, so select the tape with that tape name
            for (int i = 0; i < tapeListModel.getSize(); i++) {
                Tape aTape = (Tape) tapeListModel.get(i);
                if (aTape.getTapeName().equals(selectedTapeName)) {
                    //Found the tape
                    audioRecorderLogger.info("found the tape: " + aTape);
                    selectedTape = aTape;
                    break;
                }
            }
        }
        if (selectedTape != null) {
            return;
        }
        audioRecorderLogger.info("no selected tape");
        //There's no selectedTape on the server, or we can't find the selectedTape
        if (tapeListModel.isEmpty()) {
            //there's no tapes, so set the selected tape to be the untitled tape
            selectedTape = getUntitledTape();
        } else {
            //Set the selected tape to be the first tape
            selectedTape = (Tape) tapeListModel.firstElement();
        }
        audioRecorderLogger.info("new selected tape: " + selectedTape);
        AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.tapeSelected(getCellID(), selectedTape);
        getChannel().send(msg);
    }


    private void updateTapeModels() {
        audioRecorderLogger.info("updating tape models");
        Set<Tape> newTapes = getTapes();
        Enumeration<Tape> e = (Enumeration<Tape>) tapeListModel.elements();
        Set<Tape> existingTapes = new HashSet<Tape>();
        while (e.hasMoreElements()) {
            existingTapes.add(e.nextElement());
        }
        if (existingTapes.equals(newTapes)) {
            audioRecorderLogger.info("no change in tapes");
            return;
        }
        List<Tape> sortedTapes = new ArrayList<Tape>(newTapes);
        Collections.sort(sortedTapes);
        if (!sortedTapes.contains(selectedTape)) {
            if (!sortedTapes.isEmpty()) {
                selectedTape = sortedTapes.get(0);
            } else {
                selectedTape = getUntitledTape();
                sortedTapes.add(selectedTape);
            }
            audioRecorderLogger.info("new selected tape: " + selectedTape);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.tapeSelected(getCellID(), selectedTape);
            getChannel().send(msg);
        }
        tapeListModel.clear();
        for(Tape t: sortedTapes) {
            tapeListModel.addElement(t);
        }
        selectTape(selectedTape);
    }

    private Set<Tape> getTapes() {
        Set<Tape> tapes = new HashSet<Tape>();
        try {
            ContentCollection recordingRoot = getSystemRoot(getCellCache().getSession().getSessionManager());
            if (recordingRoot == null) {
                logger.severe("Failed to get recording root");
                return tapes;
            }
            
            ContentNode node = recordingRoot.getChild(AUDIO_RECORDINGS_DIRECTORY);
            if (node == null) {
                logger.severe("No audio recordings directory in webdav");
                return tapes;
            }
            ContentCollection dirNode = (ContentCollection) node;
            List<ContentNode> children = dirNode.getChildren();
            for (Iterator<ContentNode> it = children.iterator(); it.hasNext();) {
                ContentNode contentNode = it.next();
                String nodeName = contentNode.getName();
                if (nodeName.endsWith(".au")) {
                    int index = nodeName.indexOf(".au");
                    String tapeName = nodeName.substring(0, index);
                    Tape aTape = new Tape(tapeName);
                    ContentResource r = (ContentResource) contentNode;

                    // get the URL as a wlcontent:// URL that can be translated
                    // into a real URL by the server
                    URL contentURL;
                    try {
                        contentURL = new URL("wlcontent://system/" +
                                             AUDIO_RECORDINGS_DIRECTORY + "/" +
                                             nodeName);
                    } catch (MalformedURLException mue) {
                        logger.log(Level.WARNING, "Error creating URL", mue);
                        contentURL = r.getURL();
                    }
                    
                    aTape.setURL(contentURL.toString());
                    aTape.setUsed();
                    tapes.add(aTape);
                }
            }
        } catch (ContentRepositoryException ex) {
            logger.log(Level.SEVERE, "Failed to read content repository", ex);
        }
        return tapes;
    }

    Set<String> getTapeNames() {
       audioRecorderLogger.info("getting tape names");
       Enumeration tapes = tapeListModel.elements();
       Set<String> tapeNames = new HashSet<String>();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            tapeNames.add(aTape.getTapeName());
        }
       return tapeNames;
   }

    ListSelectionModel getTapeSelectionModel() {
        return tapeSelectionModel;
    }

    void listSelectionChanged() {
        audioRecorderLogger.info("listSelection changed");
        int index = tapeSelectionModel.getMaxSelectionIndex();
        if (index >= 0) {
            Tape aTape = (Tape) tapeListModel.elementAt(index);
            if (!selectedTape.equals(aTape)) {
                selectedTape = aTape;
                audioRecorderLogger.info("selected tape: " + selectedTape);
                AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.tapeSelected(getCellID(), selectedTape);
                getChannel().send(msg);
            }
        }
    }

    private void selectTape(Tape aTape) {
        //Received from server
        audioRecorderLogger.info("select tape: " + aTape);
        selectedTape = aTape;       
        
        int selectionIndex = tapeListModel.indexOf(aTape);
        tapeSelectionModel.clearSelection();
        tapeSelectionModel.setSelectedTapeIndex(selectionIndex);
        reelForm.selectTape(selectedTape);
        audioRecorderLogger.info("selection: " + tapeListModel.get(tapeSelectionModel.getMaxSelectionIndex()));
    }

    private void setTapeUsed(String tapeName) {
        audioRecorderLogger.info("setTapeUsed: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
            }
        }
    }

    void startRecording() {
        audioRecorderLogger.info("start recording");
        if (reelForm.isVisible()) {
            logger.warning("Can't start recording when the user is selecting a tape");
            Toolkit.getDefaultToolkit().beep();
            reelForm.toFront();
            return;
        }
        if (!isPlaying) {
            if (selectedTape == null) {
                logger.warning("Can't record when there's no selected tape");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("PLEASE_SELECT_A_TAPE"));
                    }
                });
                return;
            }
            if (!selectedTape.isFresh()) {
                logger.warning("Overwriting existing recording");
            }
            setUsed(selectedTape);
            if (userName != null) {
                logger.warning("userName should be null");
            }
            userName = getCurrentUserName();
            setRecording(true);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.recording(getCellID(), selectedTape, isRecording, userName);
            getChannel().send(msg);
        } else {
            logger.warning("Can't start recording when already playing");
        }
    }

    private void setUsed(Tape aTape) {
        audioRecorderLogger.info("setUsed: " + aTape);
        aTape.setUsed();
        AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.setTapeUsed(getCellID(), aTape);
        getChannel().send(msg);
    }

    void startPlaying() {
        audioRecorderLogger.info("start playing");
        if (reelForm.isVisible()) {
            logger.warning("Can't start playing when the user is selecting a tape");
            Toolkit.getDefaultToolkit().beep();
            reelForm.toFront();
            return;
        }
        if (!isRecording) {
            updateTapeModels();
            if (selectedTape == null) {
                logger.warning("Can't playback when there's no selected tape");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("PLEASE_SELECT_A_TAPE"));
                    }
                });
                return;
            }
            if (selectedTape.isFresh()) {
                logger.warning("Can't playback a tape that's not ben recorded");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("CAN'T_PLAYBACK_A_TAPE_THAT'S_NOT_BEN_RECORDED"));
                    }
                });
                return;
            }
            if (userName != null) {
                logger.warning("userName should be null");
            }
            userName = getCurrentUserName();
            setPlaying(true);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.playing(getCellID(), selectedTape, isPlaying, userName);
            audioRecorderLogger.info("message: " + msg.getDescription());
            getChannel().send(msg);
        } else {
            logger.warning("Can't start playing when already recording");
        }
    }


    void stop() {
        if (!isPlaying && !isRecording) {
            //logger.warning("no reason to stop, not playing or recording");
            return;
        }
        if (reelForm.isVisible()) {
            logger.warning("Can't stop when the user is selecting a tape");
            Toolkit.getDefaultToolkit().beep();
            reelForm.toFront();
            return;
        }
        if (userName != null && userName.equals(getCurrentUserName())) {
            AudioRecorderCellChangeMessage msg = null;
            if (isRecording) {
                msg = AudioRecorderCellChangeMessage.recording(getCellID(), selectedTape, false, userName);
            }
            if (isPlaying) {
                msg = AudioRecorderCellChangeMessage.playing(getCellID(), selectedTape, false, userName);
            }
            if (msg != null) {
                getChannel().send(msg);
            }
            setRecording(false);
            setPlaying(false);
            userName = null;
        } else {
            logger.warning("Attempt to stop by non-initiating user: " + userName);
            SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("YOU_CAN'T_STOP_A_TAPE_THAT'S_BEING_USED_BY_ANOTHER_USER"));
                    }
                });
        }
    }

    private void setRecording(boolean b) {
        audioRecorderLogger.info("setRecording: " + b);
        renderer.setRecording(b);
        isRecording = b;
    }

    private void setPlaying(boolean b) {
        audioRecorderLogger.info("setPlaying: " + b);
        renderer.setPlaying(b);
        isPlaying = b;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    boolean isRecording() {
        return isRecording;
    }

    private String getCurrentUserName() {
        return getCellCache().getSession().getUserID().getUsername();
    }

    private JFrame getParentFrame() {
        return ClientContextJME.getClientMain().getFrame().getFrame();
    }

    void setReelFormVisible(final boolean aBoolean) {
        audioRecorderLogger.info("set visible: " + aBoolean);
        if (isRecording || isPlaying) {
            logger.warning("Can't select a tape when the user is already playing/recording");
            //See Deron's forum post
            //http://forums.java.net/jive/thread.jspa?messageID=356627
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("CAN'T_SELECT_A_TAPE_WHEN_THE_AUDIO_RECORDER_IS_IN_USE"));
                }
            });
            return;
        }
        updateTapeModels();
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                reelForm = new ReelForm(AudioRecorderCell.this);
                reelForm.pack();
                Rectangle parentBounds = getParentFrame().getBounds();
                Rectangle formBounds = reelForm.getBounds();
                reelForm.setLocation(parentBounds.width/2 - formBounds.width/2 + parentBounds.x, parentBounds.height - formBounds.height - parentBounds.y);
                reelForm.setVisible(aBoolean);
            }
        });

    }
    

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new AudioRecorderCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    /**
     * Returns the content repository root for the system root, or null upon
     * error.
     */
    private ContentCollection getSystemRoot(ServerSessionManager loginInfo) {
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(loginInfo);
        if (repo == null) {
            logger.severe("Repository is null");
            return null;
        }
        try {
            return repo.getSystemRoot();
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to find repository root", excp);
            return null;
        }
    }

    

    class AudioRecorderCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            AudioRecorderCellChangeMessage sccm = (AudioRecorderCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case SET_VOLUME:
                        //AudioRecorderCellMenu menu = AudioRecorderCellMenu.getInstance();
                        //menu.volumeChanged(getCellID().toString(), message.getVolume());
                        break;
                    case PLAYBACK_DONE:
                        setPlaying(false);
                        userName = null;
                        break;
                    case PLAY:
                        setPlaying(sccm.isPlaying());
                        userName = sccm.getUserName();
                        break;
                    case RECORD:
                        setRecording(sccm.isRecording());
                        userName = sccm.getUserName();
                        break;
                    case TAPE_SELECTED:
                        selectTape(sccm.getTape());
                        break;
                    default:
                        logger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }
    }
}

