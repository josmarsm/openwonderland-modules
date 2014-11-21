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
package org.jdesktop.wonderland.modules.isocial.sample.common;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the sample sheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="sample-sheet")
public class SampleSheet extends SheetDetails {
    private static final String TYPE_NAME = "Sample Sheet";
    private static final String TYPE_DESC = "Sample sheet with one question";

    private String name = "Unconfigured question";
    private String question = "What is your name?";
    private boolean autoOpen = false;

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getTypeDescription() {
        return TYPE_DESC;
    }

    @Override
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    @Override
    public List<String> getResultHeadings() {
        return Collections.singletonList(getName());
    }

    @Override
    @XmlElement
    public String getEditURL() {
        return "isocial-sample.war/edit";
    }
}
