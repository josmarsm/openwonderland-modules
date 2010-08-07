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
package org.jdesktop.wonderland.modules.programmingdemo.common;

import java.util.List;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message requesting a reordering of the sort cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SortCellOrderMessage extends CellMessage {
    private List<Integer> order;
    
    public SortCellOrderMessage(CellID cellID, List<Integer> order) {
        super(cellID);

        this.order = order;
    }

    public List<Integer> getOrder() {
        return order;
    }
}
