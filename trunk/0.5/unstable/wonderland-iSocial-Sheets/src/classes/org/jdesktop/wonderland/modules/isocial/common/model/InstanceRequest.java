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

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * An instance of a lesson
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="instance-request")
public class InstanceRequest extends ISocialModelBase {
    /** the cohort that created this instance */
    private String cohortId;

    /** the unit this instance is from */
    private String unitId;

    /** the lesson this instance is based on */
    private String lessonId;

    /**
     * Default no-arg constructor used by JAXB
     */
    protected InstanceRequest() {
    }

    /**
     * Create a new instance with the given information
     * @param cohortId the id of the cohort
     * @param unitId the unit this instance is based on
     * @param lessonId the lesson this instance is based on
     */
    public InstanceRequest(String cohortId, String unitId, String lessonId)
    {
        this.cohortId = cohortId;
        this.unitId = unitId;
        this.lessonId = lessonId;
    }

    public String getCohortId() {
        return cohortId;
    }

    public void setCohortId(String cohortId) {
        this.cohortId = cohortId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }
}
