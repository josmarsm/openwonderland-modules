/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.generic.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 *
 * @author Ryan
 */
@ISocialModel
@XmlRootElement(name="MultipleChoiceQuestion")
public class MultipleChoiceQuestion extends GenericQuestion implements Serializable {

    private boolean inclusive;    
    private final List<GenericAnswer> answers;

    @Override
    public String toString() {
        return "inclusive: "+inclusive
                +"\nvalue: "+getValue()
                +"\nanswers: "+answers
                +"\ntitle: "+getTitle();

    }
    public MultipleChoiceQuestion() { 
        answers = new ArrayList<GenericAnswer>();
    }
    
    @Override
    @XmlTransient
    public String getTitle() {
        if(inclusive) {
            return "MultipleChoiceInclusive";
        } else {
            return "MultipleChoiceExclusive";
        }
    }
    
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Title cannot be set");
    }
    
    @XmlElement
    @Override
    public List<GenericAnswer> getAnswers() {
        return answers;
    }

//    @XmlTransient
//    public QuestionType getQuestionType() {
//        if(inclusive) {
//            return GenericQuestion.QuestionType.MULTIPLE_CHOICE_INCLUSIVE;
//        }
//        return GenericQuestion.QuestionType.MULTIPLE_CHOICE_EXCLUSIVE;
//    }

    public boolean getInclusive() {
        return this.inclusive;
    }
    
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }
}
