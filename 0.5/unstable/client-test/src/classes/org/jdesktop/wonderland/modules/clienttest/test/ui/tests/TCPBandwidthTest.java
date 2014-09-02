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

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;
import org.json.simple.JSONObject;

/**
 * Test TCP bandwidth
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class TCPBandwidthTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(TCPBandwidthTest.class.getName());
  
    private float failRate = 1000 * 1000;
    private float warnRate = 8000 * 1000;

    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
        
        if (config.containsKey("failRate")) {
            failRate = Float.parseFloat(String.valueOf(config.get("failRate")));
        }
        
        if (config.containsKey("warningRate")) {
            warnRate = Float.parseFloat(String.valueOf(config.get("warningRate")));
        }
    }
    
    public TestResult run() {
        try {
            String serverURL = System.getProperty("jnlp.wonderland.server.url");
            String contentURL = serverURL + "webdav/content/modules/installed/client-test/art/Hogwarts.kmz";
            
            LOGGER.log(Level.INFO, "Connect to resource {0}", contentURL);
            
            URL u = new URL(contentURL);
            InputStream is = u.openStream();
            
            byte[] buffer = new byte[38272];
            int total = 0;
            long startTime = System.currentTimeMillis();
            int read;
            
            while ((read = is.read(buffer)) > 0) {
                total += read;
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            float rate = (total * 8f) / (totalTime / 1000f);
            
            LOGGER.log(Level.INFO, "Read {0} bytes in {1} ms = {2} bps.",
                       new Object[] { total, totalTime, rate });
            
            if (rate < failRate) {
                return TestResult.FAIL;
            } else if (rate < warnRate) {
                return TestResult.WARNING;
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in TCP Ports Test", ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
    
    
}
