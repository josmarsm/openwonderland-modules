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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Generic result corresponding to GenericSheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name = "generic-result")
public class GenericResult extends ResultDetails {

    private String answer;
    private List<GenericAnswer> answers;

    public GenericResult() {
        answers = new ArrayList<GenericAnswer>();
    }

    public List<GenericAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<GenericAnswer> answers) {
        this.answers = new ArrayList(answers);
    }

    public void addAnswer(GenericAnswer answer) {
        answers.add(answer);
    }

    public void removeAnswer(GenericAnswer answer) {
        answers.remove(answer);
    }
    // <editor-fold defaultstate="collapsed" desc="Legacy code from Sample Sheet project">
//    public String getAnswer() {
//        return answer;
//    }
//
//    public void setAnswer(String answer) {
//        this.answer = answer;
//    }
    //</editor-fold>

    @Override
    public List<String> getResultValues(List<String> list, SheetDetails sd) {
        List<String> values = new ArrayList<String>();
        for (GenericAnswer answer1 : answers) {
            values.add(answer1.getValue());
        }
        return values;
    }
}
