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

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A set of dated objects.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedSet extends TreeSet<DatedObject> {
    /**
     * Create an empty dated set.
     */
    public DatedSet() {
        super (DatedObjectComparator.INSTANCE);
    }

    /**
     * Create a new dated set from the given collection of dated objects
     * @param objs the dates objects to add to this set
     */
    public DatedSet(Collection<DatedObject> objs) {
        this();

        for (DatedObject obj : objs) {
            add(obj);
        }
    }

    /**
     * Create a dated set by copying the given source set
     * @param source the source set
     */
    public DatedSet(DatedSet source) {
        super (source);
    }

    /**
     * Get all elements in the set within the given date range.  This will
     * return all dates for which the includes() method of the given
     * TimelineDate object returns true.
     * <p>
     * Unlike other set methods, the returned subset is not backed by
     * the original set. It is a separate set, and adding or removing
     * objects will have no effect on it.
     *
     * @param range the date range that must include returned dates.
     * @return a set of dates that are included in the given range, or an
     * empty set if no dates are included.
     */
    public DatedSet rangeSet(final TimelineDate range) {
        DatedSet out = new DatedSet();

        // first, find all possible objects with a minumum greater then or
        // equal to the minimum of range and less than the maximum of
        // range
        SortedSet<DatedObject> rangeSet = subSet(new SearchDatedObject(range.getMinimum()),
                                                 new SearchDatedObject(range.getMaximum()));
        // now go through each object looking for whether the given range
        // includes that object
        for (DatedObject d : rangeSet) {
            if (range.contains(d.getDate())) {
                out.add(d);
            }
        }

        return out;
    }

    /**
     * Get all elements in the given set that contain the given date.
     * * <p>
     * Unlike other set methods, the returned subset is not backed by
     * the original set. It is a separate set, and adding or removing
     * objects will have no effect on it.
     *
     * @param date the date that must be contained in the returned dates.
     * @return a set of dates that contain the given date, or an
     * empty set if no dates are contained.
     */
    public DatedSet containsSet(final TimelineDate date) {
        DatedSet out = new DatedSet();

        // first, find all possible objects with a minumum less than the
        // minimum of the given range
        SortedSet<DatedObject> containsSet = headSet(new SearchDatedObject(date.getMinimum()));

        // now go through each object looking for whether the given range
        // includes that object.  This checks every possible object with a
        // minimum less than date's minimum.  An optimization would be to
        // also look at maximums, but this would require a sorted set
        // of maximums.
        for (DatedObject d : containsSet) {
            if (d.getDate().contains(date)) {
                out.add(d);
            }
        }

        return out;
    }

    /**
     * Compare dated objects by comparing the underlying timeline date for
     * each object. Note this will compare by minimum date.
     */
    private static final class DatedObjectComparator
            implements Comparator<DatedObject>
    {
        public static final DatedObjectComparator INSTANCE =
                new DatedObjectComparator();

        public int compare(DatedObject o1, DatedObject o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    /**
     * A simple DatedObject that contains a single date, for us in searching
     * the sorted set
     */
    private class SearchDatedObject implements DatedObject {
        private TimelineDate date;

        public SearchDatedObject(Date date) {
            this.date = new TimelineDate(date);
        }

        public TimelineDate getDate() {
            return this.date;
        }
    }

}
