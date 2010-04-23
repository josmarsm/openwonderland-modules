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
package org.jdesktop.wonderland.modules.timeline.server.childcell;

import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipantProvider;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.server.layout.DatedLayoutParticipantImpl;
import org.jdesktop.wonderland.server.cell.ModelCellMO;

/**
 * Model cell that returns a DatedLayoutParticipant.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineModelCellMO extends ModelCellMO
    implements LayoutParticipantProvider
{
    private DatedObject datedObj;

    public TimelineModelCellMO(DatedObject datedObj) {
        this.datedObj = datedObj;
    }

    public LayoutParticipant getLayoutParticipant() {
        return new DatedLayoutParticipantImpl(this, datedObj);
    }

}
