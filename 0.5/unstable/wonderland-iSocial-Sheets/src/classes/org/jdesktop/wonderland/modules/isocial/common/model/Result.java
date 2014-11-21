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
package org.jdesktop.wonderland.modules.isocial.common.model;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * The abstract superclass of all results
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="result")
public class Result extends ISocialModelBase {
    /** the id of the instance this result is part of */
    private String instanceId;

    /** the id of the sheet within the instance that generated this result */
    private String sheetId;

    /** whether this result is hidden from students */
    private boolean hidden = false;

    /** the result details */
    private ResultDetails details;

    /** the result metadata */
    private ResultMetadata metadata = new ResultMetadata();
    
    /**
     * Default, no-arg constructor
     */
    protected Result() {
    }

    /**
     * Create a new result for the given instance
     * @param instanceId the id of the associated instance
     * @param sheetId the id of the sheet that generated this result
     */
    public Result(String instanceId, String sheetId) {
        this.instanceId = instanceId;
        this.sheetId = sheetId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public ResultDetails getDetails() {
        return details;
    }

    public void setDetails(ResultDetails details) {
        this.details = details;
    }
    
    public ResultMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(ResultMetadata metadata) {
        this.metadata = metadata;
    }
}
