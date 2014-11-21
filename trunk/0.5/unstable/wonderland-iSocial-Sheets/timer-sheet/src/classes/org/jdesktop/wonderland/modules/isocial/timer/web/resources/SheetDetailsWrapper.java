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
package org.jdesktop.wonderland.modules.isocial.timer.web.resources;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.modules.isocial.timer.common.TimerSection;

/**
 *
 * @author ryan
 */
@XmlSeeAlso(TimerSection.class)
@XmlRootElement(name="SheetDetailsWrapper")
public class SheetDetailsWrapper {
    private static final Logger LOGGER =
            Logger.getLogger(SheetDetailsWrapper.class.getName());
    
    private String unitId;
    private String lessonId;
    private String sheetId;
    private String sheetTitle;
    private String directions;
    private boolean singleton = false;

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }
    private boolean autoOpen = false;

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }
    private boolean dockable = false;

    private final List<TimerSection> sections;

    public SheetDetailsWrapper() {
        sections = new ArrayList<TimerSection>();
    }
    
    public String getUnitId() {
        return unitId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public String getSheetId() {
        return sheetId;
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public boolean getDockable() {
        return dockable;
    }

    @XmlJavaTypeAdapter(SectionWrapperAdapter.class)
    public List<TimerSection> getSections() {
        return sections;
    }

    // needed because we use an adapter
    public void setSections(List<TimerSection> sections) {
        this.sections.clear();
        this.sections.addAll(sections);
    }
    
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public void setSheetTitle(String sheetTitle) {
        this.sheetTitle = sheetTitle;
    }

    public void setDockable(boolean dockable) {
        this.dockable = dockable;
    }

    @Override
    public String toString() {
        return "unitid: "+unitId
                +"\nlessonid: "+lessonId
                +"\nsheetid: "+sheetId
                +"\ntitle: "+sheetTitle
                +"\npublish? "+autoOpen
                +"\nsections: "+sections;
    }

    @XmlRootElement(name="SectionWrapper")
    public static class SectionWrapper {
        private TimerSection sections;

        public SectionWrapper() {
        }

        public SectionWrapper(TimerSection section) {
            this.sections= section;
        }

        @XmlElementRef
        public TimerSection getSection() {
            return sections;
        }

        public void setSection(TimerSection section) {
            this.sections = section;
        }
    }
    
    public static class SectionWrapperAdapter extends XmlAdapter<
            SectionWrapper[], List<TimerSection>>
    {

        @Override
        public List<TimerSection> unmarshal(SectionWrapper[] vt) throws Exception {
            List<TimerSection> out = new ArrayList<TimerSection>();
            for (SectionWrapper q : vt) {
                out.add(q.getSection());
            }
            return out;
        }

        @Override
        public SectionWrapper[] marshal(List<TimerSection> bt) throws Exception {
            List<SectionWrapper> out = new ArrayList<SectionWrapper>();
            for (TimerSection q : bt) {
                out.add(new SectionWrapper(q));
            }
            return out.toArray(new SectionWrapper[out.size()]);
        }


    }

}
