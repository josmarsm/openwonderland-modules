/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        return new TimelineQueryID(id++);
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
