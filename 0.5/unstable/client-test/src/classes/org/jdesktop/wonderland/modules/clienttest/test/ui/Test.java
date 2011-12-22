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

import java.util.Map;
import org.json.simple.JSONObject;

/**
 * Base interface for tests. All tests must a have a constructor that
 * takes a single String which is returned by the getID method.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface Test {
    /**
     * Initialize the test from the given object
     * @param config the configuration object
     */
    void initialize(JSONObject config);
    
    /**
     * Get a unique id for this test.
     */
    String getId();
    
    /**
     * Get the internationalized display name for this test
     */
    String getName();
    
    /**
     * Return true if the test is runnable, or false if not
     */
    boolean isRunnable();
    
    /**
     * Run the test and return a result.
     */
    TestResult run();

    /**
     * Get the result of this test
     * @return the result of this test, or TestResult.NOT_RUN if the test
     * has not yet been run
     */
    TestResult getResult();
    
    /**
     * Set the result of this test
     * @param result the result of this test
     */
    void setResult(TestResult result);
    
    /**
     * Get the headline for this test with the given result.
     * @return the headline for this test, or null if the test
     * has not yet been run.
     */
    String getHeadline(TestResult result);
    
    /**
     * Get the fixes for this test with the current result.
     * @return the fixes for this test with the given result, null if there are
     * no fixes.
     */
    String getFixes(TestResult result);
    
    /**
     * Get messages generated from running this test.
     * @return the messages from this test, or an empty string if the test
     * has not yet been run
     */
    String getMessages();
    
    /**
     * Append a message
     * @param message the message to append
     */
    void appendMessage(String message);
    
    /**
     * Clear messages
     */
    void clearMessages();
    
    /**
     * Get properties of this test
     */
    Map<String, Object> getProperties();
}
