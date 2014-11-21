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
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 *
 * @author iSocial (Kaustubh, Ryan)
 */
@ISocialModel
@XmlRootElement(name="GenericAnswer")
public class GenericAnswer implements Serializable {

    private String questionTitle;
    private String value;

    public GenericAnswer() { }
    /**
     *
     * @return the title of the question it corresponds to
     */
    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getValue() {
        return value;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "title: " +questionTitle
                +"\nvalue: "+value;
    }
}
