/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.topmap.client;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A JPanel to be used as a HUD panel that displays a top map.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TopMapJPanel extends javax.swing.JPanel {

    // The width and height of the map image
    private static final int MAP_HEIGHT = 256;
    private static final int MAP_WIDTH = 256;

    // The buffered image in which to draw the camera scene
    private final BufferedImage bufferedImage;

    // The component that displays the top map
    private final CaptureJComponent mapComponent;

    // The model behind the elevation spinner value
    private final SpinnerNumberModel elevationModel;

    // The initial elevation to use and display in the spinner
    private static final float INITIAL_ELEVATION = 10.0f;

    // The current elevation
    private float elevation = INITIAL_ELEVATION;

    // A set of listeners for changes to the elevation
    private final Set<ElevationListener> listenerSet;

    /** Default constructor */
    public TopMapJPanel() {
        // Initialize the GUI
        listenerSet = new CopyOnWriteArraySet<ElevationListener>();
        initComponents();

        // Put a model on the spinner for elevation, with a min of 0, a max
        // of 10000, and a step size of 1.
        Float value = new Float(elevation);
        Float min = new Float(0.0f);
        Float max = new Float(10000.0f);
        Float step = new Float(1.0f);
        elevationModel = new SpinnerNumberModel(value, min, max, step);
        elevationSpinner.setModel(elevationModel);

        // Create the BufferedImage into which we will draw the camera scene
        bufferedImage = new BufferedImage(MAP_WIDTH, MAP_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        // Create and add a CaptureJPanel to the panel
        mapComponent = new CaptureJComponent(bufferedImage);
        mapComponent.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        topMapPanel.add(mapComponent);

        // Listen for changes to the elevation value and update the camera as
        // a result.
        elevationModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // Set the elevation and fire an event that it has changed
                elevation = (Float) elevationModel.getValue();
                fireElevationListener(elevation);
            }
        });
    }

    /**
     * Returns the CaptureJComponent which renders the map.
     *
     * @return The JComponent that renders the map
     */
    public CaptureJComponent getCaptureJComponent() {
        return mapComponent;
    }

    /**
     * Returns the current elevation set on the panel.
     *
     * @return The current elevation
     */
    public float getElevation() {
        return elevation;
    }

    /**
     * Adds a new listener for elevation changes. If the listener already
     * exists, this method does nothing.
     *
     * @param listener The listener to add
     */
    public void addElevationListener(ElevationListener listener) {
        listenerSet.add(listener);
    }

    /**
     * Removes an existing listener for elevation changes. If the listener
     * does not exist, this method does nothing.
     *
     * @param listener The listener to remove
     */
    public void removeElevationListener(ElevationListener listener) {
        listenerSet.remove(listener);
    }

    /**
     * Notifies all listeners that an elevation change has happened.
     */
    private void fireElevationListener(float elevation) {
        for (ElevationListener listener : listenerSet) {
            listener.elevationChanged(elevation);
        }
    }

    /**
     * A listener interface for changes in the elevation
     */
    public interface ElevationListener {
        /**
         * Indicates that the elevation has changed.
         * @param elevation The new elevation
         */
        public void elevationChanged(float elevation);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topMapPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        elevationLabel = new javax.swing.JLabel();
        elevationSpinner = new javax.swing.JSpinner();
        metersLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        topMapPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(topMapPanel, gridBagConstraints);

        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/topmap/client/resources/Bundle"); // NOI18N
        elevationLabel.setText(bundle.getString("Elevation_Label")); // NOI18N
        controlPanel.add(elevationLabel);
        controlPanel.add(elevationSpinner);

        metersLabel.setText(bundle.getString("Meters_Label")); // NOI18N
        controlPanel.add(metersLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(controlPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JLabel elevationLabel;
    private javax.swing.JSpinner elevationSpinner;
    private javax.swing.JLabel metersLabel;
    private javax.swing.JPanel topMapPanel;
    // End of variables declaration//GEN-END:variables
}
