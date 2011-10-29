/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.login.ServerDetails;
import org.json.simple.JSONObject;

/**
 * Test that just takes a second to execute
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class FakeTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(FakeTest.class.getName());

    private String name;
    private long duration = 1000;
    
    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
    
        name = (String) config.get("name");
        
        if (config.containsKey("duration")) {
            duration = (Long) config.get("duration");
        }
    }

    @Override
    public String getName() {
        return name;
    }
    
    public TestResult run() {
        try {
            Thread.sleep(duration);
            appendMessage("Test not implemented");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in Fake Test", ex);
            appendMessage("Fake test failed: " + ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
}
