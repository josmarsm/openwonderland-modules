/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents multiple choice question with immediate feedback.
 *
 * @author vlada
 */
@XmlRootElement (name = "auto-question-choice")
public class AutoQuestionChoice implements Serializable {
    
    private String imageUrl;
    private List<String> answers;
    private int correctIndex;
    private List<String> feedbacks;
    
    private String globalFeedbackCorrect;
    private String globalFeedbackWrong;
    
    public AutoQuestionChoice(){
        answers = new ArrayList<String>();
        feedbacks = new ArrayList<String>();
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

    /**
     * @return the answers
     */
    public List<String> getAnswers() {
        return answers;
    }

    /**
     * @param answers the answers to set
     */
    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    /**
     * @return the correctIndex
     */
    public int getCorrectIndex() {
        return correctIndex;
    }

    /**
     * @param correctIndex the correctIndex to set
     */
    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    /**
     * @return the feedbacks
     */
    public List<String> getFeedbacks() {
        return feedbacks;
    }

    /**
     * @param feedbacks the feedbacks to set
     */
    public void setFeedbacks(List<String> feedbacks) {
        this.feedbacks = feedbacks;
    }
    
    /**
     * Adds possible answer with data.
     * 
     * @param answer answer
     * @param feedback feedback
     * @param correct  is answer correct or not
     */
    public void addAnswer(String answer, String feedback, boolean correct){
        answers.add(answer);
        feedbacks.add(feedback);
        if(correct){
            correctIndex = answers.size() - 1;
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
    
}
