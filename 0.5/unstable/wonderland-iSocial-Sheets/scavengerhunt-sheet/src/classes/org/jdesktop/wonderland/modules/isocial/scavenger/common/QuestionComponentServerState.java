/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "question-component")
@ServerState
public class QuestionComponentServerState extends CellComponentServerState {
    
    private String sheetId;
    private int questionSrc;
   private Question question;
    
    public QuestionComponentServerState(){
        
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.isocial.scavenger.server.QuestionComponentMO";
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetId() {
        return sheetId;
    }

    public void setQuestionSrc(int questionSrc) {
        this.questionSrc = questionSrc;
    }

    public int getQuestionSrc() {
        return questionSrc;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }
    
}
