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

package org.jdesktop.wonderland.modules.cmu.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * 
 *
 * @author kevin
 */

@XmlRootElement(name="cmu-cell")
@ServerState
public class CMUCellServerState extends CellServerState {

    /** The URI of the CMU file. */
    @XmlElement(name="cmu-uri")
    private String cmuURI = null;

    /** Default constructor. */
    public CMUCellServerState() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.cmu.server.CMUCellMO";
    }

    /**
     * @return The URI of the loaded CMU file.
     */
    @XmlTransient public String getCmuURI() {
        return cmuURI;
    }

    /**
     * @param uri Points to a CMU file to load.
     */
    public void setCmuURI(String uri) {
        cmuURI = uri;
    }

}
