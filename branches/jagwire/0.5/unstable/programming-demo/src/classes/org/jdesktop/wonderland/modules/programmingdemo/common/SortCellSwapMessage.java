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

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message requesting two items be swapped in a sort cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SortCellSwapMessage extends CellMessage {
    private int index1;
    private int index2;

    public SortCellSwapMessage(CellID cellID, int index1, int index2) {
        super(cellID);

        this.index1 = index1;
        this.index2 = index2;
    }

    public int getIndex1() {
        return index1;
    }

    public int getIndex2() {
        return index2;
    }
}
