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
