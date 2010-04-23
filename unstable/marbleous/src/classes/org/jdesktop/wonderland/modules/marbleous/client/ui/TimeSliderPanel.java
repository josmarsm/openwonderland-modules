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

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.marbleous.common.trace.SampleInfo;
import org.jdesktop.wonderland.modules.marbleous.common.trace.SimTrace;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TrackRenderer;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TrackRenderer.MarbleMouseEventListener;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SelectedSampleMessage;

/**
 *
 * @author dj
 */
public class TimeSliderPanel extends javax.swing.JPanel {


    private TrackCell cell;

    private MarbleMouseEventListener marbleMouseListener;
    private SampleInfo currentSampleInfo;
    private Vector3f currentPosition;
    private SampleDisplayEntity currentSampleEntity;
    private SimTrace trace;

    /*
     * This boolean indicates whether the value of the slider is being
     * set programmatically.
     */
    private boolean setLocal = false;

    /** Creates new form TimeSliderPanel */
    public TimeSliderPanel(TrackCell cell) {
        this.cell = cell;

        initComponents();

        marbleMouseListener = new MarbleMouseListener();
        cell.addMarbleMouseListener(marbleMouseListener);
    }


    public void setSimTrace (SimTrace trace) {
        this.trace = trace;

        final float endTime = trace.getEndTime();
        try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    jLabel3.setText(format(endTime));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create time sliderpanel");
        }

