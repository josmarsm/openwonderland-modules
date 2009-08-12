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

package org.jdesktop.wonderland.modules.timeline.common.layout;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;

/**
 *
 * @author drew
 */
@XmlRootElement(name="dated-object-component-state")
@ServerState
public class DatedObjectComponentServerState extends CellComponentServerState {

    @XmlElement(name="dated-object")
    private DatedObject datedObject;

    @XmlElement(name="added-to-timeline")
    private boolean addedToTimeline = false;

    @XmlElement(name="needs-layout")
    private boolean needsLayout = true;


    public DatedObjectComponentServerState() {
    }

    @XmlTransient
    public boolean isAddedToTimeline() { return addedToTimeline; }
    public void setAddedToTimeline(boolean addedToTimeline) {
        this.addedToTimeline = addedToTimeline;
    }

    @XmlTransient
    public DatedObject getDatedObject() { return datedObject; }
    public void setDatedObject(DatedObject datedObject) {
        this.datedObject = datedObject;
    }

    @XmlTransient
    public boolean isNeedsLayout() {    return needsLayout; }
    public void setNeedsLayout(boolean needsLayout) {
        this.needsLayout = needsLayout;
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.timeline.server.layout.DatedObjectComponentMO";
    }

}
