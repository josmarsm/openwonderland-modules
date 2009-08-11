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
package org.jdesktop.wonderland.modules.marbleous.client.ui;

import javax.swing.AbstractListModel;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;

/**
 * List Model that adapts a roller coaster track.
 * //TODO: incomplete
 * @author Bernard Horan
 */
public class TrackListModel extends AbstractListModel {

    private Track track;

    public TrackListModel(Track aTrack) {
        track = aTrack;
    }

    public Object getElementAt(int index) {
        return track.getTrackSegmentAt(index);
    }

    public int getSize() {
        return (track.getSegmentCount());
    }

    void addSegment(TrackSegment newSegment) {
        int index = getSize();
        track.addTrackSegment(newSegment);
        fireIntervalAdded(this, index, index+1);
    }
}

