/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 * Base class for tests. Determines test id and name from config.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public abstract class BaseTest implements Test {
    private static final Logger LOGGER =
            Logger.getLogger(BaseTest.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle");
    
    private String id;
    private String name;
    private TestResult result = TestResult.NOT_RUN;
    private StringBuilder messages = new StringBuilder();
    
    public void initialize(JSONObject config) {
        id = (String) config.get("id");
        if (id == null) {
            id = TestManager.INSTANCE.nextTestId();
        }
        
        // first try to get an unlocalized name from the config and localize
        // it
        name = getUnlocalizedName();
        if (name != null) {
            name = BUNDLE.getString(name);
        }
        
        // if there is no name, set the name to id for now
        if (name == null) {
            name = id;
        }
    }
    
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
        LOGGER.log(Level.INFO, message);
    }

    public void clearMessages() {
        messages = new StringBuilder();
    }
    
    /**
     * Return the unlocalized name of the test. The localized name will be 
     * looked up in the default resource bundle based on the key provided
     * by this method.
     * @return the key to look up in the bundle file, or null by default to
     * not use this method for lookup.
     */
    protected String getUnlocalizedName() {
        return null;
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
