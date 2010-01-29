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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A date range in the timeline system. This class reprents a set of dates
 * divided into a set of segments of a given unit size.  It uses calendars
 * internally to calculate the various dates, so handles all date
 * related oddities like leap years correctly.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineDateRange extends TimelineDate {
    /** the set of increments */
    private final List<TimelineDate> increments =
            new ArrayList<TimelineDate>();

    /** the units */
    private int units;

    /**
     * Create a new timeline date with the given minimum and maximum
     * @param min the minimum date
     * @param max the requested maximum date.  The actualy maximum date
     * will be adjusted to fall on an unit boundary
     * @param the unit, as specified in the Calendar class
     */
    public TimelineDateRange(Date min, Date max, int units) {
        this.units = units;

        Calendar cal = Calendar.getInstance();
        cal.setTime(min);

        Calendar end = Calendar.getInstance();
        end.setTime(max);

        Date prev = min;
        while (cal.before(end)) {
            cal.add(units, 1);
            increments.add(new TimelineDate(prev, cal.getTime()));
            prev = cal.getTime();
        }
    }

    /**
     * Create a new timeline date with the given minimum and number of
     * increments
     * @param min the minimum date
     * @param incrementCount the number of increments
     * @param the unit, as specified in the Calendar class
     */
    public TimelineDateRange(Date min, int incrementCount, int units) {
        this.units = units;

        Calendar cal = Calendar.getInstance();
        cal.setTime(min);

        Date prev = min;
        for (int i = 0; i < incrementCount; i++) {
            cal.add(units, 1);
            increments.add(new TimelineDate(prev, cal.getTime()));
            prev = cal.getTime();
        }
    }

    /**
     * Get the units this range is measured in
     * @return the units (from the calendar class) this timeline is
     * measured in
     */
    public int getUnits() {
        return units;
    }

    /**
     * Get the minimum date
     * @return the minimum date
     */
    @Override
    public Date getMinimum() {
        return increments.get(0).getMinimum();
    }

    /**
     * Get the maximum date
     * @return the maximum date
     */
    @Override
    public Date getMaximum() {
        return increments.get(increments.size() - 1).getMaximum();
    }

    /**
     * Get the increments for this timeline
     * @return the increments
     */
    public List<TimelineDate> getIncrements() {
        return increments;
    }

    /**
     * Get the number of increments on this timeline
     * @return the number of increments
     */
    public int getIncrementCount() {
        return increments.size();
    }

    /**
     * Get the increment at the given index
     * @return the increment at the given index
     */
    public TimelineDate getIncrement(int index) {
        return increments.get(index);
    }

    /**
     * Get the date of a given percentage of the timeline.  This will take
     * the given value (between 0 and 1) and map it to a particular date in the
     * range.  It does this by finding the increment the date is in, and then
     * finding the proper percentage within that increment.  This will account
     * for issues with irregular ranges such as leap years.
     * @param percent the percent of the timeline (between 0.0 and 1.0)
     * @return the date of that point in the timeline
     */
    public Date getDate(double percent) {
        // first find what increment we are in
        double increment = percent * getIncrementCount();

        // now divide into an increment and a percentage within that
        // increment
        int incrementIndex = (int) Math.floor(increment);
        double remainder = increment - incrementIndex;

        // find the date range for the given increment
        TimelineDate range = getIncrement(incrementIndex);

        // calculate the index into the increment
        long time = range.getMinimum().getTime() + (long) (remainder * range.getRange());

        // return the exact date
        return new Date(time);
    }

    /**
     * Get the increment of a given percentage of the range.
     * @param percent the percent of the timeline (between 0.0 and 1.0)
     * @return the increment for that percentage
     */
    public TimelineDate getIncrement(double percent) {
        // first find what increment we are in
        double increment = percent * getIncrementCount();

        // now divide into an increment
        int incrementIndex = (int) Math.floor(increment);

        // find the date range for the given increment
        return getIncrement(incrementIndex);
    }

    @Override
    public String toString() {
        return "TimelineDateRange: " + getMinimum() + " -> " + getMaximum();
    }
}
