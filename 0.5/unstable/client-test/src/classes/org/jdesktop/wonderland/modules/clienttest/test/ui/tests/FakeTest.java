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
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;
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
            LOGGER.warning("Fake test message");
            Thread.sleep(duration);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error in Fake Test", ex);
            return TestResult.FAIL;
        }
        
        return TestResult.PASS;
    }
}
