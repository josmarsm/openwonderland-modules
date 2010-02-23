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

package org.jdesktop.wonderland.modules.timeline.server.layout;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.layout.server.cell.CellMOLayoutParticipant;
import org.jdesktop.wonderland.modules.timeline.common.layout.DatedLayoutParticipant;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * An extension of LayoutParticipant with extra data included for dated
 * objects.
 * <p>
 * This implementation also contains optimizations needed to layout a number
 * of participants without touching the underlying ManagedObjects unless
 * necessary.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedLayoutParticipantImpl extends CellMOLayoutParticipant
    implements DatedObject, DatedLayoutParticipant
{
    private DatedObject obj;
    private CellTransform position;
    private BoundingVolume size;
    private boolean sizeChecked = false;

    public DatedLayoutParticipantImpl(CellMO cell, DatedObject obj) {
        super (cell);

        this.obj = obj;
    }

    public DatedObject getDatedObject() {
        return obj;
    }

    public TimelineDate getDate() {
        return obj.getDate();
    }

    @Override
    public CellTransform getPosition() {
        if (position == null) {
            position = super.getPosition();
        }

        return position;
    }

    @Override
    public void setPosition(CellTransform position) {
        // make sure the position is changing before we de-reference
        // the parent
        if (this.position == null || !this.position.equals(position)) {
            this.position = position;
            super.setPosition(position);
        }
    }

    @Override
    public BoundingVolume getSize() {
        if (!sizeChecked) {
            sizeChecked = true;
            
            size = super.getSize();
        }

        return size;
    }

    @Override
    public void setSize(BoundingVolume size) {
        if (this.size == null || this.size.equals(size)) {
            this.size = size;
            this.sizeChecked = true;
            super.setSize(size);
        }
    }
}
