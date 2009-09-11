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
import java.util.Date;

/**
 * A date in the timeline system.  A date actually represents a range of time,
 * with a minimum and maximum value (they may be the same).
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineDate implements Comparable, Serializable {
    /** the minimum date */
    private Date min;

    /** the maximum date */
    private Date max;

    /**
     * Create a new timeline date with the current time.
     */
    public TimelineDate() {
        this (new Date());
    }

    /**
     * Create a new timeline date with the given time as both the minumum
     * and maximum.
     * @param date the date to set as both the minimum and maximum
     */
    public TimelineDate(Date date) {
        this (date, date);
    }

    /**
     * Create a new timeline date with the given minimum and maximum
     * @param min the minimum date
     * @param max the maximum date
     */
    public TimelineDate(Date min, Date max) {
        this.min = min;
        this.max = max;

        if (!min.equals(max) && min.after(max)) {
            throw new IllegalArgumentException("Minimum date must be " +
                    "less than or equal to the maximum date.");
        }
    }

    /**
     * Get the minimum date
     * @return the minimum date
     */
    public Date getMinimum() {
        return min;
    }

    /**
     * Get the "middle" date.  This is the date exactly in between the
     * minimum and the maximum.
     * @return the middle of this date.
     */
    public Date getMiddle() {
        return new Date(min.getTime() + ((max.getTime() - min.getTime()) / 2));
    }

    /**
     * Get the maximum date
     * @return the maximum date
     */
    public Date getMaximum() {
        return max;
    }

    /**
     * Get the range (in ms) between the dates represented by this TimelineDate.
     * @return
     */
    public long getRange() {
        return getMaximum().getTime() - getMinimum().getTime();
    }

    /**
     * Return true if the date range described by this object includes
     * the given date.  This will return true if the given date is greater
     * than or equal to this object's minimum date, and is also strictly less
     * than this object's maximum date.
     * @param date the date to compare
     * @return true if this timeline date includes the given date, or false
     * if not
     */
    public boolean contains(Date date) {
        // check if the minimum is less than or equal to the given date
        boolean before = getMinimum().equals(date) || getMinimum().before(date);

        // make sure the given date is less than the maximum
        return before && getMaximum().after(date);
    }

    /**
     * Return true if this date includes the given timeline date.  This
     * will return true if this object includes both the minimum or maximum
     * value of the given date.
     * @param date the date to compare
     */
    public boolean contains(TimelineDate date) {
        return contains(date.getMinimum()) && contains(date.getMaximum());
    }

    /**
     * Two timeline dates are equal if and only if both the minimum and
     * maximum dates are identical.
     * @param o the other object
     * @return true of the other object is a timeline date with the same
     * maximum and minimum values.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimelineDate)) {
            return false;
        }

        TimelineDate tdo = (TimelineDate) o;
        return getMaximum().equals(tdo.getMaximum()) &&
               getMinimum().equals(tdo.getMinimum());

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 47 * hash + (this.max != null ? this.max.hashCode() : 0);
        return hash;
    }

    /**
     * Compare two timeline date objects.  This comparison is based solely
     * on the minimum value of the objects.
     * @param o the object to compare
     * @return -1 if this object is smaller than the given object, 0 if
     * they are equal or 1 if this given object is greater
     */
    public int compareTo(Object o) {
        if (!(o instanceof TimelineDate)) {
            throw new IllegalArgumentException("Can't comapre to " +
                    o.getClass().getName());
        }

        TimelineDate tdo = (TimelineDate) o;
        return getMinimum().compareTo(tdo.getMinimum());
    }

    @Override
    public String toString() {
        return "TimelineDate: " + min + " -> " + max;
    }
}
