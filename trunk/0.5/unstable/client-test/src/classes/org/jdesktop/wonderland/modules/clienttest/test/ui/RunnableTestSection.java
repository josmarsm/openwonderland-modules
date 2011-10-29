/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
     * Get the result of running this section
     * @return the result of running this section, or TestResult.NOT_RUN if
     * the section has not been run
     */
    TestResult getResult();
}
