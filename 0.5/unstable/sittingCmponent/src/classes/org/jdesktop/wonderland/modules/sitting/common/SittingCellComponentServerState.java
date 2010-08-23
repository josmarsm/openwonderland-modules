/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.sitting.common;

import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for sitting cell component
 *
 * @author Morris Ford
 */
@XmlRootElement(name="sitting-cell-component")
@ServerState
public class SittingCellComponentServerState extends CellComponentServerState {

    private String info;

    /** Default constructor */
    public SittingCellComponentServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.sitting.server.SittingCellComponentMO";
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        Logger.getLogger(SittingCellComponentServerState.class.getName()).warning("SETTING INFO TO " + info);
        this.info = info;
    }
}
