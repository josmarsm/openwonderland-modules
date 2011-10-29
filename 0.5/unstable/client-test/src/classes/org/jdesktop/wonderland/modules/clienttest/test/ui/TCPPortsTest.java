/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

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

/**
 * Test TCP port connections
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class TCPPortsTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(TCPPortsTest.class.getName());
    
    @Override
    protected String getUnlocalizedName() {
        return "TCP_Ports";
    }
    
    public TestResult run() {
        try {
            String serverURL = System.getProperty("wonderland.server.url");
            appendMessage("Server URL is: " + serverURL);
            
            ServerDetails details = loadDetails(serverURL);
            appendMessage("Got server details: Version: " + details.getVersion() +
                          " URL: " + details.getServerURL());
            
            if (!connectToDarkstar(details)) {
                appendMessage("Unable to connecto to Darkstar server");
                return TestResult.FAIL;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in TCP Ports Test", ex);
            appendMessage("TCP Ports test failed: " + ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
    
    protected ServerDetails loadDetails(String serverURL) 
            throws IOException, JAXBException 
    {
        URL detailsURL = new URL(new URL(serverURL), "wonderland-web-front/resources/ServerDetails");
        LOGGER.log(Level.INFO, "Retrieving server details from " + detailsURL);
        
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
            appendMessage("No Darkstar servers found");
            return false;
        }
        
        DarkstarServer server = details.getDarkstarServers().get(0);
        appendMessage("Using server " + server.getHostname() + " : " +
                       server.getPort());
        
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();
        
        final AtomicBoolean result = new AtomicBoolean(false);
 
        SimpleClient client = new SimpleClient(new SimpleClientListener() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ClientTest", new char[0]);
            }

            public void loggedIn() {
                LOGGER.warning("Login success");
                result.set(true);
                lock.release();
            }

            public void loginFailed(String string) {
                LOGGER.warning("Login failed: " + string);
                
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
                LOGGER.warning("Disconnected: " + string);
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
