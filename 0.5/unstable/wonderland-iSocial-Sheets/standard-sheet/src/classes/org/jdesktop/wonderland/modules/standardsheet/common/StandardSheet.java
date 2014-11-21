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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.SheetDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Sheet configuration for the standard sheet.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="standard-sheet")
public class StandardSheet extends SheetDetails implements Serializable {
    private static final String TYPE_NAME = "Standard Sheet";
    private static final String TYPE_DESC = "Standard sheet allows configuration of multiple questions";

    private String name = "Unconfigured Sheet";
    private boolean autoOpen = false;
    private boolean dockable = false;

    private List<StandardQuestion> questions = new ArrayList<StandardQuestion>();
      
    
    public StandardSheet() {
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

    @XmlElement
    public List<StandardQuestion> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<StandardQuestion> questions) {
        this.questions = questions;
    }

    @XmlElement
    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    @XmlElement
    public boolean isDockable() {
        return dockable;
    }
    public void setDockable(boolean dockable) {
        this.dockable = dockable;
    }

    @Override
    public List<String> getResultHeadings() {
        return Collections.singletonList("Completed");
    }
    
    @Override
    @XmlElement
    public String getEditURL() {
        return "standard-sheet.war/edit";
    }
}
