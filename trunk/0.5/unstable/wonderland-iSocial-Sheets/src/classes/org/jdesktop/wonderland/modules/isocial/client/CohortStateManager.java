/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.client;

import java.io.IOException;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortState;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortStateDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSBoolean;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSDouble;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSFloat;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSInteger;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSLong;
import org.jdesktop.wonderland.modules.isocial.common.model.state.CSString;

/**
 * Manage per-cohort state
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public enum CohortStateManager {
    INSTANCE;
    
    public static CohortStateManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get a string value from cohort state
     * @param key the key to get
     * @return the string value associated with key, or null if no value
     * is stored
     */
    public String getString(String key) throws IOException {
        CSString value = getState(key, CSString.class);
        if (value == null) {
            return null;
        }
        
        return value.getValue();
    }
    
    /**
     * Store a string value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setString(String key, String value) throws IOException {
        setState(key, CSString.valueOf(value));
    }
    
    /**
     * Get a boolean value from cohort state
     * @param key the key to get
     * @return the boolean value associated with key, or false if no
     * value is stored
     */
    public boolean getBoolean(String key) throws IOException {
        CSBoolean value = getState(key, CSBoolean.class);
        if (value == null) {
            return false;
        }
        
        return value.getValue();
    }
    
    /**
     * Store boolean value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setBoolean(String key, boolean value) throws IOException {
        setState(key, CSBoolean.valueOf(value));
    }
    
      /**
     * Get a double value from cohort state
     * @param key the key to get
     * @return the double value associated with key, or 0 if no value is stored
     */
    public double getDouble(String key) throws IOException {
        CSDouble value = getState(key, CSDouble.class);
        if (value == null) {
            return 0;
        }
        
        return value.getValue();
    }
    
    /**
     * Store double value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setDouble(String key, double value) throws IOException {
        setState(key, CSDouble.valueOf(value));
    }
    
    /**
     * Get a float value from cohort state
     * @param key the key to get
     * @return the float value associated with key, or 0 if no value is stored
     */
    public float getFloat(String key) throws IOException {
        CSFloat value = getState(key, CSFloat.class);
        if (value == null) {
            return 0;
        }
        
        return value.getValue();
    }
    
    /**
     * Store float value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setFloat(String key, float value) throws IOException {
        setState(key, CSFloat.valueOf(value));
    }
    
        
    /**
     * Get a int value from cohort state
     * @param key the key to get
     * @return the int value associated with key, or 0 if no value is stored
     */
    public int getInt(String key) throws IOException {
        CSInteger value = getState(key, CSInteger.class);
        if (value == null) {
            return 0;
        }
        
        return value.getValue();
    }
    
    /**
     * Store int value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setInt(String key, int value) throws IOException {
        setState(key, CSInteger.valueOf(value));
    }
    
        
    /**
     * Get a long value from cohort state
     * @param key the key to get
     * @return the long value associated with key, or 0 if no value is stored
     */
    public long getLong(String key) throws IOException {
        CSLong value = getState(key, CSLong.class);
        if (value == null) {
            return 0;
        }
        
        return value.getValue();
    }
    
    /**
     * Store long value in cohort state
     * @param key the key to store the state with
     * @param value the value to store
     */
    public void setLong(String key, long value) throws IOException {
        setState(key, CSLong.valueOf(value));
    }
    
    /**
     * Get cohort state by key
     * @param key the key that the state is stored with
     * @param clazz the class to return, a subclass of CohortStateDetails
     * @returns the value stored with the given key, or null if no value
     * is stored for that key
     * @throws ClassCastException if the given key does not return an instance
     * of clazz
     */
    public <T extends CohortStateDetails> T getState(String key, 
                                                     Class<T> clazz)
        throws IOException
    {
        CohortState cs = ISocialManager.INSTANCE.getCohortState(key);
        if (cs == null) {
            return null;
        }
        
        return (T) cs.getDetails();
    }
    
    /**
     * Set cohort state
     * @param key the key to store the object with
     * @param state the state to store
     */
    public void setState(String key, CohortStateDetails state)
        throws IOException
    {
        CohortState cs = new CohortState();
        cs.setKey(key);
        cs.setDetails(state);
        
        ISocialManager.INSTANCE.setCohortState(key, cs);
    }
}
