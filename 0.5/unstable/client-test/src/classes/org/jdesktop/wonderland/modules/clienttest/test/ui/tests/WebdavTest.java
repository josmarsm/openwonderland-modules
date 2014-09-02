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
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 * Test TCP port connections
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class WebdavTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(WebdavTest.class.getName());
  
    public TestResult run() {
        try {
            String serverURL = System.getProperty("jnlp.wonderland.server.url");
            String contentURL = serverURL + "webdav/content/modules/installed/client-test/art";
            
            LOGGER.log(Level.INFO, "Connect to webdave resource {0}", contentURL);
            
            WebdavResource resource = new WebdavResource(contentURL);
            WebdavResources children = resource.getChildResources();
            if (children.isEmpty()) {
                LOGGER.log(Level.WARNING, "No children found");
                return TestResult.FAIL;
            }            
            
            LOGGER.log(Level.INFO, "Read resource with {0} children", 
                       children.list().length);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in TCP Ports Test", ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
    
    
}
