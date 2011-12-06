/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.clienttest.test.ui.ModelInstructionsFrame;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 *
 * @author jkaplan
 */
public class ModelInstructions extends BaseTest {
    private final ModelInstructionsFrame frame =
            new ModelInstructionsFrame();

    @Override
    public String getName() {
        return "ModelTestInstructions";
    }
        
    public TestResult run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
        
        try {
            frame.waitForAnswer();
        } catch (InterruptedException ie) {
            // ignore
        }
        
        return TestResult.PASS;
    }
}
