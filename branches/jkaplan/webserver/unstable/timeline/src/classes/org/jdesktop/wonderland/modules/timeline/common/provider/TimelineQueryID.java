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
package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.io.Serializable;

/**
 * A unique identifier for a timeline query and associated results.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineQueryID implements Serializable, Comparable<TimelineQueryID> {
    private int id;

    public TimelineQueryID(int id) {
        this.id = id;
    }

    /**
     * Get the next valid id
     * @return the next valid id
     */
    public TimelineQueryID next() {
        return new TimelineQueryID(id + 1);
    }

    public int compareTo(TimelineQueryID o) {
        return Integer.valueOf(id).compareTo(Integer.valueOf(o.id));
    }

    /**
     * Used for serialization -- clients should not use this directly.
     * @return
     */
    int getID() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimelineQueryID other = (TimelineQueryID) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return "TimelineQuery-" + String.valueOf(id);
    }
}
