/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author ryan
 */
    class MessageTimer implements CellMoveListener {

        private long timeSum = 0;
        private long lastReport;
        private int count = 0;
        private long min = Long.MAX_VALUE;
        private long max = 0;
        private static final long REPORT_INTERVAL = 5000; // Report time in ms
        private LinkedList<TimeRecord> messageTimes = new LinkedList();
        private static final Logger logger = Logger.getLogger(MessageTimer.class.getName());
        private String username = "";

        public MessageTimer(String username) {
            lastReport = System.nanoTime();
            this.username = username;
        }

        /**
         * Callback for messages from server
         * @param arg0
         * @param arg1
         */
        public void cellMoved(CellTransform transform, CellMoveSource moveSource) {
            if (messageTimes.size() != 0 && messageTimes.getFirst().getTransform().equals(transform)) {
                TimeRecord rec = messageTimes.removeFirst();

                long time = ((System.nanoTime()) - rec.getSendTime()) / 1000000;

                min = Math.min(min, time);
                max = Math.max(max, time);
                timeSum += time;
                count++;

                if (System.nanoTime() - lastReport > REPORT_INTERVAL * 1000000) {
                    long avg = timeSum / count;
                    logger.info("Roundtrip time avg " + avg + "ms " + username + " min " + min + " max " + max);
                    timeSum = 0;
                    lastReport = System.nanoTime();
                    count = 0;
                    min = Long.MAX_VALUE;
                    max = 0;
                }
            } else {
                logger.warning("No Time record for " + transform.getTranslation(null) + " queue size " + messageTimes.size());
//                if (messageTimes.size()!=0)
//                    logger.warning("HEAD "+messageTimes.getFirst().transform.getTranslation(null));
            }
        }

        public void messageSent(CellTransform transform) {
            messageTimes.add(new TimeRecord(transform, System.nanoTime()));
        }
    }
