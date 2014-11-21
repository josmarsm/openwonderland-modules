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

import java.util.Collection;
import javax.swing.JComponent;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;

/**
 * Inteface for different types of subpanels displayed by the 
 * StandardSheetPanel.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface StandardQuestionPanel {
    public int getId();
    public Collection<JComponent> getJComponents(int width);
    
    public StandardAnswer getAnswer();
    
    public void renderAnswer(StandardAnswer answer);
    public void clearAnswer();
    
    public void setEditable(boolean editable);
}
