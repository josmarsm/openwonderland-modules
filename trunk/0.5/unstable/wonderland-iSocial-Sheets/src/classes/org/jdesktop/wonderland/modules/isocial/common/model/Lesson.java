/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.common.model;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * A lesson
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="lesson")
public class Lesson extends ISocialModelBase {

    /** the unit this lesson is part of */
    private String unitId;

    /** the display name for this lesson */
    private String name;

    /** the name of the snapshot associated with this lesson */
    private String snapshotName;

    /** the id of the snapshot associated with this lesson */
    private String snapshotId;

    /**
     * Default no-arg constructor used by JAXB
     */
    protected Lesson() {
    }

    /**
     * Create a new lesson in the given unit
     * @param unitId the id of the unit this lesson is associated with
     */
    public Lesson(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public int compareTo(ISocialModelBase o) {
        if (o instanceof Lesson) {
            int res = getName().compareTo(((Lesson) o).getName());
            if (res != 0) {
                return res;
            }
        }

        return super.compareTo(o);
    }
}
