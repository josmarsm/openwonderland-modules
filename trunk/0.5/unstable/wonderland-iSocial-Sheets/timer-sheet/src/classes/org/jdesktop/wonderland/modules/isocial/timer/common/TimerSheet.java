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
package org.jdesktop.wonderland.modules.isocial.timer.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the Generic sheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name = "TimerSheet")
public class TimerSheet extends SheetDetails implements Serializable {

    private static final String TYPE_NAME = "Timer Sheet";
    private static final String TYPE_DESC = "Timer sheet with section names and times.";
    @XmlTransient
    private String[] headings = {"Completed Minutes", "Remaining Minutes"};
    private String name = "Timer Sheet";
//    private String question = "Unconfigured Question";
    private boolean autoOpen = false;
//    private String questionType = "Question Type";
    private boolean dockable = false;
    private boolean singleton = false;
    private String directions = "";
    private List<TimerSection> sections;

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public TimerSheet() {
        //questions = new ArrayList<GenericQuestion>();
        sections = new ArrayList<TimerSection>();
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getTypeDescription() {
        return TYPE_DESC;
    }

    @Override
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    public boolean isDockable() {
        return dockable;
    }

    public void setDockable(boolean dockable) {
        this.dockable = dockable;
    }

    public boolean getSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public List<TimerSection> getSections() {
        return sections;
    }

    public void setSections(List<TimerSection> sections) {
        this.sections = sections;
    }

    @Override
    public List<String> getResultHeadings() {
        List<String> headingList = new ArrayList<String>();
        headingList.addAll(Arrays.asList(headings));
//        for(GenericQuestion question: questions ) {
//            headings.add(question.getValue());
//        }
        return headingList;

        //return Collections.singletonList(getName());
    }

    @Override
    @XmlElement
    public String getEditURL() {
        return "isocial-timer-sheet.war/edit";
    }
}
