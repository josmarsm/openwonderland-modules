/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

/*
 * ScavengerHuntStudentViewPanel.java
 *
 * Created on Mar 15, 2012, 4:02:26 PM
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultMetadata;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.components.ScavengerHuntComponent;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntResult;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntSheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataList;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntStudentViewPanel extends JPanel implements SharedMapListenerCli {
    
    private final ISocialManager manager;
    private final Sheet sheet;
    private List<ScavengerHuntItem> itemsList;
    // item map per student
    private SharedMapCli map;
    // hunt-wide map
    private SharedMapCli globalMap;
    private String resultId;
    private String username;
    private JPanel itemPanel;
    private Timer timer;
    private String serverAndPort;
    
    private final int prefWidth;
    private JLabel directionsLabel;
    private TimerPanel timerPanel;
    private boolean initialized;
    private Timer clockTimer;
    private ClockTimerHandler clockTimerHandler;

    /** Creates new form ScavengerHuntStudentViewPanel */
    public ScavengerHuntStudentViewPanel(ISocialManager manager, Sheet sheet, final String username) {
        initialized = false;
        this.manager = manager;
        this.sheet = sheet;
        this.username  = username;
        this.serverAndPort = manager.getSession().getServerNameAndPort();
        itemsList = new ArrayList<ScavengerHuntItem>();
        ScavengerHuntSheet scs = (ScavengerHuntSheet) sheet.getDetails();
        directionsLabel = new JLabel("<html><body style='width: 150px'>" + scs.getInstructions().replaceAll("\\r?\\n", "<br>") + "</html>");
        directionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        directionsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        timerPanel = new TimerPanel(username, sheet);
        // panel for directions and  timer
        JPanel upper = new JPanel(new BorderLayout());
        upper.add(directionsLabel, BorderLayout.NORTH);
        upper.add(timerPanel, BorderLayout.SOUTH);
//         directionsLabel.setText();
        initComponents();
        add(upper, BorderLayout.NORTH);
        prefWidth = (int)directionsLabel.getPreferredSize().getWidth();
        itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        map = findSheetdMap();
        if(map == null){
            // if no map is set, start a timer that will poll for map creation
            timer = new Timer(ScavengerHuntConstants.MAP_POLL_INTERVAL, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    map = findSheetdMap();   
                    if(map != null){
                        sheetMapUpdated();
                        validate();
                        timer.stop();
                    }
                }
            });
            timer.setRepeats(true);
            timer.start();
        } else {
            sheetMapUpdated();
        }


