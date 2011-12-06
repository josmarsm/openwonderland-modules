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

/**
 *
 * @author jkaplan
 */
public interface RunnableTestSection extends TestSection {
    /**
     * Called when the section is started (always after it is made visible)
     */
    void sectionStarted();
    
    /**
     * Called if the panel is skipped
     */
    void sectionSkipped();
    
    /**
     * Get the currently running test
     * @return the currently running test, or null if no test is currently
     * running.
     */
    Test getCurrentTest();
    
    /**
     * Get the result of running this section
     * @return the result of running this section, or TestResult.NOT_RUN if
     * the section has not been run
     */
    TestResult getResult();
}
