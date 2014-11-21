/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents question with automatic feedback which contains a range of values or
 * a single value that must be entered.
 *
 * @author vlada
 */
@XmlRootElement (name = "auto-question-number")
public class AutoQuestionNumber implements Serializable {
    
    /** Range question. */
    public static final int TYPE_RANGE = 0;
    
    /** Exact value question. */
    public static final int TYPE_EXACT = 1;
    
    private int questionType;
    private float rangeMin;
    private float rangeMax;
    private float exactValue;
    private float answer;
    private String unit;
    private String imageUrl;
    
    private String feedbackLow;
    private String feedbackHigh;
    private String feedbackCorrect;
    
    private String globalFeedbackCorrect;
    private String globalFeedbackWrong;
    
    public AutoQuestionNumber(){
        feedbackLow = "";
        feedbackHigh = "";
        feedbackCorrect = "";
    }

    /**
     * @return the rangeMin
     */
    public float getRangeMin() {
        return rangeMin;
    }

    /**
     * @param rangeMin the rangeMin to set
     */
    public void setRangeMin(float rangeMin) {
        this.rangeMin = rangeMin;
    }

    /**
     * @return the rangeMax
     */
    public float getRangeMax() {
        return rangeMax;
    }

    /**
     * @param rangeMax the rangeMax to set
     */
    public void setRangeMax(float rangeMax) {
        this.rangeMax = rangeMax;
    }

    /**
     * @return the exactValue
     */
    public float getExactValue() {
        return exactValue;
    }

    /**
     * @param exactValue the exactValue to set
     */
    public void setExactValue(float exactValue) {
        this.exactValue = exactValue;
    }

    /**
     * @return the answer
     */
    public float getAnswer() {
        return answer;
    }

    /**
     * @param answer the answer to set
     */
    public void setAnswer(float answer) {
        this.answer = answer;
    }
    
     public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    public int getQuestionType() {
        return questionType;
    }
    
    public boolean isAnswerCorrect(float answer){
        if(questionType == TYPE_RANGE){
            return (answer >= rangeMin && answer <= rangeMax);
        } else {
            return (answer == exactValue);
        }
    }
    
    public String getFeedbackWrong(float value){
        StringBuilder sb = new StringBuilder(globalFeedbackWrong).append(" ");
        if(questionType == TYPE_RANGE){
            if(value < rangeMin){
                sb.append(feedbackLow);
            } else if(value > rangeMax){
                sb.append(feedbackHigh);
            }
        } else {
            if(value < exactValue){
                sb.append(feedbackLow);
            } else if(value > exactValue){
                sb.append(feedbackHigh);
            }
        }
        return sb.toString();
    }

    /**
     * @return the feedbackLow
     */
    public String getFeedbackLow() {
        return feedbackLow;
    }

    /**
     * @param feedbackLow the feedbackLow to set
     */
    public void setFeedbackLow(String feedbackLow) {
        this.feedbackLow = feedbackLow;
        if(feedbackLow == null){
            this.feedbackLow = "";
        }
    }

    /**
     * @return the feedbackHigh
     */
    public String getFeedbackHigh() {
        return feedbackHigh;
    }

    /**
     * @param feedbackHigh the feedbackHigh to set
     */
    public void setFeedbackHigh(String feedbackHigh) {
        this.feedbackHigh = feedbackHigh;
        if(feedbackHigh == null){
            this.feedbackHigh = "";
        }
    }

    /**
     * @return the feedbackCorrect
     */
    public String getFeedbackCorrect() {
        return feedbackCorrect;
    }

    /**
     * @param feedbackCorrect the feedbackCorrect to set
     */
    public void setFeedbackCorrect(String feedbackCorrect) {
        this.feedbackCorrect = feedbackCorrect;
        if(feedbackCorrect == null){
            this.feedbackCorrect = "";
        }
    }

    /**
     * @return the globalFeedbackCorrect
     */
    public String getGlobalFeedbackCorrect() {
        return globalFeedbackCorrect;
    }

    /**
     * @param globalFeedbackCorrect the globalFeedbackCorrect to set
     */
    public void setGlobalFeedbackCorrect(String globalFeedbackCorrect) {
        this.globalFeedbackCorrect = globalFeedbackCorrect;
    }

    /**
     * @return the globalFeedbackWrong
     */
    public String getGlobalFeedbackWrong() {
        return globalFeedbackWrong;
    }

    /**
     * @param globalFeedbackWrong the globalFeedbackWrong to set
     */
    public void setGlobalFeedbackWrong(String globalFeedbackWrong) {
        this.globalFeedbackWrong = globalFeedbackWrong;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
}
