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

package org.jdesktop.wonderland.modules.isocial.generic.web.resources;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericQuestion;

/**
 *
 * @author ryan
 */
@XmlSeeAlso(GenericQuestion.class)
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

    private final List<GenericQuestion> questions;

    public SheetDetailsWrapper() {
        questions = new ArrayList<GenericQuestion>();
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

    @XmlJavaTypeAdapter(QuestionWrapperAdapter.class)
    public List<GenericQuestion> getQuestions() {
        return questions;
    }

    // needed because we use an adapter
    public void setQuestions(List<GenericQuestion> questions) {
        this.questions.clear();
        this.questions.addAll(questions);
    }
    
//    public void setQuestions(List<GenericQuestion> questions) {
//        this.questions = questions;
//    }

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
                +"\nquestions: "+questions;              
    }

    @XmlRootElement(name="QuestionWrapper")
    public static class QuestionWrapper {
        private GenericQuestion question;

        public QuestionWrapper() {
        }

        public QuestionWrapper(GenericQuestion question) {
            this.question= question;
        }

        @XmlElementRef
        public GenericQuestion getQuestion() {
            return question;
        }

        public void setQuestion(GenericQuestion question) {
            this.question = question;
        }
    }
    
    public static class QuestionWrapperAdapter extends XmlAdapter<
            QuestionWrapper[], List<GenericQuestion>>
    {

        @Override
        public List<GenericQuestion> unmarshal(QuestionWrapper[] vt) throws Exception {
            List<GenericQuestion> out = new ArrayList<GenericQuestion>();
            for (QuestionWrapper q : vt) {
                out.add(q.getQuestion());
            }
            return out;
        }

        @Override
        public QuestionWrapper[] marshal(List<GenericQuestion> bt) throws Exception {
            List<QuestionWrapper> out = new ArrayList<QuestionWrapper>();
            for (GenericQuestion q : bt) {
                out.add(new QuestionWrapper(q));
            }
            return out.toArray(new QuestionWrapper[out.size()]);
        }


    }

}
