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
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 *
 * @author iSocial (Kaustubh, Ryan)
 */
@ISocialModel
@XmlSeeAlso({MultipleChoiceQuestion.class, YesNoQuestion.class})
public abstract class GenericQuestion implements Serializable {
    private String title;
    private String value;
    
    /**
     *
     * @return The title of the question (e.g. "Favorite Colors")
     */
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     *
     * @return The actual question to be asked (e.g. "What is your favorite color?")
     */
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }


    /**
     *
     * @return The possible answers to the stated question.
     */
    public List<GenericAnswer> getAnswers() {
        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @return The type of the question (e.g. OPEN_ENDED)
     */
//    public abstract QuestionType getQuestionType();
//
//    public static enum QuestionType {
//        OPEN_ENDED, //includes essay, fill in the blank, short answer, etc...
//        MULTIPLE_CHOICE_INCLUSIVE, // includes checkboxes
//        MULTIPLE_CHOICE_EXCLUSIVE // includes radiobuttons
//    }
}
