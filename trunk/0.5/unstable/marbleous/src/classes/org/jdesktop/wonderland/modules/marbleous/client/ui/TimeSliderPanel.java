/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimeSliderPanel.java
 *
 * Created on Aug 12, 2009, 10:07:15 AM
 */

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
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

    private static final float SAMPLE_ENTITY_Y_OFFSET = 0.3f;

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
                    jLabel3.setText(Float.toString(endTime));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create time sliderpanel");
        }
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
        System.err.println("t = " + selectedTime);
        SampleInfo sampleInfo = trace.getSampleInfo(selectedTime);
        setCurrentSampleInfo(sampleInfo);
        System.err.println("********** currentPosition = " + currentPosition);

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

        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setValue(0);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel1.setText("Time (ms)");

        jLabel2.setText("0");

        jLabel3.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(232, 232, 232)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 216, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(32, 32, 32))
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        if (setLocal == false) {
            int value = jSlider1.getValue();
            float pct = (float) value / (float) (jSlider1.getMaximum() - jSlider1.getMinimum());
            //System.err.println("trace.getEndTime() = " + trace.getEndTime());
            float t = pct * trace.getEndTime();

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
                    currentSampleEntity.setVisible(! currentSampleEntity.getVisible());
                }
            }
        }
    }


    private SampleDisplayEntity createSampleEntity (SampleInfo sampleInfo) {
        SampleDisplayEntity sampleEntity = new SampleDisplayEntity(null/*TODO: should be cell entity*/,
                                                                 sampleInfo, 0.006f);
        Vector3f position = sampleInfo.getPosition();
        float y = position.y + marbleRadius + SAMPLE_ENTITY_Y_OFFSET;
        sampleEntity.setLocalTranslation(new Vector3f(position.x, y, position.z));

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
}
