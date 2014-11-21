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
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the Scavenger Hunt sheet
 @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="scavenger-hunt-sheet")
public class ScavengerHuntSheet extends SheetDetails implements Serializable {
    
    /** Default type name for sheet. */
    private static final String TYPE_NAME = "Scavenger Hunt Sheet";
    
    /** Default sheet description. */
    private static final String TYPE_DESC = "Scavenger Hunt configuration sheet";
    
    /** Column names for result. */
    private static final String[] SUMMARY_RESULT_HEADINGS;

    private String name = "Default Scavenger Hunt";
    private String question = "Default question?";
    private boolean autoOpen = false;
    private boolean includeInMenu = false;
    private String instructions;
    private boolean giveUp = false;
    private String giveUpText = "Give up?";
    
    static {
        SUMMARY_RESULT_HEADINGS = new String[]{"Items", "Hints", "Questions", "Time completed"};
    }
    
    public ScavengerHuntSheet(){

    }
    
    

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

    public void setIncludeInMenu(boolean includeInMenu) {
        this.includeInMenu = includeInMenu;
    }

    public boolean isIncludeInMenu() {
        return includeInMenu;
    }

    public void setGiveUp(boolean giveUp) {
        this.giveUp = giveUp;
    }

    public boolean isGiveUp() {
        return giveUp;
    }

    public void setGiveUpText(String giveUpText) {
        this.giveUpText = giveUpText;
    }

    public String getGiveUpText() {
        return giveUpText;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getInstructions() {
        return instructions;
    }
    
    
    @Override
    public List<String> getResultHeadings() {
        return Arrays.asList(SUMMARY_RESULT_HEADINGS);
    }

    @Override
    @XmlElement
    public String getEditURL() {
        return "scavengerhunt-sheet.war/edit";
    }
}
