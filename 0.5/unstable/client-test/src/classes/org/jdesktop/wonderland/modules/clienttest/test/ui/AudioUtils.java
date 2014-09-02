/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.common.NetworkAddress;
import org.jdesktop.wonderland.webstart.SoftphoneInstaller;

/**
 *
 * @author jkaplan
 */
public enum AudioUtils {
    INSTANCE;
    
    private static final Logger LOGGER = 
            Logger.getLogger(AudioUtils.class.getName());
    
    private boolean softphoneInstalled;
    private String registrar;
    private String sipAddress;
    
    private AudioTestFrame frame;
    
    public String startSoftphone() throws IOException {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        
        if (!softphoneInstalled) {
            try {
                Class.forName("org.jdesktop.wonderland.webstart.SoftphoneInstaller");
                
                LOGGER.info("Installing softphone");
                SoftphoneInstaller si = new SoftphoneInstaller();
                si.onStartup();
            } catch (ClassNotFoundException cnfe) {
                LOGGER.info("Not installing softphone for local run");
            } finally {
                softphoneInstalled = true;
            }
        }
        
        if (!sc.isRunning()) {
            String bridgeInfo = System.getProperty("jnlp.voicebridge.info");
            if (bridgeInfo == null) {
                LOGGER.warning("No bridge info. Guessing based on Wonderland server");
                URL serverURL = new URL(System.getProperty("jnlp.wonderland.server.url"));
                bridgeInfo = "null::" + serverURL.getHost() + ":6666:5060:" +
                             serverURL.getHost() + ":6666:5060";
            }
            
            LOGGER.log(Level.INFO, "Launching softphone with bridge {0}", 
                       bridgeInfo);
        
            String[] tokens = bridgeInfo.split(":");
            InetAddress ia = NetworkAddress.getPrivateLocalAddress(
                    "server:" + tokens[5] + ":" + tokens[7] + ":10000");
            
            LOGGER.log(Level.INFO, "Obtained InetAddress: {0}", ia);
            
            registrar = tokens[5] + ":" + tokens[7];
            String registrarAddress = tokens[5] + ";sip-stun:" + tokens[7];
            LOGGER.log(Level.INFO, "Using registar address: {0}", registrarAddress);
            
            sipAddress = sc.startSoftphone("test", registrarAddress, 10, 
                                           ia.getHostAddress());
            LOGGER.log(Level.INFO, "Local sip address is: {0}", sipAddress);
        } else {
            LOGGER.info("Softphone already running");
        }
        
        
        return sipAddress;
    }
    
    public String getRegistrar() {
        return registrar;
    }
    
    public static AudioTestFrame getFrame() {
        return INSTANCE.getFrameInternal();
    }
    
    private synchronized AudioTestFrame getFrameInternal() {
        if (frame == null) {
            Runnable createFrame = new Runnable() {
                public void run() {
                    frame = new AudioTestFrame();
                    frame.pack();
                
                    synchronized (AudioUtils.this) {
                        AudioUtils.this.notifyAll();
                    }
                }
            };
            
            // are we on the AWT event thread already?
            if (SwingUtilities.isEventDispatchThread()) {
                createFrame.run();
            } else {
                // jump onto AWT event thread
                SwingUtilities.invokeLater(createFrame);
            
                try {
                    while (frame == null) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                }
            }
        }
         
        return frame;
    }
}
