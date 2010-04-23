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
package org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * ClickableLinkComponent's ServerState.  99% boilerplate
 * @author Ben (shavnir)
 *
 */
@XmlRootElement(name = "clickableLink-component")
@ServerState
public class ClickableLinkComponentServerState extends CellComponentServerState {

    /**
     * Stores the URL for the link
     */
    private String linkURL;

    @XmlElement
    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.server.ClickableLinkComponentMO";
    }

    public ClickableLinkComponentServerState() {
    }

    public ClickableLinkComponentServerState(String url) {
        this.linkURL = url;
    }
}
