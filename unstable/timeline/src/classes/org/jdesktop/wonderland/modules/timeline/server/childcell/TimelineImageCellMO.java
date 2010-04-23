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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.imageviewer.server.cell.ImageViewerCellMO;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipantProvider;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.server.layout.DatedLayoutParticipantImpl;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * Image cell that returns a DatedLayoutParticipant.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineImageCellMO extends ImageViewerCellMO
    implements LayoutParticipantProvider
{
    private DatedObject datedObj;

    public TimelineImageCellMO(DatedObject datedObj) {
        this.datedObj = datedObj;
    }

    public LayoutParticipant getLayoutParticipant() {
        return new TimelineImageLayoutParticipant(this, datedObj);
    }

    private static class TimelineImageLayoutParticipant
            extends DatedLayoutParticipantImpl
    {
        private boolean sizeChecked = false;
        private BoundingVolume size;

        public TimelineImageLayoutParticipant(CellMO cell, DatedObject obj) {
            super (cell, obj);
        }

        @Override
        public BoundingVolume getSize() {
            if (!sizeChecked) {
                sizeChecked = true;

                DatedObject obj = getDatedObject();
                if (obj instanceof DatedImage
                        && ((DatedImage) obj).getWidth() > 0
                        && ((DatedImage) obj).getHeight() > 0) {
                    // special case for dated images -- first look at the image
                    // size to guess how big the cell is going to be
                    size = new BoundingBox(new Vector3f(0f, 0f, 0f),
                            ((DatedImage) obj).getWidth() * 0.01f,
                            ((DatedImage) obj).getHeight() * 0.01f,
                            0.1f);
                } else {
                    size = super.getSize();
                }
            }

            return size;
        }
    }
}
