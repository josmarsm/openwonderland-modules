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
package org.jdesktop.wonderland.modules.timeline.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.jdesktop.wonderland.modules.timeline.client.provider.KeywordConsumer;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderUtils;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineQueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration.TimelineUnits;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDateRange;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * A panel for creating a new Timeline
 * @author nsimpson
 */
public class TimelineCreationHUDPanel extends javax.swing.JPanel {

    private static final Logger logger =
            Logger.getLogger(TimelineCreationHUDPanel.class.getName());
    private HUD mainHUD;
    private TimelineAddCollectionPanel addCollectionPanel;
    private HUDComponent addCollectionHUD;
    private TimelineAddProviderHUDPanel addProviderPanel;
    private HUDComponent addProviderHUD;
    private PropertyChangeSupport listeners;
    private List<TimelineQueryBuilder> builders;
    private List<TimelineQuery> queries;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    private TimelineClientConfiguration config;

    public enum Mode {

        CREATE, UPDATE
    };
    private Mode mode = Mode.CREATE;

    public TimelineCreationHUDPanel() {
        this(null);
    }

    public TimelineCreationHUDPanel(TimelineClientConfiguration conf) {
        builders = new LinkedList<TimelineQueryBuilder>();
        queries = new LinkedList<TimelineQuery>();

        if (conf != null) {
            this.config = conf;
        } else {
            this.config = new TimelineClientConfiguration();
        }

	
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	initComponents();
        	setDefaults();
	    }
	});
    }

    private void setDefaults() {
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.CREATE) {
            createButton.setText("Create");
        } else if (mode == Mode.UPDATE) {
            createButton.setText("Update");
        }
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

    public float getScale() {
        return (Float) scaleSpinner.getValue();
    }

    public TimelineConfiguration.TimelineUnits getUnits() {
        return TimelineUnits.valueOf((String) granularitySpinner.getValue());
    }

    public void setEndDate(Date d) {
        this.endDateTextField.setText(dateFormat.format(d));
    }

    public void setStartDate(Date d) {
        this.startDateTextField.setText(dateFormat.format(d));
    }

    public Float getInnerRadius() {
        return (Float) innerRadiusSpinner.getValue();
    }

    public Float getSpiralWidth() {
        return (Float) widthSpinner.getValue();
    }

    public Float getPitch() {
        return (Float) pitchSpinner.getValue();
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

            TimelineQueryBuilder builder = TimelineProviderUtils.createBuilder(provider, config);
            if (builder != null) {
                logger.info("--- got a builder: " + builder);
                builders.add(builder);

                TimelineProviderPanel panel = new TimelineProviderPanel();
                panel.setProviderName(builder.getDisplayName());

                // add the search combo box
                JComboBox combo = builder.getConfigurationComboBox();
                if (combo != null) {
                    panel.add(combo, BorderLayout.CENTER);
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

                            configPanel.addPropertyChangeListener(new PropertyChangeListener() {

                                public void propertyChange(PropertyChangeEvent pe) {
                                    logger.info("--- property changed: " + pe);
                                    configureQueryHUD.setVisible(false);
                                }
                            });
                            
                            mainHUD.addComponent(configureQueryHUD);
                            configureQueryHUD.setVisible(true);
                        }
                    });
                    panel.add(configButton, BorderLayout.EAST);
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

        providersScrollPane = new javax.swing.JScrollPane();
        providersPanel = new javax.swing.JPanel();
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
        addKeywordButton = new javax.swing.JButton();
        granularitySpinner = new javax.swing.JSpinner();
        scaleSpinner = new javax.swing.JSpinner();
        cancelButton = new javax.swing.JButton();
        innerRadiusLabel = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        widthSpinner = new javax.swing.JSpinner();
        pitchLabel = new javax.swing.JLabel();
        pitchSpinner = new javax.swing.JSpinner();
        segmentsLabel = new javax.swing.JLabel();
        innerRadiusSpinner = new javax.swing.JSpinner();
        numSegmentsLabel = new javax.swing.JLabel();
        numSegmentsLabel1 = new javax.swing.JLabel();
        numSegmentsLabel2 = new javax.swing.JLabel();
        numSegmentsLabel3 = new javax.swing.JLabel();
        numSegmentsLabel4 = new javax.swing.JLabel();

        providersPanel.setBackground(new java.awt.Color(255, 255, 204));
        providersPanel.setLayout(new java.awt.GridLayout(0, 1));
        providersScrollPane.setViewportView(providersPanel);

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
        startDateTextField.setText(dateFormat.format(config.getDateRange().getMinimum()));

        endDateLabel.setText("End Date:");

        endDateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        endDateTextField.setText(dateFormat.format(config.getDateRange().getMaximum()));

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

        granularitySpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"HOURS", "DAYS", "WEEKS", "MONTHS", "YEARS"}));

        scaleSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), null, null, Float.valueOf(0.25f)));
        scaleSpinner.setValue(config.getUnitsPerRev());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        innerRadiusLabel.setText("Inner Radius:");

        widthLabel.setText("Width:");

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(5.0f), null, null, Float.valueOf(0.5f)));
        widthSpinner.setValue(config.getWidth());

        pitchLabel.setText("Pitch:");

        pitchSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(3.0f), null, null, Float.valueOf(0.5f)));
        pitchSpinner.setValue(config.getPitch());

        segmentsLabel.setText("Your settings result in ");

        innerRadiusSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(3.0f), null, null, Float.valueOf(0.5f)));
        innerRadiusSpinner.setValue(config.getInnerRadius());

        numSegmentsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        numSegmentsLabel.setText("X");

        numSegmentsLabel1.setText("segments of");

        numSegmentsLabel2.setText("Y(units)");

        numSegmentsLabel3.setText("and");

        numSegmentsLabel4.setText("Z deg");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(providersScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 485, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(scaleLabel)
                            .add(titleLabel)
                            .add(descriptionLabel)
                            .add(startDateLabel)
                            .add(innerRadiusLabel)
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
                                    .add(endDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createSequentialGroup()
                                .add(innerRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(widthLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pitchLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pitchSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(cancelButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(createButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(segmentsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numSegmentsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numSegmentsLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numSegmentsLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numSegmentsLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numSegmentsLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(addProviderButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addKeywordButton)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {endDateTextField, startDateTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(innerRadiusLabel)
                    .add(innerRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(widthLabel)
                    .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pitchLabel)
                    .add(pitchSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(segmentsLabel)
                    .add(numSegmentsLabel)
                    .add(numSegmentsLabel1)
                    .add(numSegmentsLabel2)
                    .add(numSegmentsLabel3)
                    .add(numSegmentsLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providersLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(providersScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addKeywordButton)
                    .add(addProviderButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(createButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addProviderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProviderButtonActionPerformed
        if (mainHUD == null) {
            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        }
        if (addProviderHUD == null) {
            addProviderPanel = new TimelineAddProviderHUDPanel();

            addProviderPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
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

        TimelineDateRange range = new TimelineDateRange(getStartDate(),
                                                        getEndDate(), 
                                                        getUnits().getCalendarUnit());
        addCollectionPanel.setDateRange(range);
        addCollectionHUD.setVisible(true);
    }//GEN-LAST:event_addKeywordButtonActionPerformed

    public void buildQuery() {
        queries.clear();

        logger.info("--- Building query for " + builders.size() + " builders");

        ListIterator<TimelineQueryBuilder> iter = builders.listIterator();
        while (iter.hasNext()) {
            TimelineQueryBuilder builder = iter.next();

            // make sure the configuration is up to date for the builder
            builder.setTimelineConfiguration(config);

            // see about setting up keywords
            if (addCollectionPanel != null && builder instanceof KeywordConsumer) {
                ((KeywordConsumer) builder).setKeywords(addCollectionPanel.getQueries());
            }

            TimelineQuery query = builder.build();
            queries.add(query);
        }
    }

    public List<TimelineQuery> getQueries() {
        return queries;
    }

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        logger.info("rebuild client config");
        logger.info("PREVIOUS client config's pitch:" + config.getPitch());
        config.setDateRange(new TimelineDate(getStartDate(), getEndDate()));
        config.setUnitsPerRev(getScale());
        config.setUnits(getUnits());
        config.setInnerRadius(getInnerRadius());
        float width = getSpiralWidth();
        config.setOuterRadius(width + getInnerRadius());
        config.setWidth(width);
        config.setPitch(getPitch());
        
        buildQuery();

        if (mode == Mode.CREATE) {
            listeners.firePropertyChange("create", new String(""), null);
        } else if (mode == Mode.UPDATE) {
            listeners.firePropertyChange("update", new String(""), null);
        }
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
    private javax.swing.JLabel innerRadiusLabel;
    private javax.swing.JSpinner innerRadiusSpinner;
    private javax.swing.JLabel numSegmentsLabel;
    private javax.swing.JLabel numSegmentsLabel1;
    private javax.swing.JLabel numSegmentsLabel2;
    private javax.swing.JLabel numSegmentsLabel3;
    private javax.swing.JLabel numSegmentsLabel4;
    private javax.swing.JLabel pitchLabel;
    private javax.swing.JSpinner pitchSpinner;
    private javax.swing.JLabel providersLabel;
    private javax.swing.JPanel providersPanel;
    private javax.swing.JScrollPane providersScrollPane;
    private javax.swing.JLabel revolutionLabel;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JSpinner scaleSpinner;
    private javax.swing.JLabel segmentsLabel;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JTextField startDateTextField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
}
