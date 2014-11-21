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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * An instance of a lesson
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="instance")
public class Instance extends ISocialModelBase {
    /** the cohort that created this instance */
    private String cohortId;

    /** a copy of the unit this instance is from */
    private Unit unit;

    /** a copy of the lesson this instance is based on */
    private Lesson lesson;

    /** a copy of the sheets for this instance */
    private List<Sheet> sheets = new ArrayList<Sheet>();

    /**
     * Default no-arg constructor used by JAXB
     */
    protected Instance() {
    }

    /**
     * Create a new instance with the given information
     * @param cohortId the id of the cohort
     * @param unit a copy of the unit this instance is based on
     * @param lesson a copy of the lesson this instance is based on
     * @param sheets a list of sheets this instance uses
     */
    public Instance(String cohortId, Unit unit, Lesson lesson, 
                    List<Sheet> sheets)
    {
        this.cohortId = cohortId;
        this.unit = unit;
        this.lesson = lesson;
        this.sheets.addAll(sheets);
    }

    public String getCohortId() {
        return cohortId;
    }

    public void setCohortId(String cohortId) {
        this.cohortId = cohortId;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public Sheet getSheet(String sheetId) {
        for (Sheet sheet : getSheets()) {
            if (sheet.getId().equals(sheetId)) {
                return sheet;
            }
        }

        return null;
    }

    public List<Sheet> getSheets() {
        return sheets;
    }

    public void setSheets(List<Sheet> sheets) {
        this.sheets = sheets;
    }
}
