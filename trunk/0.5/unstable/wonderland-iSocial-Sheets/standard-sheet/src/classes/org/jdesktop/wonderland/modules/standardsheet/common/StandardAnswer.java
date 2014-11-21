/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.common;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Answer to a single question on a standard sheet.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="standard-answer")
public class StandardAnswer implements Serializable {
    private int id;
    private String questionTitle;
    private String valueString;
    private Map<String, String> properties = new LinkedHashMap<String, String>();
    
    public StandardAnswer() { 
    }
    
    public StandardAnswer(StandardQuestion question) {
        this (question, null);
    }
    
    public StandardAnswer(StandardQuestion question, String value) {
        id = question.getId();
        questionTitle = question.getText();
        valueString = value;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the title of the question it corresponds to
     */
    public String getQuestionTitle() {
        return questionTitle;
    }
    
    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }
    
    @XmlElement
    @XmlJavaTypeAdapter(XmlMapAdapter.class)
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @XmlElement
    public String getValueString() {
        return valueString;
    }
    
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
   
    @Override
    public String toString() {
        return "title: " + getQuestionTitle() +
               "\nvalue: " + getValueString();
    }
}
