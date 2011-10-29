/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

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
     * Get messages generated from running this test.
     * @return the messages from this test, or an empty String if the test
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
}
