/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.admintools.client;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDDialog.BUTTONS;
import org.jdesktop.wonderland.client.hud.HUDDialog.MESSAGE_TYPE;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.LogViewer;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.admintools.common.AdminToolsConnectionType;
import org.jdesktop.wonderland.modules.admintools.common.BroadcastMessage;
import org.jdesktop.wonderland.modules.admintools.common.DisconnectMessage;
import org.jdesktop.wonderland.modules.admintools.common.ErrorReportMessage;
import org.jdesktop.wonderland.modules.admintools.common.MuteMessage;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClientPlugin;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.errorreport.client.ErrorReportFrame;
import org.jdesktop.wonderland.modules.errorreport.common.ErrorReport;

/**
 * Connection type for admin tools
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class AdminToolsConnection extends BaseConnection {
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsConnection.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.admintools.client.Bundle");

    public ConnectionType getConnectionType() {
        return AdminToolsConnectionType.CONNECTION_TYPE;
    }

    @Override
    public void handleMessage(Message message) {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        if (message instanceof MuteMessage) {
            LOGGER.warning("Muted");

            HUDMessage hm = mainHUD.createMessage(BUNDLE.getString("Muted"),
                    MESSAGE_TYPE.WARNING, BUTTONS.NONE);
            hm.setPreferredLocation(Layout.NORTHEAST);
            mainHUD.addComponent(hm);
            hm.setVisible(true);
            hm.setVisible(false, 10000);

            // OWL issue #128: update the mute UI when we are forced mute
            AudioManagerClientPlugin.getClient().forceMute();
        } else if (message instanceof ErrorReportMessage) {
            ServerSessionManager ssm = LoginManager.getPrimary();
            String reportId = ((ErrorReportMessage) message).getReportID();
            
            try {
                ErrorReport report;
            
                if (reportId == null) {
                    String username = ssm.getUsername();
                    String comments = "Administrator requested error report";
                    report = new ErrorReport(username, new Date(), null, comments);
                } else {
                    report = ErrorReportFrame.read(reportId);
                
                }
            
                String log = ErrorReportFrame.generateErrorReport(LogViewer.INSTANCE.getEntries());
                report.setContent(log);
            
                ErrorReportFrame.submit(report);
                
                HUDMessage hm = mainHUD.createMessage(
                        BUNDLE.getString("Error_Report"),
                        MESSAGE_TYPE.INFO, BUTTONS.NONE);
                hm.setPreferredLocation(Layout.NORTHEAST);
                mainHUD.addComponent(hm);
                hm.setVisible(true);
                hm.setVisible(false, 10000);
            } catch (ContentRepositoryException ex) {
                LOGGER.log(Level.WARNING, "Error submitting report", ex);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "Error submitting report", ex);
            }
        } else if (message instanceof DisconnectMessage) {
            LOGGER.warning("Disconnected");

            // diable auto-reconnect, so we don't immediately log back
            // in
            ClientContextJME.getClientMain().setAutoReconnect(false);

            // show in a real dialog, since HUD dialogs don't look right
            // when you are logged out
            JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(),
                    BUNDLE.getString("Disconnected_By"),
                    BUNDLE.getString("Disconnected"),
                    JOptionPane.ERROR_MESSAGE);
        } else if (message instanceof BroadcastMessage) {
            HUDMessage hm = mainHUD.createMessage(
                    ((BroadcastMessage) message).getText(),
                    MESSAGE_TYPE.INFO, BUTTONS.NONE);
            hm.setPreferredLocation(Layout.NORTHEAST);
            mainHUD.addComponent(hm);
            hm.setVisible(true);
            hm.setVisible(false, 10000);
        }
    }
}
