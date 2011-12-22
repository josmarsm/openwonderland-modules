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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.clienttest.test.ui.AudioUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 *
 * @author jkaplan
 */
public abstract class BaseAudioTest extends BaseTest {
    private static final Logger LOGGER =
            Logger.getLogger(BaseAudioTest.class.getName());
    
    public TestResult run() {
        AudioUtils.getFrame().reset();
        
        try {
            setup();
        
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    AudioUtils.getFrame().setTitle(getName());
                    AudioUtils.getFrame().setHeadline(getHeadline());
                    AudioUtils.getFrame().setInstructions(getInstructions());
                    AudioUtils.getFrame().setText(getText());
                    AudioUtils.getFrame().setQuestion(getQuestion());
                    AudioUtils.getFrame().setVisible(true);
                }
            });
        
            return AudioUtils.getFrame().waitForAnswer();
        } catch (InterruptedException ie) {
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error running test", ioe);
        } finally {
            cleanup();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {                    
                    hideWindow();
                }
            });
        }
        
        return TestResult.FAIL;
    }
    
    protected abstract void setup() throws IOException;
    protected abstract void cleanup();

    protected void hideWindow() {
        AudioUtils.getFrame().setVisible(false);
    }
    
    protected String getHeadline() {
        return getBundle().getString("Test_Headline");
    }
    
    protected String getInstructions() {
        return getBundle().getString("Test_Instructions");
    }
    
    protected String getText() {
        return getBundle().getString("Test_Text");
    }
    
    protected String getQuestion() {
        return getBundle().getString("Test_Question");
    }
}
