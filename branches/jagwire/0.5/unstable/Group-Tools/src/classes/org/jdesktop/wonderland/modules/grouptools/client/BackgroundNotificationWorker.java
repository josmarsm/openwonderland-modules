/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.client;

import javax.swing.SwingWorker;
import org.jdesktop.wonderland.client.hud.HUDComponent;


/**
 *
 * @author Ryan
 */
public class BackgroundNotificationWorker extends SwingWorker {
    private final HUDComponent component;
    private final long delay;
    
    public BackgroundNotificationWorker(HUDComponent component, long delay) {
        this.component = component;
        this.delay = delay;
    }

    @Override
    protected Object doInBackground() throws Exception {
        Thread.sleep(delay); //sleep for 10 seconds
        return null;
    }

    @Override
    protected void done() {
        //display notification
        System.out.println("Done in BackgrondNotificationWorker.");

        // hide the component
        component.setVisible(false);

        // remove the message we just finished displaying
        String message = GroupChatManager.getNotificationBuffer().poll();
        if (message == null) {
            // shouldn't happen
            return;
        }
        
        // if there is a new message to display, display it now and schedule
        // a new worker to clean up
        message = GroupChatManager.getNotificationBuffer().peek();
        if (message != null) {
            HUDComponent hc = GroupChatManager.doShowNotification(message);
            new BackgroundNotificationWorker(hc, delay).execute();
        }
    }
}
