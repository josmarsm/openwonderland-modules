/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Vladimir Djurovic
 */
public class QuestionComponentClientState extends CellComponentClientState {
    
    private String sheetId;
    private int questionSrc;
    private Question question;
   

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetId() {
        return sheetId;
    }

    public int getQuestionSrc() {
        return questionSrc;
    }

    public void setQuestionSrc(int questionSrc) {
        this.questionSrc = questionSrc;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }
    
}
