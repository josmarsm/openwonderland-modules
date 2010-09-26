/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.scriptingPoster.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * The cell server state for the PosterCell.<br>
 * Adapted from the Cell server state for the "generic" Cell originally written<br>
 * by Jordan Slott <jslott@dev.java.net>
 *
 * @author Bernard Horan
 */
@XmlRootElement(name="scriptingpostercell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class ScriptingPosterCellServerState extends CellServerState {

    public ScriptingPosterCellServerState() {
        super();
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.scriptingPoster.server.ScriptingPosterCellMO";
    }

}
