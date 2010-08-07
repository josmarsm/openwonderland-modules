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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * WFS server state for sort cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@XmlRootElement(name="sort-cell")
@ServerState
public class SortCellServerState extends CellServerState {
    private CodeCellServerState codeState;

    public SortCellServerState() {
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.programmingdemo.server.SortCellMO";
    }

    @XmlElement(name="code-cell")
    public CodeCellServerState getCodeState() {
        return codeState;
    }

    public void setCodeState(CodeCellServerState codeState) {
        this.codeState = codeState;
    }
}
