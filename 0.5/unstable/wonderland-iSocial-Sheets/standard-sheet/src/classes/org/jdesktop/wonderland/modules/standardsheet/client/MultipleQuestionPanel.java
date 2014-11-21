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
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;

/**
 * Panel that displays a multiple choice question as radio buttons or 
 * check boxes.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class MultipleQuestionPanel implements StandardQuestionPanel {
    private final StandardQuestion question;
    private final TextQuestionPanel label;
    private final ButtonGroup bg = new ButtonGroup();
    private final List<ButtonWrapper> buttons = new ArrayList<ButtonWrapper>();
    
    public MultipleQuestionPanel(StandardQuestion question) {
        this.question = question;
        this.label = new TextQuestionPanel(question.getText());
        
        boolean multiple = false;
        String multipleStr = question.getProperties().get("multiple");
        if (multipleStr != null) {
            multiple = Boolean.parseBoolean(multipleStr);
        }
        
        String choicesStr = question.getProperties().get("choices");
        String[] splitChoices = choicesStr.split("\n");
        int index = 0;
        for (String choice : splitChoices) {
            if (choice.length() > 0) {
                buttons.add(createButtonWrapper(index++, choice, multiple));
            }
        }
        
        boolean other = false;
        String otherStr = question.getProperties().get("other");
        if (otherStr != null) {
            other = Boolean.parseBoolean(otherStr);
        }
        
        String otherText = question.getProperties().get("otherText");
        
        if (other) {
            String otherLabel = "Other";
            if (otherText != null) {
                otherLabel += " (" + otherText + ")";
            }
            otherLabel += ":";
            
            buttons.add(new OtherPanel(index, otherLabel, multiple));
        }
    }
    
    public int getId() {
        return question.getId();
    }
        
    public Collection<JComponent> getJComponents(int width) {
        ArrayList<JComponent> out = new ArrayList<JComponent>();
        out.addAll(label.getJComponents(width));
        
        for (ButtonWrapper wrapper : buttons) {
            out.add(wrapper.getJComponent());
        }
        
        return out;
    }
    
    public void setEditable(boolean editable) {
        for (ButtonWrapper wrapper : buttons) {
            wrapper.setEditable(editable);
        }
    }
    
    public StandardAnswer getAnswer() {
        StringBuilder answerStr = new StringBuilder();
        StringBuilder indexStr = new StringBuilder();
        String otherText = null;
        boolean selected = false;
        
        for (int i = 0; i < buttons.size(); i++) {
            ButtonWrapper wrapper = buttons.get(i);
            if (wrapper.isSelected()) {
                answerStr.append(wrapper.getAnswer()).append("\n");
                indexStr.append(i).append(",");
                
                if (wrapper instanceof OtherPanel) {
                    otherText = ((OtherPanel) wrapper).getOtherText();
                }
                
                selected = true;
            }
        }
        
        if (!selected) {
            return null;
        }
        
        StandardAnswer out = new StandardAnswer(question, answerStr.toString());
        out.getProperties().put("selectedIndices", indexStr.toString());
        
        if (otherText != null) {
            out.getProperties().put("otherText", otherText);
        }
        
        return out;
    }


    public void renderAnswer(StandardAnswer answer) {
        String indices = answer.getProperties().get("selectedIndices");
        if (indices != null) {
            for (String index : indices.split(",")) {
                if (index.length() > 0) {
                    int idx = Integer.parseInt(index);
                    ButtonWrapper wrapper = buttons.get(idx);
                    wrapper.setSelected(true);
                }
            }
        }
        
        String otherText = answer.getProperties().get("otherText");
        if (otherText != null) {
            for (ButtonWrapper wrapper : buttons) {
                if (wrapper instanceof OtherPanel) {
                    ((OtherPanel) wrapper).setOtherText(otherText);
                }
            }
        }
    
    }

    public void clearAnswer() {
        for (ButtonWrapper wrapper : buttons) {
            wrapper.clearAnswer();
        }
        
        bg.clearSelection();
    }
    
    private ButtonWrapper createButtonWrapper(int index, String text, 
                                              boolean multiple)
    {
        return new AbstractButtonWrapper(index, createButton(text, multiple));
    }
    
    private AbstractButton createButton(String text, boolean multiple) {
        AbstractButton out;
        
        if (multiple) {
            out = new JCheckBox(text);
        } else {
            out = new JRadioButton(text);
            bg.add((JRadioButton) out);
        }
        
        return out;
    }
    
    private interface ButtonWrapper {
        JComponent getJComponent();
        int getIndex();
        
        boolean isSelected();
        void setSelected(boolean selected);
        
        String getAnswer();
        void clearAnswer();
        
        void setEditable(boolean editable);
    }
   
    private class AbstractButtonWrapper implements ButtonWrapper {
        private final int index;
        private final AbstractButton button;
        
        public AbstractButtonWrapper(int index, AbstractButton button) {
            this.index = index;
            this.button = button;
        }
        
        public JComponent getJComponent() {
            return button;
        }

        public int getIndex() {
            return index;
        }

        public boolean isSelected() {
            return button.isSelected();
        }

        public void setSelected(boolean selected) {
            button.setSelected(selected);
        }

        public String getAnswer() {
            return button.getText();
        }

        public void clearAnswer() {
            button.setSelected(false);
        }
        
        public void setEditable(boolean editable) {
            button.setEnabled(editable);
        }
    }
    
    private class OtherPanel extends JPanel implements ButtonWrapper {
        private final int index;
        private final AbstractButton button;
        private final JTextArea otherTA;
        
        public OtherPanel(int index, String otherLabel, boolean multiple) {
            this.index = index;
            
            otherTA = new JTextArea();
            otherTA.setRows(1);
            otherTA.setLineWrap(true);
            otherTA.setWrapStyleWord(true);
            
            button = createButton(otherLabel, multiple);
            
            setLayout(new BorderLayout());
            add(button, BorderLayout.LINE_START);
            add(otherTA, BorderLayout.CENTER);
        }
        
        public JComponent getJComponent() {
            return this;
        }

        public int getIndex() {
            return index;
        }
        
        public boolean isSelected() {
            return button.isSelected();
        }
        
        public void setSelected(boolean selected) {
            button.setSelected(selected);
        }
        
        public String getAnswer() {
            return "Other: " + otherTA.getText();
        }

        public void clearAnswer() {
            button.setSelected(false);
            otherTA.setText("");
        }
        
        public void setEditable(boolean editable) {
            button.setEnabled(editable);
            otherTA.setEditable(editable);
        }
        
        public String getOtherText() {
            return otherTA.getText();
        }
        
        public void setOtherText(String otherText) {
            otherTA.setText(otherText);
        }
    }
}
