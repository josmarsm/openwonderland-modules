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
package org.jdesktop.wonderland.modules.isocial.generic.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the Generic sheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="generic-sheet")
public class GenericSheet extends SheetDetails implements Serializable {
    private static final String TYPE_NAME = "Generic Sheet";
    private static final String TYPE_DESC = "Generic sheet with open ended and multichoice questions.";

    private String name = "Unconfigured Sheet";
//    private String question = "Unconfigured Question";
    private boolean autoOpen = false;
//    private String questionType = "Question Type";
    private boolean dockable = false;

    private boolean singleton = false;

    private String directions = "";

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

      //@XmlElement
      private List<GenericQuestion> questions;
      public GenericSheet() {
          questions = new ArrayList<GenericQuestion>();
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

    //@XmlElementRef
    public List<GenericQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<GenericQuestion> questions) {
        this.questions = questions;
    }

    public void addQuestion(MultipleChoiceQuestion question) {
        questions.add(question);
    }

    public void removeQuestion(GenericQuestion question) {
        if(questions.contains(question)) {
            questions.remove(question);
        }
      
    }



//    public String getQuestion() {
//        return question;
//    }
//
//    public void setQuestion(String question) {
//        this.question = question;
//    }

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

    @Override
    public List<String> getResultHeadings() {
        List<String> headings = new ArrayList<String>();
        for(GenericQuestion question: questions ) {
            headings.add(question.getValue());
        }
        return headings;

        //return Collections.singletonList(getName());
    }
    
    @Override
    @XmlElement
    public String getEditURL() {
        return "isocial-generic-sheet.war/edit";
    }

//    public void setQuestionType(String type) {
//        this.questionType = type;
//    }
//
//    public String getQuestionType(){
//        return questionType;
//    }
}
