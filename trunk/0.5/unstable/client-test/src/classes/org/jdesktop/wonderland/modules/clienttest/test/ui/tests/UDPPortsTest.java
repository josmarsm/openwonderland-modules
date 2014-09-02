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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.clienttest.test.ui.AudioUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 * Test TCP port connections
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class UDPPortsTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(UDPPortsTest.class.getName());
  
    public TestResult run() {
        try {
            String bridgeInfo = System.getProperty("jnlp.voicebridge.info");
            LOGGER.log(Level.INFO, "Connect to voice bridge {0}", bridgeInfo);
            
            String addr = AudioUtils.INSTANCE.startSoftphone();
            LOGGER.log(Level.INFO, "Returned sip address is {0}", addr);
            
            if (addr == null) {
                return TestResult.FAIL;
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in UDP Ports Test", ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
    
    
}