        // Initialize the current sample info to t0
        SampleInfo sampleInfo = trace.getSampleInfo(0);
        setCurrentSampleInfo(sampleInfo);
    }

    public void init () {
        // Initialize the current sample info to t0
        SampleInfo sampleInfo = trace.getSampleInfo(0);
        setCurrentSampleInfo(sampleInfo);
        updateMarbleWithTime(0);
    }

    /**
     * Sets whether the changes being made to the JSlider is doing so
     * programmatically, rather than via a mouse event. This is used to
     * make sure that requests to the other clients are not made at the
     * wrong time.
     *
     * @param isLocal True to indicate the JSlider values are being set
     * programmatically.
     */
    private void setLocalChanges(boolean isLocal) {
        setLocal = isLocal;
    }

    /**
     * Sets the selected time, updates the GUI to indicate as such
     *
     * @param selectedTime The selected time (in seconds)
     */
    public void setSelectedTime(final float selectedTime) {
        // Set the value of the slider, make sure this is done on the AWT
        // Event Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                float pct = selectedTime / trace.getEndTime();
                float dT = (float) (jSlider1.getMaximum() - jSlider1.getMinimum());
                int value = (int) (pct * dT);

                // Update the value of the slider, but indicate that we are
                // doing this programmatically.
                setLocalChanges(true);
                try {
                    jSlider1.setValue(value);
                } finally {
                    setLocalChanges(false);
                }
            }
        });

        // Update the marble entity based upon the selected time
        updateMarbleWithTime(selectedTime);
    }

    /**
     * Updates the marble based upon the selected time.
     */
    private void updateMarbleWithTime(float selectedTime) {
        //System.err.println("value = " + value);
        //System.err.println("pct = " + pct);
        //System.err.println("t = " + selectedTime);
        SampleInfo sampleInfo = trace.getSampleInfo(selectedTime);
        setCurrentSampleInfo(sampleInfo);
        //System.err.println("********** currentPosition = " + currentPosition);

        // Assumes that the marble is still attached to the cell
        Entity marbleEntity = cell.getMarbleEntity();
        RenderComponent rc = marbleEntity.getComponent(RenderComponent.class);
        final Node marbleNode = rc.getSceneRoot();

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                marbleNode.setLocalTranslation(currentPosition);
                ClientContextJME.getWorldManager().addToUpdateList(marbleNode);
            }
        }, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setValue(0);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel1.setText("Time (sec)");

        jLabel2.setText("0");

        jLabel3.setText("0");

        jLabel4.setText("Current Time: ");

        jLabel5.setText("0");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .add(232, 232, 232)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 234, Short.MAX_VALUE)
                .add(jLabel3)
                .add(32, 32, 32))
            .add(layout.createSequentialGroup()
                .add(32, 32, 32)
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 489, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(63, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(209, 209, 209)
                .add(jLabel4)
                .add(18, 18, 18)
                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(225, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jLabel4))
                .add(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setSliderValue (int value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jSlider1.setValue(0);
            }
        });
        
    }

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        if (setLocal == false && trace != null) {
            int value = jSlider1.getValue();
            float pct = (float) value / (float) (jSlider1.getMaximum() - jSlider1.getMinimum());
            //System.err.println("trace.getEndTime() = " + trace.getEndTime());
            float t = pct * trace.getEndTime();
            jLabel5.setText(format(t));

            // Tell the other clients that the slider value has changed
            cell.sendCellMessage(new SelectedSampleMessage(t));

            // Update the marble with the selected time
            updateMarbleWithTime(t);
        }
    }//GEN-LAST:event_jSlider1StateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSlider jSlider1;
    // End of variables declaration//GEN-END:variables

    private void setCurrentSampleInfo (SampleInfo sampleInfo) {
        if (sampleInfo == currentSampleInfo) {
            return;
        }

        currentSampleInfo = sampleInfo;
        //System.err.println("currentSampleInfo = " + currentSampleInfo);

        currentPosition = currentSampleInfo.getPosition();
        //System.err.println("current position = " + currentPosition);

        updateCurrentSampleEntity();
    }

    private final HashMap<String,SampleDisplayEntity> sampleEntities = new HashMap<String,SampleDisplayEntity>();

    // TODO: eventually get from TrackRenderer
    private float marbleRadius = 0.25f;

    private class MarbleMouseListener implements TrackRenderer.MarbleMouseEventListener {
        public void commitEvent (Entity marbleEntity, Event event) {
            MouseEvent3D me3d = (MouseEvent3D) event;
            MouseEvent me = (MouseEvent) me3d.getAwtEvent();
            if(me3d.getID() == MouseEvent.MOUSE_CLICKED){
                if(me.getButton() == MouseEvent.BUTTON1 &&
                   me.getModifiersEx() == 0) {
                    if (currentSampleEntity != null) {
                        currentSampleEntity.setVisible(! currentSampleEntity.getVisible());
                    }
                }
            }
        }
    }


    private SampleDisplayEntity createSampleEntity (SampleInfo sampleInfo) {
        Vector3f position = sampleInfo.getPosition();
        SampleDisplayEntity sampleEntity = 
            new SampleDisplayEntity(null/*TODO: should be cell entity*/,
                                    sampleInfo, 0.006f, marbleRadius, position);        
        return sampleEntity;
    }    

    private void updateCurrentSampleEntity () {
        String tStr = Float.toString(currentSampleInfo.getTime());
        SampleDisplayEntity sampleEntity = sampleEntities.get(tStr);
        synchronized (sampleEntities) {
            sampleEntity = sampleEntities.get(tStr);
            if (sampleEntity == null) {
                sampleEntity = createSampleEntity(currentSampleInfo);
                sampleEntities.put(tStr, sampleEntity);
            }
        }

        if (currentSampleEntity == sampleEntity) return;

        if (currentSampleEntity != null) {
            currentSampleEntity.setCurrent(false);
            // So it is not displayed automatically when we come back to it
            currentSampleEntity.setVisible(false);
        }

        currentSampleEntity = sampleEntity;
        currentSampleEntity.setCurrent(true);
    }

    public void cleanup () {
        setSliderValue(0);
        // TODO: dispose all entities, not just current
        sampleEntities.clear();
        if (currentSampleEntity != null) {
            currentSampleEntity.dispose();
        }
        currentSampleEntity = null;
        SampleDisplayEntity.disposeAll();
        currentSampleInfo = null;
        trace = null;
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private String format (float f) {
        return decimalFormat.format(f);
    }
}
