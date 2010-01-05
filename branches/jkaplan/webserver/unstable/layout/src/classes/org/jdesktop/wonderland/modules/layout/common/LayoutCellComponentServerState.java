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

package org.jdesktop.wonderland.modules.layout.common;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.containercell.common.ContainerComponentServerState;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;

/**
 * Server state for layout cell component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="layout-cell-component")
@ServerState
public class LayoutCellComponentServerState extends ContainerComponentServerState {

    // The layout in use by the layout cell component, given as the layout
    // configuration
    @XmlElementRef()
    private LayoutConfig layoutConfig = null;

    /** Default constructor */
    public LayoutCellComponentServerState() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.layout.server.cell.LayoutCellComponentMO";
    }

    @XmlTransient
    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(LayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }
}
