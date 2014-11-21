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
package org.jdesktop.wonderland.modules.isocial.tokensheet.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the token sheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 * @author Ryan Babiuch
 */
@ISocialModel
@XmlRootElement(name = "token-sheet")
public class TokenSheet extends SheetDetails implements Serializable {

    private static final String TYPE_NAME = "Token Sheet";
    private static final String TYPE_DESC = "Token sheet with token system information.";
    private String name = "Token sheet";
    private boolean autoOpen = false;
    private boolean dockable = false;
    @XmlTransient
    private String[] headings = {"Tokens", "Passes", "Strikes"};
    private int maxLessonTokens, maxUnitTokens, maxStudents;

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

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    public boolean isDockable() {
        return dockable;
    }

    public void setDockable(boolean dockable) {
        this.dockable = dockable;
    }

    @Override
    public List<String> getResultHeadings() {
        List<String> headingList = new ArrayList<String>();
        headingList.addAll(Arrays.asList(headings));
        return headingList;
    }

    @Override
    @XmlElement
    public String getEditURL() {
        return "isocial-token-sheet.war/edit";
    }

    public void setMaxLessonTokens(int maxLessonTokens) {
        this.maxLessonTokens = maxLessonTokens;
    }

    public int getMaxLessonTokens() {
        return maxLessonTokens;
    }

    public int getMaxUnitTokens() {
        return maxUnitTokens;
    }

    public void setMaxUnitTokens(int maxUnitTokens) {
        this.maxUnitTokens = maxUnitTokens;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }
}
