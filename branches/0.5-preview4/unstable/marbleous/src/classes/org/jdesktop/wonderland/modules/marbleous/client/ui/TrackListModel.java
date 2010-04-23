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

import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;

/**
 * List Model that adapts a roller coaster track.
 * @author Bernard Horan
 */
public class TrackListModel extends AbstractListModel {
    private static Logger logger = Logger.getLogger(TrackListModel.class.getName());


    private Track track;

    public TrackListModel(Track aTrack) {
        track = aTrack;
    }

    public Object getElementAt(int index) {
        return track.getTrackSegmentAt(index);
    }

    public int getSize() {
        return track.getSegmentCount();
    }

    public Track getTrack() {
        return track;
    }

    public void addSegment(TrackSegment newSegment) {
        int index = getSize();
        track.addTrackSegment(newSegment);
        fireIntervalAdded(this, index, index);
    }

    public void modifySegment(TrackSegment segment) {
        logger.info("TrackListModel modify segment");
        int index = track.indexOf(segment);
        track.replaceTrackSegment(segment);
        fireContentsChanged(this, index, index);
    }

    public void removeSegment(TrackSegment oldSegment) {
        int index = track.indexOf(oldSegment);
        track.removeTrackSegment(oldSegment);
         if (index >= 0) {
             fireIntervalRemoved(this, index, index);
         }
    }
}

