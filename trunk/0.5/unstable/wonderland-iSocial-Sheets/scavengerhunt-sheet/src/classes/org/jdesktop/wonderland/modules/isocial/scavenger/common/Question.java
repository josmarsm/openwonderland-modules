/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents custom questionText in Question sheet.
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "question")
public class Question implements Serializable{
    
    /** Standard question. */
    public static final int QUESTION_TYPE_STANDARD = 0;
    
    /** Auto feedback numeric. */
    public static final int QUESTION_TYPE_NUMERIC = 1;
    
    /** Auto feedback multiple choice. */
    public static final int QUESTION_TYPE_CHOICE = 2;
    
    private String cellId;
    private String questionText;
    private boolean includeAnswer;
    private boolean includeAudio;
    private String answerText;
    private long timestamp;
    private int type;
    private String imageUrl;
    private String sheetType;//----------------------------ADDED FOR ESL AUDIO
    
    /**
     * Optional numeric question.
     */
    private AutoQuestionNumber numericQuestion;
    
    /**
     * Optional multiple choice question.
     */
    private AutoQuestionChoice multipleChoiceQuestion;
    
    public Question(){
        type = QUESTION_TYPE_STANDARD;
    }
    
    public Question(String cellId, String question){
        this();
        this.cellId = cellId;
        this.questionText = question;
        // HACK: set this to "C" by default to avoid conflict with ESL AUDIO code
        sheetType = "C";
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getCellId() {
        return cellId;
    }

    public void setQuestionText(String question) {
        this.questionText = question;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setIncludeAnswer(boolean includeAnswer) {
        this.includeAnswer = includeAnswer;
    }

    public boolean isIncludeAnswer() {
        return includeAnswer;
    }

    public void setIncludeAudio(boolean includeAudio) {
        this.includeAudio = includeAudio;
    }

    public boolean isIncludeAudio() {
        return includeAudio;
    }

    public void setAnswerText(String answertext) {
        this.answerText = answertext;
    }

    public String getAnswerText() {
        return answerText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setSheetType(String sheetType)//////////////////////////////////ADDED FOR ESL AUDIO
    {
        this.sheetType = sheetType;
    }
    
    public String getSheetType()////////////////////////////////////////////////////ADDED FOR ESL AUDIO
    {
        return sheetType;
    }

    public void setNumericQuestion(AutoQuestionNumber numericQuestion) {
        this.numericQuestion = numericQuestion;
    }

    public AutoQuestionNumber getNumericQuestion() {
        return numericQuestion;
    }

    public void setMultipleChoiceQuestion(AutoQuestionChoice multipleChoiceQuestion) {
        this.multipleChoiceQuestion = multipleChoiceQuestion;
    }

    public AutoQuestionChoice getMultipleChoiceQuestion() {
        return multipleChoiceQuestion;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
