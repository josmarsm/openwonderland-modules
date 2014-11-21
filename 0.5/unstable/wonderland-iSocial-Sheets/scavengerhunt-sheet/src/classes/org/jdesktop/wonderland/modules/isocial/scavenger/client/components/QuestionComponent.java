/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client.components;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.MultipleChoiceQuestionPanel;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.NumericQuestionPanel;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.FindMethod;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.Question;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionComponentClientState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionResultDetails;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedFindMethod;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author Vladimir Djurovic
 * @author Abhishek Upadhyay
 */
public class QuestionComponent extends CellComponent implements 
        ContextMenuActionListener, SharedMapListenerCli
        , ProximityListener, CapabilityBridge {

    private static final Logger LOGGER = Logger.getLogger(QuestionComponent.class.getName());
    private SharedStateComponent sharedState;
    private SharedMapCli globalSharedMap;
    @UsesCellComponent
    private ContextMenuComponent ctxMenu;
    @UsesCellComponent
    private ProximityComponent proximity;
    private ContextMenuItem rightClickMenuItem;
    private ContextMenuFactorySPI ctxMenuFactory;
    private MouseEventListener mouseEventListener;
    /**
     * Indicates whether answer dialog is currently opened (true) or closed
     * (false)
     */
    private boolean answerDlgOpened = false;
    private Cell parentCell;
    private String sheetId;
    private int questionSrc;
    private Question question;
    private FindMethod findMethod;
    ChannelComponent channel = null;
//    public Cell thisCell;//---------------------------------------------------------ADDED FOR ESL AUDIO
    RecordingPanel childPanel;//---------------------------------------------------------ADDED FOR ESL AUDIO

    public QuestionComponent(Cell cell) {

        super(cell);
        this.parentCell = cell;
//        thisCell = cell;
        // register an instance if shared state component
        if (sharedState == null) {
            sharedState = ((SharedStateComponent) cell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class));
        }
    }

    public SharedMapCli getGlobalSharedMap() {
        return globalSharedMap;
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (status == CellStatus.RENDERING) {
            if (mouseEventListener == null) {
                mouseEventListener = new MouseEventListener();
                mouseEventListener.addToEntity(((BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
            }

        } else if (status == CellStatus.ACTIVE && increasing) {
            if (findMethod == null) {
                findMethod = new FindMethod();
            }
            // need to initialize shared map in separate thread to avoid deadlock
            // map hangs waiting for initialization
            Thread th = new Thread(new Runnable() {
                public void run() {
                    globalSharedMap = sharedState.get(sheetId);
                    globalSharedMap.addSharedMapListener(QuestionComponent.this);
                    if (globalSharedMap.get(ScavengerHuntConstants.FIND_METHOD_KEY_NAME) == null) {
                        globalSharedMap.put(ScavengerHuntConstants.FIND_METHOD_KEY_NAME, new SharedFindMethod(new FindMethod()));
                    }

                }
            });
            th.start();
        } else if (status == CellStatus.DISK && !increasing) {
            mouseEventListener.removeFromEntity(((BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        if (clientState != null) {
            sheetId = ((QuestionComponentClientState) clientState).getSheetId();
            questionSrc = ((QuestionComponentClientState) clientState).getQuestionSrc();
            question = ((QuestionComponentClientState) clientState).getQuestion();
        }
    }

    public void actionPerformed(ContextMenuItemEvent event) {
        try {
            onItemFound();
        } catch (IOException ex) {
            Logger.getLogger(QuestionComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void propertyChanged(SharedMapEventCli smec) {
        SharedData newData = smec.getNewValue();
        if (newData != null && newData instanceof SharedFindMethod) {
            FindMethod newfm = ((SharedFindMethod) newData).getMethod();
            if (findMethod == null) {
                setFindMethod(newfm);
            } else if (findMethod.getFindType() != newfm.getFindType() || (findMethod.getParam() != null && !findMethod.getParam().equals(newfm.getParam()))) {
                setFindMethod(newfm);
            }
        }
    }

    private void setFindMethod(FindMethod fm) {
        findMethod = fm;
        switch (fm.getFindType()) {
            case FindMethod.LEFT_CLICK:
                // remove proximity listener and context menu item
                if (proximity != null) {
                    proximity.removeProximityListener(this);
                }
                if (rightClickMenuItem != null) {
                    ctxMenu.removeContextMenuFactory(ctxMenuFactory);
                }
                break;
            case FindMethod.PROXIMITY:
                // remove right click menu item
                if (rightClickMenuItem != null) {
                    ctxMenu.removeContextMenuFactory(ctxMenuFactory);
                }
                // add proximity listener
                if (proximity != null) {
                    BoundingVolume bv = new BoundingSphere(Float.parseFloat(findMethod.getParam()), new Vector3f(0, 0, 0));
                    proximity.addProximityListener(this, new BoundingVolume[]{bv});
                }
                break;
            case FindMethod.RIGHT_CLICK:
                // remove proximity listener
                if (proximity != null) {
                    proximity.removeProximityListener(this);
                }
                // set context menu
                if (ctxMenu != null) {
                    rightClickMenuItem = new SimpleContextMenuItem(findMethod.getParam(), this);
                    ctxMenuFactory = new ContextMenuFactorySPI() {
                        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                            return new ContextMenuItem[]{rightClickMenuItem};
                        }
                    };
                    ctxMenu.addContextMenuFactory(ctxMenuFactory);
                }
                break;
        }
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if (entered) {
            try {
                onItemFound();
            } catch (IOException ex) {
                Logger.getLogger(QuestionComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() && mbe.getButton() == ButtonId.BUTTON1) {
                if (findMethod != null && findMethod.getFindType() == FindMethod.LEFT_CLICK) {
                    try {
                        onItemFound();
                    } catch (IOException ex) {
                        Logger.getLogger(QuestionComponent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void onItemFound() throws IOException {
        // if answer dialog is already opened, return immediately
        if (answerDlgOpened) {
            return;
        }
        Rectangle bounds = JmeClientMain.getFrame().getFrame().getBounds();

        int type = Question.QUESTION_TYPE_STANDARD;
        if (question != null) {
            type = question.getType();
        }

        switch (type) {
            case Question.QUESTION_TYPE_NUMERIC:
                JDialog numdlg = new JDialog(JmeClientMain.getFrame().getFrame(), false);
                numdlg.addWindowListener(new QuestionDialogListener());
                numdlg.setLayout(new BorderLayout());
                numdlg.add(new NumericQuestionPanel(question, numdlg), BorderLayout.CENTER);
                numdlg.setLocation(bounds.x + ScavengerHuntConstants.DLG_OFFSET_X, bounds.y + ScavengerHuntConstants.DLG_OFFSET_Y);
                numdlg.pack();
                numdlg.setVisible(true);
                break;
            case Question.QUESTION_TYPE_CHOICE:
                JDialog choicedlg = new JDialog(JmeClientMain.getFrame().getFrame(), false);
                choicedlg.addWindowListener(new QuestionDialogListener());
                choicedlg.setLayout(new BorderLayout());
                choicedlg.add(new MultipleChoiceQuestionPanel(question, choicedlg), BorderLayout.CENTER);
                choicedlg.setLocation(bounds.x + ScavengerHuntConstants.DLG_OFFSET_X, bounds.y + ScavengerHuntConstants.DLG_OFFSET_Y);
                choicedlg.pack();
                choicedlg.setVisible(true);
                break;
            case Question.QUESTION_TYPE_STANDARD:
                if (questionSrc == ScavengerHuntConstants.QUESTION_SRC_SHEET) {
                    // display sheet HUD components
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (sheetId != null) {
                                Collection<HUDComponent> comps = ISocialManager.INSTANCE.getHUDComponents(sheetId);
                                for (HUDComponent comp : comps) {
                                    comp.setVisible(true);
                                }
                            } else {
                                JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(), "No sheet associated with this item", "Warning", JOptionPane.WARNING_MESSAGE);
                            }

                        }
                    });
                } else if (questionSrc == ScavengerHuntConstants.QUESTION_SRC_CUSTOM) {
                    BorderLayout bl = new BorderLayout();
                    bl.setHgap(10);
                    bl.setVgap(10);
                    JPanel qpanel = new JPanel(bl);
                    String txt = (question != null) ? question.getQuestionText() : "";
                    final boolean includeAnswer = (question != null) ? question.isIncludeAnswer() : false;
                    final boolean includeAudio = (question != null) ? question.isIncludeAudio() : false;
                    // set label to wrap text at 200 px
                    JLabel questionLabel = new JLabel("<html><body style='width: 200px'>" + txt.replaceAll("\\r?\\n", "<br>") + "</html>");
                    qpanel.add(questionLabel, BorderLayout.NORTH);
                    // set image icon, if exists
                    if (question.getImageUrl() != null) {
                        JLabel imageLabel = new JLabel();
                        try {
                            URL url = AssetUtils.getAssetURL(question.getImageUrl());
                            ImageIcon img = new ImageIcon(url);
                            if (img.getIconWidth() > 128) {
                                imageLabel.setIcon(new ImageIcon(img.getImage().getScaledInstance(128, 128, 0)));
                            } else {
                                imageLabel.setIcon(img);
                            }

                        } catch (MalformedURLException ex) {
                            LOGGER.log(Level.SEVERE, "Could not load question image: {0}", ex.getMessage());
                        }
                        qpanel.add(imageLabel, BorderLayout.EAST);
                    }
                    final JTextArea answerArea = new JTextArea();
                    answerArea.setRows(3);
                    answerArea.setLineWrap(true);
                    answerArea.setWrapStyleWord(true);
                    if (question != null) {
                        answerArea.setText(question.getAnswerText());
                    }


                    //---------------------------------------------------------ADDED FOR ESL AUDIO                        
                    final String sheetType = (question != null) ? question.getSheetType() : "NO_SHEET";

                    if (sheetType.equals("A")) {
                    }
                    if (sheetType.equals("B")) {
                        qpanel.add(answerArea, BorderLayout.CENTER);
                    }
                    if (sheetType.equals("C")) {
                        // add audio panel if question supports it
                        if (includeAudio) {
                            if (childPanel == null) {
                                childPanel = new RecordingPanel(cell, ISocialManager.INSTANCE.getUsername().toString());
                            }
                            qpanel.add(childPanel, BorderLayout.CENTER);
                        }
                        // only add answer area if required, otherwise just show confirmation
                        if (includeAnswer) {
                            //render answer
                            answerArea.setText(question.getAnswerText());
                            qpanel.add(answerArea, BorderLayout.SOUTH);
                        }

                    }
                    //---------------------------------------------------------ADDED FOR ESL AUDIO            
                    Object[] options = (includeAnswer || includeAudio) ? new Object[]{"Cancel", "Submit"} : new Object[]{"OK"};
                    final JOptionPane pane = new JOptionPane(qpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null, options, null);
                    String title = (includeAnswer || includeAudio) ? "Quetion" : "Info";
                    final JDialog dlg = pane.createDialog(JmeClientMain.getFrame().getFrame(), title);
                    // position dialog to upper-left corner
                    dlg.setLocation(bounds.x + ScavengerHuntConstants.DLG_OFFSET_X, bounds.y + ScavengerHuntConstants.DLG_OFFSET_Y);
                    dlg.setModal(false);
                    dlg.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentHidden(ComponentEvent e) {
                            answerDlgOpened = false;
                            if (sheetId == null) {
                                JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(), "No sheet associated with this item", "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            boolean flag = true;
                            // submit result
                            try {

                                // Vladimir: I changed this so that question can have either audio or text answer
                                String answer = "";
                                if (includeAudio) {
                                    if (pane.getValue() != null) {
                                        if (pane.getValue().equals("Submit")) {
                                            String filename = childPanel.saveAudioFiles();
                                            if (filename != null) {
                                                answer = "<audio controls='controls'><source src='http://" + ISocialManager.INSTANCE.getSession().getServerNameAndPort()
                                                        + "/webdav/content/users/" + ISocialManager.INSTANCE.getUsername() + "/question-capability-audio/"
                                                        + filename + ".wav' type='audio/wav' />"
                                                        + "<embed height='70px' src='http://" + ISocialManager.INSTANCE.getSession().getServerNameAndPort()
                                                        + "/webdav/content/users/" + ISocialManager.INSTANCE.getUsername() + "/question-capability-audio/"
                                                        + filename + ".wav' autostart='false' >"
                                                        + "</embed></audio>";
                                            } else {
                                                dlg.setVisible(true);
                                                flag = false;
                                            }
                                        } else {
                                            flag = false;
                                        }
                                    } else {
                                        flag = false;
                                    }
                                } else if (includeAnswer) {
                                    answer = answerArea.getText();
                                }
                                if (flag) {
                                    Collection<Result> results = ISocialManager.INSTANCE.getResults(sheetId);
                                    QuestionResultDetails details = new QuestionResultDetails();
                                    Result result = null;
                                    for (Result r : results) {
                                        if (r.getCreator().equals(ISocialManager.INSTANCE.getUsername())) {
                                            result = r;
                                            break;
                                        }
                                    }
                                    if (result == null) {
                                        details.setAnswer(question, answer);
                                        ISocialManager.INSTANCE.submitResult(sheetId, details);
                                    } else {
                                        QuestionResultDetails det = ((QuestionResultDetails) result.getDetails());
                                        det.setAnswer(question, answer);
                                        ISocialManager.INSTANCE.updateResult(result.getId(), det);
                                    }
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(QuestionComponent.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    answerDlgOpened = true;
                    dlg.setVisible(true);
                }
        }


    }

    public EventClassListener getMouseEventListener() {
        return mouseEventListener;
    }
    
    private class QuestionDialogListener extends WindowAdapter {

        /**
         * Trigger animation play when window is closed.
         * 
         * @param we window event
         */
        @Override
        public void windowClosed(WindowEvent we) {
            Collection<CellComponent> comps = cell.getComponents();
            for(CellComponent comp : comps){
                if(comp.getClass().getName().equals("com.wonderbuilders.modules.animation.client.AnimationComponent")){
                    
                    try {
                        Method playMethod = comp.getClass().getDeclaredMethod("toggleAnimation");
                        playMethod.invoke(comp);
                    } catch(Exception ex){
                        LOGGER.log(Level.SEVERE, "Could not play animation: {0}", ex.getMessage());
                    }
                    
                }
            }
        }
        
    }
}
