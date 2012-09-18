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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.clienttest.test.ui.Test;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;
import org.json.simple.JSONObject;

/**
 * Base class for tests. Determines test id and name from config.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public abstract class BaseTest implements Test {
    private static final Logger LOGGER =
            Logger.getLogger(BaseTest.class.getName());
    private static final String BUNDLE_PREFIX = 
            BaseTest.class.getPackage().getName() + ".resources.";
    
    private final Map<String, Object> properties =
            new LinkedHashMap<String, Object>();
    
    private String id;
    private TestResult result = TestResult.NOT_RUN;
    private StringBuilder messages = new StringBuilder();
        
    public void initialize(JSONObject config) {
        id = (String) config.get("id");
        if (id == null) {
            id = TestManager.INSTANCE.nextTestId();
        }
    }
    
    public String getId() {
        return id;
    }

    public boolean isRunnable() {
        return true;
    }
    
    public String getName() {
        return getBundle().getString("Name");
    }

    public String getHeadline(TestResult result) {
        String key = "Headline_" + result.name();
        if (getBundle().containsKey(key)) {
            return getBundle().getString(key);
        }
        
        return null;
    }
    
    public String getFixes(TestResult result) {
        String key = "Fix_" + result.name();
        if (getBundle().containsKey(key)) {
            return getBundle().getString(key);
        }
        
        return null;
    }
    
    public TestResult getResult() {
        return result;
    }
    
    public void setResult(TestResult result) {
        this.result = result;
    }
    
    public String getMessages() {
        return messages.toString();
    }
    
    public void appendMessage(String message) {
        messages.append(message).append("\n"); 
    }

    public void clearMessages() {
        messages = new StringBuilder();
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    protected String getBundleName() {
        return getClass().getSimpleName();
    }
    
    protected ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_PREFIX + getBundleName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseTest other = (BaseTest) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
