/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 *  Sheet for Question component.
 *
 * @author Vladimir Djurovic
 */
@ISocialModel
@XmlRootElement (name = "question-sheet")
public class QuestionSheetDetails extends SheetDetails implements Serializable {
    
    public static final String QUESTION_SHEET_TYPE = "Question Sheet";
    public static final String QUESTION_SHEET_DESCRIPTION = "Custom questions sheet";
    public static final String[] RESULT_HEADINGS = new String[]{"Answered", "Completed on"};
    
    private List<Question> questions;
    private boolean autoOpen;
    
    public QuestionSheetDetails(){
        questions = new ArrayList<Question>();
    }

    @Override
    public String getTypeName() {
        return QUESTION_SHEET_TYPE;
    }

    @Override
    public String getName() {
        return "Default Question sheet";
    }

    @Override
    public String getTypeDescription() {
        return QUESTION_SHEET_DESCRIPTION;
    }

    @Override
    @XmlElement
    public String getEditURL() {
        return "scavengerhunt-sheet.war/edit";
    }

    @XmlElement
    public List<Question> getQuestions() {
        return questions;
    }
    
    /**
     * Adds a new question to this sheet. If there is already an question with the same 
     * <code>cellId</code> as the new question, it is replaced with a new question.
     * 
     * @param question question to add
     */
    public void addQuestion(Question question){
        int index = -1;
        for(int i =0;i < questions.size();i++){
            if(questions.get(i).getCellId().equals(question.getCellId())){
                index = i;
                break;
            }
        }
        if(index >=0){
            questions.set(index, question);
        } else {
            questions.add(question);
        }
    }

    @Override
    public List<String> getResultHeadings() {
        return Arrays.asList(RESULT_HEADINGS);
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    public boolean isAutoOpen() {
        return autoOpen;
    }
    
}
