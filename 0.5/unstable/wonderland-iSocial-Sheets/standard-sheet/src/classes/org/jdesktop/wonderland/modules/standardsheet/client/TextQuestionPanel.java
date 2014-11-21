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

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;

/**
 * Panel that displays a text question. Simply displays a label with the
 * given text.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class TextQuestionPanel extends JLabel implements StandardQuestionPanel {
    private final int id;
    
    public TextQuestionPanel(String text) {
        this(text, -1);
    }
    
    public TextQuestionPanel(String text, int id) {
        super ("<html><body><font style='font-size: 10px;font-family: Tahoma'><b>" + text + "</b></font></body></html>");
        this.id = -1;
    }
    
    public int getId() {
        return id;
    }
    
    public Collection<JComponent> getJComponents(int width) {          
        resize(width);
    
        ArrayList<JComponent> out = new ArrayList<JComponent>(1);
        out.add(this);
        return out;
    }
    
    public StandardAnswer getAnswer() {
        return null;
    }

    public void renderAnswer(StandardAnswer answer) {
    }

    public void clearAnswer() {
    }
    
    public void setEditable(boolean editable) {
    }
    
    private void resize(int width) {
        Font font = getFont();  
        int fontHeight = getFontMetrics(font).getHeight();  
        int stringWidth = getFontMetrics(font).stringWidth(getText());  
        int linesCount = (int) Math.floor(stringWidth / width);  
        linesCount = Math.max(1, linesCount + 1);  
        setPreferredSize(new Dimension(width, (fontHeight + 2) * linesCount));
    }

    
}
