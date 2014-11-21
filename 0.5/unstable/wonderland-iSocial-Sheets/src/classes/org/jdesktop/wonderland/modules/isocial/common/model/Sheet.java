/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Abstract superclass of all sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="sheet")
public class Sheet extends ISocialModelBase {
    private String unitId;
    private String lessonId;
    private boolean published;
    private SheetDetails details;

    /**
     * Default no-argument constructor for JAXB
     */
    protected Sheet() {
    }

    /**
     * Create a new sheet with the given unitId, lessonId
     */
    public Sheet(String unitId, String lessonId) {
        this.unitId = unitId;
        this.lessonId = lessonId;
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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    /** get the display name of this sheet */
    @XmlElement
    public String getName() {
        if (getDetails() == null) {
            return "Broken sheet";
        }

        return getDetails().getName();
    }

    /** get the name of the sheet type */
    @XmlElement
    public String getType() {
        if (getDetails() == null) {
            return null;
        }

        Class configClass = getDetails().getClass();
        XmlRootElement e = (XmlRootElement) configClass.getAnnotation(XmlRootElement.class);
        return e.name();
    }

    /** get the edit URL for this sheet */
    @XmlElement
    public String getEditURL() {
        if (getDetails() == null) {
            return null;
        }

        return getDetails().getEditURL();
    }

    /** get the configuration of this sheet */
    @XmlElementRef
    public SheetDetails getDetails() {
        return details;
    }

    public void setDetails(SheetDetails config) {
        this.details = config;
    }

    @Override
    public int compareTo(ISocialModelBase o) {
        if (o instanceof Sheet) {
            int res = getName().compareTo(((Sheet) o).getName());
            if (res != 0) {
                return res;
            }
        }

        return super.compareTo(o);
    }
}
