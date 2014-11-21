/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

/*
 * QuestionComponentProperties.java
 *
 * Created on Mar 30, 2012, 8:49:58 PM
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.bind.JAXBContext;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.ISocialStateUtils;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.AutoQuestionNumber;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.FindMethod;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.Question;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionSheetDetails;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedFindMethod;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.MultipleChoiceConfigPanel;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.AutoQuestionChoice;

/**
 *
 * @author Vladimir Djurovic
 */
@PropertiesFactory(QuestionComponentServerState.class)
public class QuestionComponentProperties extends JPanel implements PropertiesFactorySPI {

    /** Resource bundle containing component configuration data. */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ScavengerHuntConstants.BUNDLE_PATH);
    private static final Logger LOGGER = Logger.getLogger(ScavengerHuntComponentProperties.class.getName());
    private CellPropertiesEditor editor = null;
    private String sheetId;
    private String unitId;
    private String lessonId;
    private Sheet questionSheet;
    private int questionSrc = 0;
    private FindMethod findMethod;
    private SharedMapCli globalSharedMap;
    private String imageUrl;
    private MultiChoicePanel multiChoicePanel;
    private Color imageLabelBgColor;

    /** Creates new form QuestionComponentProperties */
    public QuestionComponentProperties() {
        initComponents();
        sheetsCombo.setRenderer(new SheetComboBoxRenderer());
        sheetsCombo.addItemListener(new ChangeItemListener());
        leftClickRB.addItemListener(new ChangeItemListener());
        proximityRB.addItemListener(new ChangeItemListener());
        rightClickRB.addItemListener(new ChangeItemListener());
        feedbackTable.getModel().addTableModelListener(new FeedbackTableModelListener());
        multiChoicePanel = new MultiChoicePanel();
        choiceScrollPane.setViewportView(multiChoicePanel);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        triggerButtonGroup = new javax.swing.ButtonGroup();
        sourceButtonGroup = new javax.swing.ButtonGroup();
        answerTypeButtonGroup = new javax.swing.ButtonGroup();
        numericButtonGroup = new javax.swing.ButtonGroup();
        questionTypeButtonGroup = new javax.swing.ButtonGroup();
        questionConfigTitle = new javax.swing.JLabel();
        triggerLabel = new javax.swing.JLabel();
        leftClickRB = new javax.swing.JRadioButton();
        proximityRB = new javax.swing.JRadioButton();
        rightClickRB = new javax.swing.JRadioButton();
        distanceTextField = new javax.swing.JTextField();
        metersLabel = new javax.swing.JLabel();
        selectiontextField = new javax.swing.JTextField();
        questionTypePane = new javax.swing.JTabbedPane();
        standardQuestionPanel = new javax.swing.JPanel();
        customSrcRB = new javax.swing.JRadioButton();
        sheetSrcRB = new javax.swing.JRadioButton();
        sheetLabel = new javax.swing.JLabel();
        sheetsCombo = new javax.swing.JComboBox();
        noAnswerRB = new javax.swing.JRadioButton();
        includeTextBoxRB = new javax.swing.JRadioButton();
        includeRecButtonRB = new javax.swing.JRadioButton();
        choiceQuestionPanel = new javax.swing.JPanel();
        addAnswerButton = new javax.swing.JButton();
        choiceScrollPane = new javax.swing.JScrollPane();
        numericQuestionPanel = new javax.swing.JPanel();
        exactAnwerRB = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        unitField = new javax.swing.JTextField();
        rangeAnswerRB = new javax.swing.JRadioButton();
        minTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        maxTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        feedbackTable = new javax.swing.JTable();
        exactTextField = new javax.swing.JTextField();
        feedbackPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        globalCorrectField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        globalWrongField = new javax.swing.JTextField();
        questionTitleLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        questionTextArea = new javax.swing.JTextArea();
        standardRb = new javax.swing.JRadioButton();
        choiceRB = new javax.swing.JRadioButton();
        numericRB = new javax.swing.JRadioButton();
        imageLabel = new javax.swing.JLabel();

        questionConfigTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/isocial/scavenger/client/components/strings"); // NOI18N
        questionConfigTitle.setText(bundle.getString("QuestionComponentProperties.questionConfigTitle.text")); // NOI18N

        triggerLabel.setText(bundle.getString("QuestionComponentProperties.triggerLabel.text")); // NOI18N

        triggerButtonGroup.add(leftClickRB);
        leftClickRB.setSelected(true);
        leftClickRB.setText(bundle.getString("QuestionComponentProperties.leftClickRB.text")); // NOI18N
        leftClickRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftClickRBActionPerformed(evt);
            }
        });

        triggerButtonGroup.add(proximityRB);
        proximityRB.setText(bundle.getString("QuestionComponentProperties.proximityRB.text")); // NOI18N
        proximityRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proximityRBActionPerformed(evt);
            }
        });

        triggerButtonGroup.add(rightClickRB);
        rightClickRB.setText(bundle.getString("QuestionComponentProperties.rightClickRB.text")); // NOI18N
        rightClickRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightClickRBActionPerformed(evt);
            }
        });

        distanceTextField.setText(bundle.getString("QuestionComponentProperties.distanceTextField.text")); // NOI18N

        metersLabel.setText(bundle.getString("QuestionComponentProperties.metersLabel.text")); // NOI18N

        selectiontextField.setText(bundle.getString("QuestionComponentProperties.selectiontextField.text")); // NOI18N

        standardQuestionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("QuestionComponentProperties.standardQuestionPanel.border.title"))); // NOI18N

        sourceButtonGroup.add(customSrcRB);
        customSrcRB.setText(bundle.getString("QuestionComponentProperties.customSrcRB.text")); // NOI18N
        customSrcRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customSrcRBActionPerformed(evt);
            }
        });

        sourceButtonGroup.add(sheetSrcRB);
        sheetSrcRB.setText(bundle.getString("QuestionComponentProperties.sheetSrcRB.text")); // NOI18N
        sheetSrcRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sheetSrcRBActionPerformed(evt);
            }
        });

        sheetLabel.setText(bundle.getString("QuestionComponentProperties.sheetLabel.text")); // NOI18N

        sheetsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        answerTypeButtonGroup.add(noAnswerRB);
        noAnswerRB.setText(bundle.getString("QuestionComponentProperties.noAnswerRB.text")); // NOI18N
        noAnswerRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noAnswerRBActionPerformed(evt);
            }
        });

        answerTypeButtonGroup.add(includeTextBoxRB);
        includeTextBoxRB.setText(bundle.getString("QuestionComponentProperties.includeTextBoxRB.text")); // NOI18N
        includeTextBoxRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeTextBoxRBActionPerformed(evt);
            }
        });

        answerTypeButtonGroup.add(includeRecButtonRB);
        includeRecButtonRB.setText(bundle.getString("QuestionComponentProperties.includeRecButtonRB.text")); // NOI18N
        includeRecButtonRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeRecButtonRBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout standardQuestionPanelLayout = new javax.swing.GroupLayout(standardQuestionPanel);
        standardQuestionPanel.setLayout(standardQuestionPanelLayout);
        standardQuestionPanelLayout.setHorizontalGroup(
            standardQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(standardQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(standardQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(sheetsCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, standardQuestionPanelLayout.createSequentialGroup()
                        .addGroup(standardQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(customSrcRB, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, standardQuestionPanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(standardQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(noAnswerRB)
                                    .addComponent(includeTextBoxRB)
                                    .addComponent(includeRecButtonRB)))
                            .addComponent(sheetSrcRB, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sheetLabel, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(72, 72, 72)))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        standardQuestionPanelLayout.setVerticalGroup(
            standardQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(standardQuestionPanelLayout.createSequentialGroup()
                .addComponent(customSrcRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noAnswerRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(includeTextBoxRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(includeRecButtonRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sheetSrcRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sheetLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sheetsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(124, 124, 124))
        );

        questionTypePane.addTab(bundle.getString("QuestionComponentProperties.standardQuestionPanel.TabConstraints.tabTitle"), standardQuestionPanel); // NOI18N

        addAnswerButton.setText(bundle.getString("QuestionComponentProperties.addAnswerButton.text")); // NOI18N
        addAnswerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAnswerButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout choiceQuestionPanelLayout = new javax.swing.GroupLayout(choiceQuestionPanel);
        choiceQuestionPanel.setLayout(choiceQuestionPanelLayout);
        choiceQuestionPanelLayout.setHorizontalGroup(
            choiceQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choiceQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(choiceQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choiceScrollPane)
                    .addGroup(choiceQuestionPanelLayout.createSequentialGroup()
                        .addComponent(addAnswerButton)
                        .addGap(0, 235, Short.MAX_VALUE)))
                .addContainerGap())
        );
        choiceQuestionPanelLayout.setVerticalGroup(
            choiceQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(choiceQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addAnswerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choiceScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        questionTypePane.addTab(bundle.getString("QuestionComponentProperties.choiceQuestionPanel.TabConstraints.tabTitle"), choiceQuestionPanel); // NOI18N

        numericButtonGroup.add(exactAnwerRB);
        exactAnwerRB.setSelected(true);
        exactAnwerRB.setText(bundle.getString("QuestionComponentProperties.exactAnwerRB.text")); // NOI18N
        exactAnwerRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exactAnwerRBActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("QuestionComponentProperties.jLabel1.text")); // NOI18N

        unitField.setColumns(3);
        unitField.setText(bundle.getString("QuestionComponentProperties.unitField.text")); // NOI18N
        unitField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                unitFieldKeyPressed(evt);
            }
        });

        numericButtonGroup.add(rangeAnswerRB);
        rangeAnswerRB.setText(bundle.getString("QuestionComponentProperties.rangeAnswerRB.text")); // NOI18N
        rangeAnswerRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rangeAnswerRBActionPerformed(evt);
            }
        });

        minTextField.setColumns(4);
        minTextField.setText(bundle.getString("QuestionComponentProperties.minTextField.text")); // NOI18N
        minTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                minTextFieldKeyTyped(evt);
            }
        });

        jLabel3.setText(bundle.getString("QuestionComponentProperties.jLabel3.text")); // NOI18N

        maxTextField.setColumns(4);
        maxTextField.setText(bundle.getString("QuestionComponentProperties.maxTextField.text")); // NOI18N
        maxTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                maxTextFieldKeyPressed(evt);
            }
        });

        jLabel5.setText(bundle.getString("QuestionComponentProperties.jLabel5.text")); // NOI18N

        feedbackTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Too Low", "Correct", "Too High"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        feedbackTable.setGridColor(new java.awt.Color(153, 153, 153));
        feedbackTable.setRowHeight(20);
        feedbackTable.setRowSelectionAllowed(false);
        feedbackTable.setShowGrid(true);
        feedbackTable.setShowHorizontalLines(false);
        jScrollPane3.setViewportView(feedbackTable);

        exactTextField.setColumns(4);
        exactTextField.setText(bundle.getString("QuestionComponentProperties.exactTextField.text")); // NOI18N
        exactTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                exactTextFieldKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout numericQuestionPanelLayout = new javax.swing.GroupLayout(numericQuestionPanel);
        numericQuestionPanel.setLayout(numericQuestionPanelLayout);
        numericQuestionPanelLayout.setHorizontalGroup(
            numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unitField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                            .addGap(23, 23, 23)
                            .addComponent(jLabel5)))
                    .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                        .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rangeAnswerRB)
                            .addComponent(exactAnwerRB))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(exactTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                                .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        numericQuestionPanelLayout.setVerticalGroup(
            numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(numericQuestionPanelLayout.createSequentialGroup()
                .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exactAnwerRB)
                    .addComponent(exactTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rangeAnswerRB)
                    .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(numericQuestionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(unitField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 58, Short.MAX_VALUE))
        );

        questionTypePane.addTab(bundle.getString("QuestionComponentProperties.numericQuestionPanel.TabConstraints.tabTitle"), numericQuestionPanel); // NOI18N

        jLabel6.setText(bundle.getString("QuestionComponentProperties.jLabel6.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(bundle.getString("QuestionComponentProperties.jLabel7.text")); // NOI18N
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel8.setBackground(new java.awt.Color(153, 153, 153));
        jLabel8.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/check-mark.png"))); // NOI18N
        jLabel8.setText(bundle.getString("QuestionComponentProperties.jLabel8.text")); // NOI18N
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        globalCorrectField.setText(bundle.getString("QuestionComponentProperties.globalCorrectField.text")); // NOI18N

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/Red_x.png"))); // NOI18N
        jLabel9.setText(bundle.getString("QuestionComponentProperties.jLabel9.text")); // NOI18N
        jLabel9.setToolTipText(bundle.getString("QuestionComponentProperties.jLabel9.toolTipText")); // NOI18N
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        globalWrongField.setText(bundle.getString("QuestionComponentProperties.globalWrongField.text")); // NOI18N

        javax.swing.GroupLayout feedbackPanelLayout = new javax.swing.GroupLayout(feedbackPanel);
        feedbackPanel.setLayout(feedbackPanelLayout);
        feedbackPanelLayout.setHorizontalGroup(
            feedbackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(feedbackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(feedbackPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel9))
                    .addComponent(globalCorrectField, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 1032, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(globalWrongField, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        feedbackPanelLayout.setVerticalGroup(
            feedbackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(feedbackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(globalCorrectField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(globalWrongField, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        questionTypePane.addTab(bundle.getString("QuestionComponentProperties.feedbackPanel.TabConstraints.tabTitle"), feedbackPanel); // NOI18N

        questionTitleLabel.setText(bundle.getString("QuestionComponentProperties.questionTitleLabel.text")); // NOI18N

        questionTextArea.setColumns(20);
        questionTextArea.setLineWrap(true);
        questionTextArea.setRows(5);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                questionTextAreaKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(questionTextArea);

        questionTypeButtonGroup.add(standardRb);
        standardRb.setSelected(true);
        standardRb.setText(bundle.getString("QuestionComponentProperties.standardRb.text")); // NOI18N
        standardRb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standardRbActionPerformed(evt);
            }
        });

        questionTypeButtonGroup.add(choiceRB);
        choiceRB.setText(bundle.getString("QuestionComponentProperties.choiceRB.text")); // NOI18N
        choiceRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                choiceRBActionPerformed(evt);
            }
        });

        questionTypeButtonGroup.add(numericRB);
        numericRB.setText(bundle.getString("QuestionComponentProperties.numericRB.text")); // NOI18N
        numericRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numericRBActionPerformed(evt);
            }
        });

        imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageLabel.setText(bundle.getString("QuestionComponentProperties.imageLabel.text")); // NOI18N
        imageLabel.setToolTipText(bundle.getString("QuestionComponentProperties.imageLabel.toolTipText")); // NOI18N
        imageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageLabelMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                imageLabelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                imageLabelMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(questionTypePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(questionConfigTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(triggerLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(leftClickRB)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(rightClickRB)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(selectiontextField))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(proximityRB)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(metersLabel))))
                                .addGap(0, 30, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(questionTitleLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(standardRb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(choiceRB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numericRB))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(questionConfigTitle)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(triggerLabel)
                    .addComponent(leftClickRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proximityRB)
                    .addComponent(distanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(metersLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rightClickRB)
                    .addComponent(selectiontextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(questionTitleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(standardRb)
                    .addComponent(choiceRB)
                    .addComponent(numericRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(questionTypePane, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void leftClickRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftClickRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_leftClickRBActionPerformed

    private void proximityRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_proximityRBActionPerformed

    private void rightClickRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightClickRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_rightClickRBActionPerformed

    private void customSrcRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customSrcRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_customSrcRBActionPerformed

    private void sheetSrcRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sheetSrcRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_sheetSrcRBActionPerformed

    private void questionTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_questionTextAreaKeyPressed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_questionTextAreaKeyPressed

    private void noAnswerRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noAnswerRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_noAnswerRBActionPerformed

    private void includeTextBoxRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeTextBoxRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_includeTextBoxRBActionPerformed

    private void includeRecButtonRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeRecButtonRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_includeRecButtonRBActionPerformed

    private void standardRbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardRbActionPerformed

        questionTypePane.removeAll();
        questionTypePane.add("Standard", standardQuestionPanel);
        imageLabel.setEnabled(false);
    }//GEN-LAST:event_standardRbActionPerformed

    private void choiceRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choiceRBActionPerformed

       questionTypePane.removeAll();
       questionTypePane.add("Multiple Choice", choiceQuestionPanel);
       questionTypePane.add("Feedback", feedbackPanel);
        imageLabel.setEnabled(true);
        clearChoicePanel();
    }//GEN-LAST:event_choiceRBActionPerformed

    private void numericRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numericRBActionPerformed

        questionTypePane.removeAll();
       questionTypePane.add("Numeric", numericQuestionPanel);
       questionTypePane.add("Feedback", feedbackPanel);
        imageLabel.setEnabled(true);
        clearNumericPanel();
    }//GEN-LAST:event_numericRBActionPerformed

    private void exactAnwerRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exactAnwerRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
        boolean rangeStatus = !exactAnwerRB.isSelected();
        exactTextField.setEnabled(!rangeStatus);
        minTextField.setText("");
        minTextField.setEnabled(rangeStatus);
        maxTextField.setText("");
        maxTextField.setEnabled(rangeStatus);
    }//GEN-LAST:event_exactAnwerRBActionPerformed

    private void rangeAnswerRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rangeAnswerRBActionPerformed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
        boolean exactStatus = !rangeAnswerRB.isSelected();
        exactTextField.setText("");
        exactTextField.setEnabled(exactStatus);
        minTextField.setEnabled(!exactStatus);
        maxTextField.setEnabled(!exactStatus);
    }//GEN-LAST:event_rangeAnswerRBActionPerformed

    private void exactTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_exactTextFieldKeyTyped
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_exactTextFieldKeyTyped

    private void minTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minTextFieldKeyTyped
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_minTextFieldKeyTyped

    private void maxTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxTextFieldKeyPressed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_maxTextFieldKeyPressed

    private void unitFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_unitFieldKeyPressed
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_unitFieldKeyPressed

    private void addAnswerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAnswerButtonActionPerformed
        multiChoicePanel.addRow();
        editor.setPanelDirty(QuestionComponentProperties.class, true);
    }//GEN-LAST:event_addAnswerButtonActionPerformed

    private void imageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageLabelMouseClicked
        if (imageLabel.isEnabled()) {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Choose image");

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selFile = chooser.getSelectedFile();
                try {
                    imageUrl = uploadImage(selFile);
                    setQuestionImage();
                    editor.setPanelDirty(QuestionComponentProperties.class, true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not upload file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }//GEN-LAST:event_imageLabelMouseClicked

    private void imageLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageLabelMouseEntered
        imageLabel.setForeground(Color.WHITE);
    }//GEN-LAST:event_imageLabelMouseEntered

    private void imageLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageLabelMouseExited
        imageLabel.setForeground(Color.BLACK);
    }//GEN-LAST:event_imageLabelMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAnswerButton;
    private javax.swing.ButtonGroup answerTypeButtonGroup;
    private javax.swing.JPanel choiceQuestionPanel;
    private javax.swing.JRadioButton choiceRB;
    private javax.swing.JScrollPane choiceScrollPane;
    private javax.swing.JRadioButton customSrcRB;
    private javax.swing.JTextField distanceTextField;
    private javax.swing.JRadioButton exactAnwerRB;
    private javax.swing.JTextField exactTextField;
    private javax.swing.JPanel feedbackPanel;
    private javax.swing.JTable feedbackTable;
    private javax.swing.JTextField globalCorrectField;
    private javax.swing.JTextField globalWrongField;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JRadioButton includeRecButtonRB;
    private javax.swing.JRadioButton includeTextBoxRB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JRadioButton leftClickRB;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JLabel metersLabel;
    private javax.swing.JTextField minTextField;
    private javax.swing.JRadioButton noAnswerRB;
    private javax.swing.ButtonGroup numericButtonGroup;
    private javax.swing.JPanel numericQuestionPanel;
    private javax.swing.JRadioButton numericRB;
    private javax.swing.JRadioButton proximityRB;
    private javax.swing.JLabel questionConfigTitle;
    private javax.swing.JTextArea questionTextArea;
    private javax.swing.JLabel questionTitleLabel;
    private javax.swing.ButtonGroup questionTypeButtonGroup;
    private javax.swing.JTabbedPane questionTypePane;
    private javax.swing.JRadioButton rangeAnswerRB;
    private javax.swing.JRadioButton rightClickRB;
    private javax.swing.JTextField selectiontextField;
    private javax.swing.JLabel sheetLabel;
    private javax.swing.JRadioButton sheetSrcRB;
    private javax.swing.JComboBox sheetsCombo;
    private javax.swing.ButtonGroup sourceButtonGroup;
    private javax.swing.JPanel standardQuestionPanel;
    private javax.swing.JRadioButton standardRb;
    private javax.swing.ButtonGroup triggerButtonGroup;
    private javax.swing.JLabel triggerLabel;
    private javax.swing.JTextField unitField;
    // End of variables declaration//GEN-END:variables

    public String getDisplayName() {
        return BUNDLE.getString(ScavengerHuntConstants.PROP_QUESTION_NAME);
    }

    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    public JPanel getPropertiesJPanel() {
        return this;
    }

    public void open() {
        CellComponentServerState state = editor.getCellServerState().getComponentServerState(QuestionComponentServerState.class);
        if (state != null) {
            sheetId = ((QuestionComponentServerState) state).getSheetId();
            questionSrc = ((QuestionComponentServerState) state).getQuestionSrc();
        }
        // set combo box model
        try {
            List<Sheet> sheets = ISocialManager.INSTANCE.getCurrentInstance().getSheets();
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            int count = 0;
            // index of selected sheet, if any
            int index = -1;
            unitId = ISocialManager.INSTANCE.getCurrentInstance().getUnit().getId();
            lessonId = ISocialManager.INSTANCE.getCurrentInstance().getLesson().getId();
            for (Sheet sh : sheets) {
                model.addElement(sh);
                if(sh.getDetails() instanceof QuestionSheetDetails){
                    questionSheet = sh;
                }
                if (sheetId != null && sheetId.equals(sh.getId())) {
                    index = count;
                }
                count++;
            }
            // append  "add new" element 
            model.addElement("---------");
            model.addElement("Add new sheet...");
            sheetsCombo.setModel(model);
            sheetsCombo.setSelectedIndex(index);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        globalSharedMap = editor.getCell().getComponent(QuestionComponent.class).getGlobalSharedMap();
        if (globalSharedMap != null) {
            if(globalSharedMap.containsKey(ScavengerHuntConstants.FIND_METHOD_KEY_NAME)){
                findMethod = ((SharedFindMethod)globalSharedMap.get(ScavengerHuntConstants.FIND_METHOD_KEY_NAME)).getMethod();
            }
        }

        if (findMethod == null) {
            findMethod = new FindMethod();
        }

        switch (findMethod.getFindType()) {
            case FindMethod.LEFT_CLICK:
                leftClickRB.setSelected(true);
                distanceTextField.setText(null);
                selectiontextField.setText(null);
                break;
            case FindMethod.PROXIMITY:
                proximityRB.setSelected(true);
                distanceTextField.setText(findMethod.getParam());
                selectiontextField.setText(null);
                break;
            case FindMethod.RIGHT_CLICK:
                rightClickRB.setSelected(true);
                distanceTextField.setText(null);
                selectiontextField.setText(findMethod.getParam());
                break;
        }
        imageLabelBgColor = imageLabel.getBackground();
        Question question = ((QuestionComponentServerState) state).getQuestion();
        int type = Question.QUESTION_TYPE_STANDARD;
        if(question != null){
            type = question.getType();
        }
        
         switch (type) {
            case Question.QUESTION_TYPE_STANDARD:
                standardRb.setSelected(true);
                imageUrl = null;
                populateStandardFields(question);
                 questionTypePane.removeAll();
                questionTypePane.add("Standard", standardQuestionPanel);
                break;
            case Question.QUESTION_TYPE_NUMERIC:
                numericRB.setSelected(true);
                populateNumericFields(question);
                 questionTypePane.removeAll();
                 questionTypePane.add("Numeric", numericQuestionPanel);
                questionTypePane.add("Feedback", feedbackPanel);
                break;
             case Question.QUESTION_TYPE_CHOICE:
                 choiceRB.setSelected(true);
                 populateChoiceFields(question);
                 questionTypePane.removeAll();
                 questionTypePane.add("Multiple Choice", choiceQuestionPanel);
                 questionTypePane.add("Feedback", feedbackPanel);
                 break;
        }
        
        editor.setPanelDirty(QuestionComponentProperties.class, false);
    }

    public void close() {
    }

    public void restore() {
    }
    

    public void apply() {
        CellComponentServerState state = editor.getCellServerState().getComponentServerState(QuestionComponentServerState.class);
        if (state == null) {
            state = new QuestionComponentServerState();
        }
        // create new Find method
        FindMethod newfind = null;
        if (leftClickRB.isSelected()) {
            newfind = new FindMethod(FindMethod.LEFT_CLICK, null, null);
        } else if (proximityRB.isSelected()) {
            newfind = new FindMethod(FindMethod.PROXIMITY, "proximity", distanceTextField.getText());
        } else {
            newfind = new FindMethod(FindMethod.RIGHT_CLICK, "rightClick", selectiontextField.getText());
        }
        // update sheet with new find method
        if (globalSharedMap == null) {
            globalSharedMap = editor.getCell().getComponent(QuestionComponent.class).getGlobalSharedMap();
        }
        globalSharedMap.put(ScavengerHuntConstants.FIND_METHOD_KEY_NAME, new SharedFindMethod(newfind));
        findMethod = newfind;
         Question question = new Question(editor.getCell().getCellID().toString(), questionTextArea.getText());
         question.setImageUrl(imageUrl);
        // set selected question source
        if (standardRb.isSelected()) {
            question.setType(Question.QUESTION_TYPE_STANDARD);
            if (customSrcRB.isSelected()) {
                questionSrc = ScavengerHuntConstants.QUESTION_SRC_CUSTOM;
                question.setIncludeAnswer(includeTextBoxRB.isSelected());
                question.setIncludeAudio(includeRecButtonRB.isSelected());
                StringBuilder sb = new StringBuilder("http://").append(ISocialManager.INSTANCE.getSession().getServerNameAndPort()).
                        append("/isocial-sheets/isocial-sheets/resources/sheets");


                // add default question sheet if none exists
                if (questionSheet == null) {
                    questionSheet = new Sheet(unitId, lessonId);
                    questionSheet.setPublished(true);
                    QuestionSheetDetails details = new QuestionSheetDetails();
                    details.addQuestion(question);
                    questionSheet.setDetails(details);
                    // construct new sheet URL
                    sb.append("/").append(unitId).append("/").append(lessonId).append("/new");
                } else {
                    sheetId = questionSheet.getId();
                    ((QuestionSheetDetails) questionSheet.getDetails()).addQuestion(question);
                    // contruct update URL
                    sb.append("/").append(unitId).append("/").append(lessonId).append("/").append(sheetId);
                }
                // create or update sheet
                try {
                    URL url = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/xml");
                    // write sheet XML data
                    OutputStream out = conn.getOutputStream();
                    JAXBContext ctx = ISocialStateUtils.createContext(ISocialManager.INSTANCE.getSession().getClassloader());
                    ctx.createMarshaller().marshal(questionSheet, out);
                    out.flush();

                    int response = conn.getResponseCode();
                    // if new sheet  was created, set sheet ID
                    if (response == 201) {
                        Sheet newSheet = (Sheet) ctx.createUnmarshaller().unmarshal(conn.getInputStream());
                        sheetId = newSheet.getId();
                    }
                    // check HTTP response status code
                    if (response != 200 && response != 201) {
                        JOptionPane.showMessageDialog(this, "Could not update default Question sheet. Response code " + response,
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    System.out.println("CATCH");
                    throw new RuntimeException(ex);
                }

            } else if (sheetSrcRB.isSelected()) {
                // set selected sheet id
                sheetId = ((Sheet) sheetsCombo.getSelectedItem()).getId();
                questionSrc = ScavengerHuntConstants.QUESTION_SRC_SHEET;
                ((QuestionComponentServerState) state).setQuestion(null);
            }
        } else if(choiceRB.isSelected()){
            if(!multiChoicePanel.validateInput()){
                JOptionPane.showMessageDialog(this, "Please fill in each answer field and select correct answer", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            AutoQuestionChoice autoQuestion = new AutoQuestionChoice();
            multiChoicePanel.populateOptions(autoQuestion);
            autoQuestion.setImageUrl(imageUrl);
            autoQuestion.setGlobalFeedbackCorrect(globalCorrectField.getText());
            autoQuestion.setGlobalFeedbackWrong(globalWrongField.getText());
            
            question.setMultipleChoiceQuestion(autoQuestion);
            question.setNumericQuestion(null);
            question.setType(Question.QUESTION_TYPE_CHOICE);
            
        } else if(numericRB.isSelected()){
            AutoQuestionNumber autoQuestion = new AutoQuestionNumber();
            autoQuestion.setUnit(unitField.getText());
            autoQuestion.setFeedbackLow((String)feedbackTable.getValueAt(0, 0));
            autoQuestion.setFeedbackCorrect((String)feedbackTable.getValueAt(0, 1));
            autoQuestion.setFeedbackHigh((String)feedbackTable.getValueAt(0, 2));
            if(exactAnwerRB.isSelected()){
                autoQuestion.setQuestionType(AutoQuestionNumber.TYPE_EXACT);
                autoQuestion.setExactValue(Float.parseFloat(exactTextField.getText()));
            } else {
                autoQuestion.setQuestionType(AutoQuestionNumber.TYPE_RANGE);
                autoQuestion.setRangeMin(Float.parseFloat(minTextField.getText()));
                autoQuestion.setRangeMax(Float.parseFloat(maxTextField.getText()));
            }
            autoQuestion.setGlobalFeedbackCorrect(globalCorrectField.getText());
            autoQuestion.setGlobalFeedbackWrong(globalWrongField.getText());
            autoQuestion.setImageUrl(imageUrl);
            question.setNumericQuestion(autoQuestion);
            question.setMultipleChoiceQuestion(null);
            question.setType(Question.QUESTION_TYPE_NUMERIC);
           
        }
       
        ((QuestionComponentServerState) state).setQuestion(question);
        ((QuestionComponentServerState) state).setSheetId(sheetId);
        ((QuestionComponentServerState) state).setQuestionSrc(questionSrc);

        editor.addToUpdateList(state);
    }

    private class SheetComboBoxRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Sheet) {
                ((JLabel) comp).setText(((Sheet) value).getName());
            }
            return comp;
        }
    }
    
    /**
     * Populate GUI fields for standard question.
     * 
     * @param question question
     */
    private void populateStandardFields(Question question) {
        //set correct source radio button
        if (questionSrc == ScavengerHuntConstants.QUESTION_SRC_CUSTOM) {
            customSrcRB.setSelected(true);
            if (question != null) {
                imageUrl = question.getImageUrl();
                questionTextArea.setText(question.getQuestionText());
                if (question.isIncludeAnswer()) {
                    includeTextBoxRB.setSelected(true);
                } else if (question.isIncludeAudio()) {
                    includeRecButtonRB.setSelected(true);
                } else {
                    noAnswerRB.setSelected(true);
                }
            } else {
                questionTextArea.setText(null);
                noAnswerRB.setSelected(true);
            }

        } else if (questionSrc == ScavengerHuntConstants.QUESTION_SRC_SHEET) {
            sheetSrcRB.setSelected(true);
            questionTextArea.setText(null);
        }
        
        setQuestionImage();
    }
    
    /**
     * Populate GUI fields for numeric question.
     * 
     * @param question question
     */
    private void populateNumericFields(Question question){
        AutoQuestionNumber autoQuestion = question.getNumericQuestion();
        questionTextArea.setText(question.getQuestionText());
        if(autoQuestion != null){
            int type = autoQuestion.getQuestionType();
            imageUrl = autoQuestion.getImageUrl();
            setQuestionImage();
            unitField.setText(autoQuestion.getUnit());
            feedbackTable.setValueAt(autoQuestion.getFeedbackLow(), 0, 0);
            feedbackTable.setValueAt(autoQuestion.getFeedbackCorrect(), 0, 1);
            feedbackTable.setValueAt(autoQuestion.getFeedbackHigh(), 0, 2);
            globalCorrectField.setText(autoQuestion.getGlobalFeedbackCorrect());
            globalWrongField.setText(autoQuestion.getGlobalFeedbackWrong());
            if(type == AutoQuestionNumber.TYPE_EXACT){
                exactAnwerRB.setSelected(true);
                exactTextField.setText(Float.toString(autoQuestion.getExactValue()));
                minTextField.setEnabled(false);
                maxTextField.setEnabled(false);
            } else {
                rangeAnswerRB.setSelected(true);
                minTextField.setText(Float.toString(autoQuestion.getRangeMin()));
                maxTextField.setText(Float.toString(autoQuestion.getRangeMax()));
                exactTextField.setEnabled(false);
            }
        } else {
           clearNumericPanel();
        }
    }
    
    private void clearNumericPanel(){
         unitField.setText(null);
            feedbackTable.setValueAt(null, 0, 0);
            feedbackTable.setValueAt(null, 0, 1);
            feedbackTable.setValueAt(null, 0, 2);
            exactTextField.setText(null);
            minTextField.setText(null);
            maxTextField.setText(null);
    }
    
    private void populateChoiceFields(Question question){
        AutoQuestionChoice autoQuestion = question.getMultipleChoiceQuestion();
        questionTextArea.setText(question.getQuestionText());
        if(autoQuestion != null){
            imageUrl = autoQuestion.getImageUrl();
            setQuestionImage();
            multiChoicePanel.createFromQuestion(autoQuestion);
            globalCorrectField.setText(autoQuestion.getGlobalFeedbackCorrect());
            globalWrongField.setText(autoQuestion.getGlobalFeedbackWrong());
        } else {
            clearChoicePanel();
        }
    }
    
    private void clearChoicePanel(){
        multiChoicePanel = new MultiChoicePanel();
        choiceScrollPane.setViewportView(multiChoicePanel);
    }
    
    private void setQuestionImage(){
        if(imageUrl != null){
            try {
                URL url = AssetUtils.getAssetURL(imageUrl);
                ImageIcon img = new ImageIcon(url);
                imageLabel.setText(null);
                if (img.getIconWidth() > 64) {
                    imageLabel.setIcon(new ImageIcon(img.getImage().getScaledInstance(64, 64, 0)));
                } else {
                    imageLabel.setIcon(img);
                }

            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, "Could not load question image: " + ex.getMessage());
            }
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("<html><body style='text-align: center;'>Click to<br/>add image</body></html>");
        }
        
    }

    /**
     * Marks panel dirty on item change.
     */
    private class ChangeItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            editor.setPanelDirty(QuestionComponentProperties.class, true);
        }
    }
    
    private class FeedbackTableModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent tme) {
            editor.setPanelDirty(QuestionComponentProperties.class, true);
        }
        
    }
    
    /**
     * Upload image to server.
     * 
     * @param file image file
     * @return  image URL in repository
     */
    private String uploadImage(File file) throws ContentRepositoryException, IOException{
        WonderlandSession session = editor.getCell().getCellCache().getSession();
        ContentRepositoryRegistry repoReg = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = repoReg.getRepository(session.getSessionManager());
        
        ContentCollection root = repo.getUserRoot();
        ContentResource node = (ContentResource)root.getChild(file.getName());
        if(node == null){
            node = (ContentResource)root.createChild(file.getName(), ContentNode.Type.RESOURCE);
        }
        node.put(file);
        return "wlcontent:/" + node.getPath();
    }
    
    public class MultiChoicePanel extends JPanel {
        
        private JLabel answerLabel;
        private JLabel correctLabel;
        private JLabel feedbackLabel;
        private MultipleChoiceConfigPanel firstRowPanel;
        private JPanel container;
        private ButtonGroup bgroup;
        private List<MultipleChoiceConfigPanel> panels;

        public MultiChoicePanel() {
            setLayout(new BorderLayout());
            bgroup = new ButtonGroup();
            panels = new ArrayList<MultipleChoiceConfigPanel>();
            
            container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            answerLabel = new JLabel("Answer");
            correctLabel = new JLabel(new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/icons/check-mark.png")));
            feedbackLabel = new JLabel("Feedback (Optional)"); 
            titlePanel.add(answerLabel);
            titlePanel.add(correctLabel);
            titlePanel.add(feedbackLabel);
            
            container.add(titlePanel);
            firstRowPanel = new MultipleChoiceConfigPanel(this);
            panels.add(firstRowPanel);
            bgroup.add(firstRowPanel.getCorrectRB());
            container.add(firstRowPanel);
            add(container, BorderLayout.NORTH);
            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent ce) {
                    adjustLayout();
                }
                
            });
        }
        
        void adjustLayout(){
            answerLabel.setMinimumSize(new Dimension(firstRowPanel.getAnswerWidth(), answerLabel.getHeight()));
            answerLabel.setPreferredSize(new Dimension(firstRowPanel.getAnswerWidth(), answerLabel.getHeight()));
            answerLabel.setMaximumSize(new Dimension(firstRowPanel.getAnswerWidth(), answerLabel.getHeight()));
            
            correctLabel.setMinimumSize(new Dimension(firstRowPanel.getCorrectWidth(), correctLabel.getHeight()));
            correctLabel.setPreferredSize(new Dimension(firstRowPanel.getCorrectWidth(), correctLabel.getHeight()));
            correctLabel.setMaximumSize(new Dimension(firstRowPanel.getCorrectWidth(), correctLabel.getHeight()));
            
            container.validate();
            choiceScrollPane.setViewportView(multiChoicePanel);
        }
        
       public  void addRow(){
           MultipleChoiceConfigPanel panel = new MultipleChoiceConfigPanel(this);
            container.add(panel);
            panels.add(panel);
            bgroup.add(panel.getCorrectRB());
            container.validate();
            choiceScrollPane.setViewportView(multiChoicePanel);
        }
       
       public void removeRow(Component comp){
           bgroup.remove(((MultipleChoiceConfigPanel)comp).getCorrectRB());
           container.remove(comp);
           container.validate();
           choiceScrollPane.setViewportView(multiChoicePanel);
       }
       
       public void populateOptions(AutoQuestionChoice question){
           Component[] comps = container.getComponents();
           for(Component comp : comps){
               if(comp instanceof MultipleChoiceConfigPanel){
                   question.addAnswer(((MultipleChoiceConfigPanel)comp).getAnswer(), ((MultipleChoiceConfigPanel)comp).getFeedback(), ((MultipleChoiceConfigPanel)comp).isCorrect());
               }
           }
       }
       
       public void createFromQuestion(AutoQuestionChoice question){
           List<String> answers = question.getAnswers();
           List<String> feedbacks = question.getFeedbacks();
           int correct = question.getCorrectIndex();
           firstRowPanel.setAnswer(answers.get(0));
           firstRowPanel.setFeedback(feedbacks.get(0));
           if(correct == 0){
               firstRowPanel.setCorrect(true);
           }
           for(int i = 1;i < answers.size();i++){
               if(panels.size() <= i){
                   addRow();
               }
                panels.get(i).setAnswer(answers.get(i));
                   panels.get(i).setFeedback(feedbacks.get(i));
                    if(correct == i){
                        panels.get(i).setCorrect(true);
                    }
               
           }
       }
       
       public void setDirty(){
           editor.setPanelDirty(QuestionComponentProperties.class, true);
       }
       
       /**
        * Check if user input for this panel is valid. There must be selected a correct option, and each answer 
        * field must have a text.
        * 
        * @return <code>true</code> if input is valid, <code>false</code> otherwise
        */
       boolean validateInput(){
           boolean valid = true;
           if(bgroup.getSelection() == null){
               valid = false;
           }
           Component[] comps = container.getComponents();
           for(Component comp : comps){
               if(comp instanceof MultipleChoiceConfigPanel){
                   String answer = ((MultipleChoiceConfigPanel)comp).getAnswer();
                   if(answer == null || answer.isEmpty()){
                       valid = false;
                       break;
                   }
               }
           }
           return valid;
       }
    }
}
