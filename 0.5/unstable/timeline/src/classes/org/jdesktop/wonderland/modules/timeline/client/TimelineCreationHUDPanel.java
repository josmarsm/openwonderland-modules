/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderUtils;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineQueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * A panel for creating a new Timeline
 * @author nsimpson
 */
public class TimelineCreationHUDPanel extends javax.swing.JPanel {

    private static final Logger logger =
            Logger.getLogger(TimelineCreationHUDPanel.class.getName());
    private final TimelineCell cell;
    private HUD mainHUD;
    private TimelineAddCollectionPanel addCollectionPanel;
    private HUDComponent addCollectionHUD;
    private TimelineAddProviderHUDPanel addProviderPanel;
    private HUDComponent addProviderHUD;
    private PropertyChangeSupport listeners;
    private List<TimelineQueryBuilder> builders;
    private SimpleDateFormat dateFormat = new SimpleDateFormat();

    public TimelineCreationHUDPanel() {
        builders = new LinkedList<TimelineQueryBuilder>();
        initComponents();
        cell = null;
    }

    public TimelineCreationHUDPanel(TimelineCell cell) {
        builders = new LinkedList<TimelineQueryBuilder>();
        initComponents();

        this.cell = cell;
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public String getDescription() {
        return descriptionTextArea.getText();
    }

    public Date getStartDate() {
        String dateText = startDateTextField.getText();
        Date date;

        try {
            date = dateFormat.parse(dateText);
        } catch (ParseException e) {
            logger.warning("failed to parse date: " + dateText + ": " + e);
            date = new Date();
        }
        return date;
    }

    public Date getEndDate() {
        String dateText = endDateTextField.getText();
        Date date;

        try {
            date = dateFormat.parse(dateText);
        } catch (ParseException e) {
            logger.warning("failed to parse date: " + dateText + ": " + e);
            date = new Date();
        }
        return date;
    }

    public int getScale() {
        return (Integer) scaleSpinner.getValue();
    }

    public String getUnits() {
        return (String) granularitySpinner.getValue();
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    private void addProviders() {
        String[] providers = addProviderPanel.getProviders();
        for (int i = 0; i < providers.length; i++) {
            String provider = providers[i];
            logger.info("--- adding provider: " + i + ": " + provider);

            TimelineQueryBuilder builder = TimelineProviderUtils.createBuilder(provider, cell.getClientConfiguration());
            if (builder != null) {
                logger.info("--- got a builder: " + builder);
                builders.add(builder);

                TimelineProviderPanel panel = new TimelineProviderPanel();
                panel.setProviderName(builder.getDisplayName());

                // add the search combo box
                JComboBox combo = builder.getConfigurationComboBox();
                if (combo != null) {
                    combo.setPreferredSize(new Dimension(40, (int) combo.getPreferredSize().getHeight()));
                    panel.add(combo);
                }

                // add the query configuration button and panel
                final JPanel configPanel = builder.getConfigurationPanel();
                if (configPanel != null) {
                    JButton configButton = new JButton("Configure...");
                    configButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            final HUDComponent configureQueryHUD = mainHUD.createComponent(configPanel);
                            configureQueryHUD.setPreferredLocation(Layout.EAST);
                            configureQueryHUD.setName("Configure Query");
                            configureQueryHUD.addEventListener(new HUDEventListener() {

                                public void HUDObjectChanged(HUDEvent event) {
                                    switch (event.getEventType()) {
                                        case CLOSED:
                                            mainHUD.removeComponent(configureQueryHUD);
                                            break;
                                    }
                                }
                            });
                            mainHUD.addComponent(configureQueryHUD);
                            configureQueryHUD.setVisible(true);
                        }
                    });
                    panel.add(configButton);
                    // TODO: create HUD panel for configuration panel
                    // and display it when the button is pressed
                }
                addProvider(panel);
            }
        }
    }

    /**
     * Add a provider panel
     * @param panel the timeline provider panel to add
     */
    public void addProvider(TimelineProviderPanel panel) {
        int providers = getProviderCount();
        providersPanel.setPreferredSize(new Dimension(
                providersPanel.getWidth(),
                (int) ((providers + 1) * panel.getPreferredSize().getHeight())));
        providersPanel.add(panel);
        providersPanel.validate();

        setPreferredSize(new Dimension(
                (int) (this.getPreferredSize().getWidth()),
                (int) (this.getPreferredSize().getHeight() + panel.getPreferredSize().getHeight())));
        validate();
    }

    /**
     * Remove a provider panel
     * @param panel the timeline provider panel to remove
     */
    public void removeProvider(TimelineProviderPanel panel) {
        int height = panel.getHeight();
        providersPanel.remove(panel);
        providersPanel.setSize(providersPanel.getWidth(), providersPanel.getHeight() - height);
        validate();
    }

    /**
     * Get the number of provider panels
     * @return the number of panels
     */
    public int getProviderCount() {
        return providersPanel.getComponentCount();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        createLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        startDateLabel = new javax.swing.JLabel();
        startDateTextField = new javax.swing.JTextField();
        endDateLabel = new javax.swing.JLabel();
        endDateTextField = new javax.swing.JTextField();
        scaleLabel = new javax.swing.JLabel();
        revolutionLabel = new javax.swing.JLabel();
        providersLabel = new javax.swing.JLabel();
        addProviderButton = new javax.swing.JButton();
        createButton = new javax.swing.JButton();
        providersPanel = new javax.swing.JPanel();
        addKeywordButton = new javax.swing.JButton();
        granularitySpinner = new javax.swing.JSpinner();
        scaleSpinner = new javax.swing.JSpinner();
        cancelButton = new javax.swing.JButton();

        createLabel.setFont(createLabel.getFont().deriveFont(createLabel.getFont().getStyle() | java.awt.Font.BOLD));
        createLabel.setText("Create Timeline");

        titleLabel.setText("Title:");

        titleTextField.setText("New Timeline");

        descriptionLabel.setText("Description:");

        descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        startDateLabel.setText("Start Date:");

        startDateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        startDateTextField.setText("12/31/09");

        endDateLabel.setText("End Date:");

        endDateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        endDateTextField.setText("9/11/10");

        scaleLabel.setText("Scale:");

        revolutionLabel.setText("make 1 revolution equal");

        providersLabel.setText("Providers:");

        addProviderButton.setText("Add Provider...");
        addProviderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProviderButtonActionPerformed(evt);
            }
        });

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        addKeywordButton.setText("Add Keyword Collection...");
        addKeywordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeywordButtonActionPerformed(evt);
            }
        });

        granularitySpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"Hours", "Days", "Weeks", "Months", "Years"}));

        scaleSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), null, null, Integer.valueOf(1)));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(addProviderButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addKeywordButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(createButton))
                    .add(createLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, providersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(scaleLabel)
                                    .add(titleLabel)
                                    .add(descriptionLabel)
                                    .add(startDateLabel)
                                    .add(providersLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(titleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(revolutionLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(scaleSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(12, 12, 12)
                                        .add(granularitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, descriptionScrollPane, 0, 0, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                            .add(startDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(endDateLabel)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(endDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                        .add(10, 10, 10)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {endDateTextField, startDateTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(createLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(titleLabel)
                    .add(titleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startDateLabel)
                    .add(startDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(endDateLabel)
                    .add(endDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(scaleSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(revolutionLabel)
                    .add(scaleLabel)
                    .add(granularitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providersLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addKeywordButton)
                    .add(addProviderButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createButton)
                    .add(cancelButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addProviderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProviderButtonActionPerformed
        // TODO: display a provider chooser dialog
        // for now, just add another generic provider panel
        if (mainHUD == null) {
            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        }
        if (addProviderHUD == null) {
            addProviderPanel = new TimelineAddProviderHUDPanel();

            addProviderPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
                    logger.info("--- property changed: " + pe);
                    if (pe.getPropertyName().equals("add")) {
                        addProviders();
                        addProviderHUD.setVisible(false);
                    } else if (pe.getPropertyName().equals("cancel")) {
                        addProviderHUD.setVisible(false);
                    }
                }
            });
            addProviderHUD = mainHUD.createComponent(addProviderPanel);
            addProviderHUD.setPreferredLocation(Layout.EAST);
            addProviderHUD.setName("Add Provider");

            mainHUD.addComponent(addProviderHUD);
        }
        addProviderHUD.setVisible(true);
        //addProvider(new TimelineProviderPanel());
    }//GEN-LAST:event_addProviderButtonActionPerformed

    private void addKeywordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeywordButtonActionPerformed
        if (mainHUD == null) {
            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        }
        if (addCollectionHUD == null) {
            addCollectionPanel = new TimelineAddCollectionPanel();
            addCollectionPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
                    if (pe.getPropertyName().equals("done") || pe.getPropertyName().equals("cancel")) {
                        addCollectionHUD.setVisible(false);
                    }
                }
            });
            addCollectionHUD = mainHUD.createComponent(addCollectionPanel);
            addCollectionHUD.setPreferredLocation(Layout.EAST);
            addCollectionHUD.setName("Add Keyword Collection");
            addCollectionHUD.addEventListener(new HUDEventListener() {

                public void HUDObjectChanged(HUDEvent event) {
                    switch (event.getEventType()) {
                        case CLOSED:
                            // TODO: add keyword collection
                            break;
                    }
                }
            });
            mainHUD.addComponent(addCollectionHUD);
        }
        addCollectionHUD.setVisible(true);
    }//GEN-LAST:event_addKeywordButtonActionPerformed

    private void buildQuery() {
        ListIterator<TimelineQueryBuilder> iter = builders.listIterator();
        while (iter.hasNext()) {
            TimelineQueryBuilder builder = iter.next();
            TimelineQuery query = builder.build();
        }
    }

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        listeners.firePropertyChange("create", new String(""), null);
        buildQuery();
    }//GEN-LAST:event_createButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        listeners.firePropertyChange("cancel", new String(""), null);
    }//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addKeywordButton;
    private javax.swing.JButton addProviderButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton createButton;
    private javax.swing.JLabel createLabel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JTextField endDateTextField;
    private javax.swing.JSpinner granularitySpinner;
    private javax.swing.JLabel providersLabel;
    private javax.swing.JPanel providersPanel;
    private javax.swing.JLabel revolutionLabel;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JSpinner scaleSpinner;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JTextField startDateTextField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables
}
