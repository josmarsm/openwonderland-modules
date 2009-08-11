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

package org.jdesktop.wonderland.modules.timeline.common;

import java.awt.Color;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 * Represents an individual timeline segment. A segment maps 1:1 onto a specific
 * time period, eg a day, month, or year.
 *
 * @author drew
 */
public class TimelineSegment implements DatedObject {

    private TimelineDate date;

    private CellTransform transform;

    private String treatment;

    // Just making this up. I imagine we'll change it when we have a better
    // sense of what needs to be here.
    public TimelineSegment(TimelineDate date) {
        this.date = date;
    }

    public TimelineDate getDate() {
        return date;
    }

    public CellTransform getTransform() {
        return transform;
    }

    public void setTransform(CellTransform transform) {
        this.transform = transform;
    }

    public void setColor(Color c) {
        // We're going to want to do this, but this will need to call into
        // the renderer. Maybe we need a client version of this that extends
        // the common one that does set color operations on the jme objects?
        throw new UnsupportedOperationException("Not implemented in the common version of this object - server/client versions will handle this differently.");
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    @Override
    public String toString() {
        return date + " (@" + transform + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TimelineSegment))
            return false;
        
        TimelineSegment seg = (TimelineSegment)o;

        // Is this the right way to do this? I'm not really sure.
        return seg.date.equals(this.date) && seg.transform.equals(this.transform);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = 29 * hash + (this.transform != null ? this.transform.hashCode() : 0);
        return hash;
    }
}
