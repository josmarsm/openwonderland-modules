/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.programmingdemo.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.modules.tooltip.client.TooltipCellComponent;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ComponentLookupClass(TooltipCellComponent.class)
public class CodeTooltipComponent extends TooltipCellComponent {
    private String text = null;

    public CodeTooltipComponent(Cell cell) {
        super (cell);
    }

    @Override
    public String getText() {
        if (text == null) {
            return super.getText();
        }

        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
