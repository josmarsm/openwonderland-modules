/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import javax.swing.JPanel;
import org.json.simple.JSONObject;

/**
 *
 * @author jkaplan
 */
public interface TestSection {
    /**
     * Initialize the test section
     */
    void initialize(JSONObject config);
    
    /** 
     * Return the internationalized name of the section
     */
    String getName();
    
    /**
     * Return a panel to display the contents of the section
     */
    JPanel getPanel();
    
    /**
     * Called after the panel is made visible
     */
    void sectionVisible();
}