//        itemScroll.setViewportView(itemPanel);
        clockTimerHandler = new ClockTimerHandler();
        clockTimer = new Timer(ClockTimerHandler.SEC_MILIS, clockTimerHandler);
        clockTimer.setRepeats(true);
        timerPanel.setClockTimer(clockTimer);
        timerPanel.setStudentViewPanel(this);
    }
    
    public void setResultId(String id){
        this.resultId = id;
    }

    /**
     * Display items list for user.
     */
    public void showItemsList(){
         itemScroll.setViewportView(itemPanel);
         validate();
    }
    
    /**
     * Ending the hunt will mark all un-found items as given up.
     */
    public void endHunt(){
        timerPanel.endHunt();
        for(Component comp : itemPanel.getComponents()){
            if(comp instanceof StudentViewItemPanel){
                ((StudentViewItemPanel)comp).giveUpItem();
            }
        }
    }
    
    private SharedMapCli findSheetdMap() {
        return ScavengerHuntComponent.getSharedMap(sheet.getId(), username);
    }
    
    /**
     * Invoked when shared map is found. This method will construct UI based on state found 
     * in shared state component map.
     */
    private void sheetMapUpdated(){
            final String sheetId = sheet.getId();
            // check if reordering collection exists
            Collection<String> keys = null;
            if(map.containsKey(ScavengerHuntConstants.ORDER_LIST_NAME)){
                keys = ((SharedDataList)map.get(ScavengerHuntConstants.ORDER_LIST_NAME)).getList();
            } else {
                keys = map.keySet();
            }
            for (String key : keys) {
                // skip item order list key
                if (key != null && 
                    !key.equals(ScavengerHuntConstants.ORDER_LIST_NAME) &&
                    !key.equals(ScavengerHuntConstants.FIND_METHOD_KEY_NAME) &&
                    map.get(key) instanceof SharedDataItem) 
                {
                    StudentViewItemPanel panel = new StudentViewItemPanel(((SharedDataItem) map.get(key)).getItem(), sheetId, username, serverAndPort);
                    itemPanel.add(panel);
                    itemsList.add(((SharedDataItem) map.get(key)).getItem());
                }

            }
            ScavengerHuntResult result = new ScavengerHuntResult();
            result.setItems(itemsList);
            timerPanel.setResult(result);
            map.addSharedMapListener(this);
            globalMap = ScavengerHuntComponent.getSharedMap(sheet.getId(), null);
            // add listener for re-ordering items
            globalMap.addSharedMapListener(new SharedMapListenerCli() {

                public void propertyChanged(SharedMapEventCli smec) {
                    SharedData oldVal = smec.getOldValue();
                    SharedData newVal = smec.getNewValue();
                    if (newVal instanceof SharedDataList) {
                        // reorder items
                        itemPanel.removeAll();
                        List<String> keys = ((SharedDataList) smec.getNewValue()).getList();
                        for (String key : keys) {
                            StudentViewItemPanel panel = new StudentViewItemPanel(((SharedDataItem) map.get(key)).getItem(), sheetId, username, serverAndPort);
                            itemPanel.add(panel);
                        }
                        validate();
                    } else if(newVal instanceof SharedDataItem){
                        // if new item is added to global map, add it to user map
                        SharedDataItem sdi = (SharedDataItem)newVal;
                        if(!map.containsKey(sdi.getItem().getCellId())){
                            map.put(sdi.getItem().getCellId(), sdi);
                            // add new item panel
                            StudentViewItemPanel panel = new StudentViewItemPanel(sdi.getItem(), sheetId, username, serverAndPort);
                            itemPanel.add(panel);
                            validate();
                        } else {
                            // update existing item
                            ScavengerHuntItem item = ((SharedDataItem)map.get(sdi.getItem().getCellId())).getItem();
                            item.update(sdi.getItem());
                            for (Component comp : itemPanel.getComponents()) {
                                if (comp instanceof StudentViewItemPanel && ((StudentViewItemPanel) comp).getItemCellId().equals(sdi.getItem().getCellId())) {
                                    ((StudentViewItemPanel) comp).setItem(item);
                                    break;
                                }
                            }
                        }
                    } else if(oldVal != null && (oldVal instanceof SharedDataItem) && newVal == null){
                        String id = ((SharedDataItem)oldVal).getItem().getCellId();
                        // remove item from user map
                        map.remove(id);
                        // item is removed, so remove it from student view
                        for(Component comp : itemPanel.getComponents()){
                            if(comp instanceof StudentViewItemPanel && ((StudentViewItemPanel)comp).getItemCellId().equals(id)){
                                itemPanel.remove(comp);
                                validate();
                                break;
                            }
                        }
                    }
                } 
            });
            initialized = true;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        itemScroll = new javax.swing.JScrollPane();

        setMaximumSize(new java.awt.Dimension(200, 150));
        setLayout(new java.awt.BorderLayout());

        itemScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(itemScroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane itemScroll;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener will update result with changed data from shared map.
     * @param smec 
     */
    public void propertyChanged(SharedMapEventCli smec) {
        if (initialized) {
            boolean completed = true;
            List<ScavengerHuntItem> updated = new ArrayList<ScavengerHuntItem>();
            for (String key : map.keySet()) {
                // skip item order list key
                if (!key.equals(ScavengerHuntConstants.ORDER_LIST_NAME)
                        && !key.equals(ScavengerHuntConstants.FIND_METHOD_KEY_NAME)
                        && map.get(key) instanceof SharedDataItem) {
                    updated.add(((SharedDataItem) map.get(key)).getItem());
                    completed &= (((SharedDataItem) map.get(key)).getItem().isFound() || ((SharedDataItem) map.get(key)).getItem().isUserGaveUp());
                }
            }
            try {
                if (resultId != null) {
                    ScavengerHuntResult result = (ScavengerHuntResult) manager.getResult(resultId).getDetails();
                    result.setItems(updated);
                    if (completed) {
                        result.setDuration(timerPanel.getTime());
                        endHunt();
                    }
                    manager.updateResult(resultId, result);
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    /**
     * Handler for component event and timer. It will activate timer when student view is displayed, and update
     * current time every second.
     */
    private class ClockTimerHandler implements ActionListener, Runnable{
        
        /** Number of miliseconds in  a second. */
        private static final int SEC_MILIS = 1000;
        
        /** Timer elapsed time ( in miliseconds). */
        private long elapsedTime = 0;

        /**
         * Update displayed time on every timer update. Update is performed on EDT.
         * @param e  event
         */
        public void actionPerformed(ActionEvent e) {
            elapsedTime += SEC_MILIS;
            SwingUtilities.invokeLater(this);
        }

        /**
         * Executes timer update on separate thread.
         */
        public void run() {
            timerPanel.updateTime(elapsedTime);
        }
        
        
        
    }
}
