/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.client;

import javax.swing.SwingWorker;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;


/**
 *
 * @author Ryan
 */
public class BackgroundNotificationWorker extends SwingWorker<Integer, Integer> {

    private boolean urgent;
    private String message;
    public BackgroundNotificationWorker(boolean urgent) {
        this.urgent = urgent;
    }
    @Override
    protected Integer doInBackground() throws Exception {
        message = GroupChatManager.getNotificationBuffer().poll();
        //sanity check
        if(message == null) {
            System.out.println("Empty queue in BackgroundNotificationWorker");
            return -1;
        }

        //if this isn't the first message, go ahead and sleep
        if(!urgent)  {
            Thread.currentThread().sleep(1000 * 10); //sleep for 10 seconds
        }
        else {
            //if it is the first, return right away so the message is displayed.
            return 0;
        }
        if(!GroupChatManager.getNotificationBuffer().isEmpty()) {
            new BackgroundNotificationWorker(false).execute();
        }
        
        return 0;
    }


    protected void done() {
        //display notification
        System.out.println("Done in BackgrondNotificationWorker.");
        HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
        NotifyPanel panel = new NotifyPanel(message);
        HUDComponent component = hud.createComponent(panel);
        component.setDecoratable(true);
        component.setPreferredLocation(Layout.NORTHEAST);
        component.setVisible(true);
        component.setVisible(false, 10 * 1000);
        return;
    }

}
