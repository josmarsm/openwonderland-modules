/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to
 * the "Classpath" exception as provided WonderBuilders, Inc. in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;

/**
 * Panel that displays a single text field with a variable number of lines.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class FieldQuestionPanel extends JTextArea 
    implements StandardQuestionPanel 
{
    private final StandardQuestion question;
    private final TextQuestionPanel label;
    private final String instructions;
    private boolean instructionsShown = true;
    
    public FieldQuestionPanel(StandardQuestion question) {
        
        this.question = question;
        this.label = new TextQuestionPanel(question.getText());
        
        int lines = 2;
        String linesStr = question.getProperties().get("lines");
        if (linesStr != null && (!linesStr.equals(""))) {
            lines = Integer.parseInt(linesStr);
        }
        
        this.instructions = question.getProperties().get("instructions");
        
        setRows(lines);
        setLineWrap(true);
        setWrapStyleWord(true);
        
        showInstructions();
    
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent fe) {
                if (instructionsShown) {
                    hideInstructions();
                }
            }

            public void focusLost(FocusEvent fe) {
                if (getText().length() == 0) {
                    showInstructions();
                }
            }            
        });
    }
        
    public int getId() {
        return question.getId();
    }
    
    public StandardAnswer getAnswer() {
        if (instructionsShown || getText().length() == 0) {
            return null;
        }
        
        return new StandardAnswer(question, getText());
    }
   
    public void renderAnswer(StandardAnswer answer) {
        hideInstructions();
        setText(answer.getValueString());
    }

    public void clearAnswer() {
        setText("");
        showInstructions();
    }

    public Collection<JComponent> getJComponents(int width) {
        ArrayList<JComponent> out = new ArrayList<JComponent>(2);
        out.addAll(label.getJComponents(width));
        out.add(this);
        
        return out;
    }
    
    protected String getInstructions() {
        return instructions;
    }
    
    private void showInstructions() {
        setForeground(Color.GRAY);
        setText(instructions);
        instructionsShown = true;
    }
    
    private void hideInstructions() {
        setForeground(Color.BLACK);
        setText("");
        instructionsShown = false;
    }
}
