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
 *
*/
package org.jdesktop.wonderland.modules.marbleous.common.cell.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;

/**
 *
 * @author Bernard Horan
 */
public class TrackCellMessage extends CellMessage {

    private TrackAction action;
    private TrackSegment trackSegment;

    public enum TrackAction {

        ADD_SEGMENT,
        REMOVE_SEGMENT,
        MODIFY_SEGMENT};

    private TrackCellMessage(CellID cellID) {
        super(cellID);
    }

    public static TrackCellMessage addSegment(CellID cellID, TrackSegment aSegment) {
        TrackCellMessage msg = new TrackCellMessage(cellID);
        msg.action = TrackAction.ADD_SEGMENT;
        msg.trackSegment = aSegment;
        return msg;
    }

    public static TrackCellMessage removeSegment(CellID cellID, TrackSegment aSegment) {
        TrackCellMessage msg = new TrackCellMessage(cellID);
        msg.action = TrackAction.REMOVE_SEGMENT;
        msg.trackSegment = aSegment;
        return msg;
    }

    public static TrackCellMessage modifySegment(CellID cellID, TrackSegment aSegment) {
        TrackCellMessage msg = new TrackCellMessage(cellID);
        msg.action = TrackAction.MODIFY_SEGMENT;
        msg.trackSegment = aSegment;
        return msg;
    }

    public TrackAction getAction() {
        return action;
    }

    public TrackSegment getTrackSegment() {
        return trackSegment;
    }
}
