/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;
import org.jdesktop.wonderland.modules.isocial.common.model.query.CSVTable;

/**
 * Abstract superclass of all result details
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
public abstract class ResultDetails implements Serializable {
    /**
     * Get the values corresponding to the headings from the sheet
     * this result originates from. The returned list should have the same
     * size as the list of headings passed in. Empty results should be
     * represented by the empty string.
     *
     * @param headings the headings from the sheetDetails
     * @param sheetDetails the sheet that this result originated from
     */
    public abstract List<String> getResultValues(List<String> headings,
                                                 SheetDetails sheetDetails);
    
    /**
     * Get a set of CSV tables customized for this result. The default 
     * implementation returns an empty list, but subclasses can override this
     * to provide details.
     * @param sheet the sheet this detail object is part of
     * @param result the result this detail object is part of
     * @param filter <code>true</code> if result should be filtered for HTML markup
     * @return the CSV tables for this result
     */
    public List<CSVTable> getResultTables(Sheet sheet, Result result, boolean filter) {
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Check if result is empty. Default implementation always returns false.
     * 
     * @return <code>true</code> if result is empty, <code>false</code> otherwise
     */
    public boolean isEmpty(){
        return false;
    }
}
