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
        frame.reset();
                
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
        
        boolean result = false;
        try {
            result = frame.waitForAnswer();
        } catch (InterruptedException ie) {
            // ignore
        }
        
        if (result) {
            return TestResult.PASS;
        } else {
            return TestResult.NOT_RUN;
        }
    }
}
