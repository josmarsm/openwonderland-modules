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
package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A set of dated objects.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedSet<T extends DatedObject> extends LinkedHashSet<T> {
    /**
     * Create an empty dated set.
     */
    public DatedSet() {
        super ();
    }

    /**
     * Create a new dated set from the given collection of dated objects
     * @param objs the dates objects to add to this set
     */
    public DatedSet(Collection<T> objs) {
        this();

        for (T obj : objs) {
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
    public DatedSet<T> rangeSet(final TimelineDate range) {
        DatedSet out = new DatedSet();

        // go through each object looking for whether the given range
        // includes that object
        for (DatedObject d : this) {
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
    public DatedSet<T> containsSet(final TimelineDate date) {
        DatedSet out = new DatedSet();

        // go through each object looking for whether the given range
        // includes that object.
        for (DatedObject d : this) {
            if (d.getDate().contains(date)) {
                out.add(d);
            }
        }

        return out;
    }

    /**
     * Get the set of all dates in ascending order.
     * @return the set of all dates, in descending order
     */
    public List<T> ascendingList() {
        ArrayList<T> out = new ArrayList<T>(this);
        Collections.sort(out, new DatedObjectComparator(true));
        return out;
    }

    /**
     * Get the set of all dates in descending order.
     * @return the set of all dates, in descending order
     */
    public List<T> descendingList() {
        ArrayList<T> out = new ArrayList<T>(this);
        Collections.sort(out, new DatedObjectComparator(false));
        return out;
    }

    /**
     * Compare dated objects by comparing the underlying timeline date for
     * each object. Note this will compare by minimum date.
     */
    private static final class DatedObjectComparator
            implements Comparator<DatedObject>, Serializable
    {
        private boolean ascending;

        public DatedObjectComparator(boolean ascending) {
            this.ascending = ascending;
        }

        public int compare(DatedObject o1, DatedObject o2) {
            if (ascending) {
                return o1.getDate().compareTo(o2.getDate());
            } else {
                return o2.getDate().compareTo(o1.getDate());
            }
        }
    }
}
