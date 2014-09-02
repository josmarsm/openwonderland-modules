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
package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.login.DarkstarServer;
import org.jdesktop.wonderland.client.login.ServerDetails;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 * Test TCP port connections
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class TCPPortsTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(TCPPortsTest.class.getName());
  
    public TestResult run() {
        try {
            String serverURL = System.getProperty("jnlp.wonderland.server.url");
            LOGGER.log(Level.INFO, "Connect to server {0}", serverURL);
            
            ServerDetails details = loadDetails(serverURL);
            LOGGER.log(Level.INFO, "Got server details: Version: {0} URL: {1}", 
                       new Object[]{details.getVersion(), details.getServerURL()});
            
            if (!connectToDarkstar(details)) {
                LOGGER.warning("Unable to connecto to Darkstar server");
                return TestResult.FAIL;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in TCP Ports Test", ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
    
    protected ServerDetails loadDetails(String serverURL) 
            throws IOException, JAXBException 
    {
        URL detailsURL = new URL(new URL(serverURL), "wonderland-web-front/resources/ServerDetails");
        LOGGER.log(Level.INFO, "Retrieving server details from {0}", detailsURL);
        
        URLConnection detailsURLConn = detailsURL.openConnection();
        detailsURLConn.setRequestProperty("Accept", "application/xml");
        return ServerDetails.decode(new InputStreamReader(detailsURLConn.getInputStream()));
    }
    
    protected boolean connectToDarkstar(ServerDetails details) 
            throws IOException, InterruptedException
    {
        if (details.getDarkstarServers() == null ||
            details.getDarkstarServers().isEmpty()) 
        {
            LOGGER.warning("No Darkstar servers found");
            return false;
        }
        
        DarkstarServer server = details.getDarkstarServers().get(0);
        LOGGER.log(Level.INFO, "Using server {0} : {1}",
                   new Object[]{server.getHostname(), server.getPort()});
        
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();
        
        final AtomicBoolean result = new AtomicBoolean(false);
 
        SimpleClient client = new SimpleClient(new SimpleClientListener() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ClientTest", new char[0]);
            }

            public void loggedIn() {
                LOGGER.info("Login success");
                result.set(true);
                lock.release();
            }

            public void loginFailed(String string) {
                LOGGER.log(Level.INFO, "Login failed: {0}", string);
                
                // if the result is "login refused", that means we were able
                // to connect to the server but not authenticate. That
                // is fine, the port works as expected.
                if (string.equals("login refused")) {
                    result.set(true);
                } else {
                    result.set(false);
                }
                
                lock.release();
            }

            public ClientChannelListener joinedChannel(ClientChannel cc) {
                return null;
            }

            public void receivedMessage(ByteBuffer bb) {
                // ignore
            }

            public void reconnecting() {
                // ignore
            }

            public void reconnected() {
                // ignore
            }

            public void disconnected(boolean bln, String string) {
                LOGGER.log(Level.INFO, "Disconnected: {0}", string);
                result.set(false);
                lock.release();
            }
        });
        
        Properties props = new Properties();
        props.setProperty("host", server.getHostname());
        props.setProperty("port", String.valueOf(server.getPort()));
        client.login(props);
        
        // wait for login to complete
        lock.acquire();
        
        // log out
        if (client.isConnected()) {
            client.logout(true);
        }
        
        return result.get();
    }
}
