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
package org.jdesktop.wonderland.modules.isocial.common.model.state;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortStateDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Float for use in CohortState
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="cs-boolean")
public class CSBoolean extends CohortStateDetails {
    private static final long serialVersionUID = 1L;
    private static final CSBoolean TRUE = new CSBoolean(true);
    private static final CSBoolean FALSE = new CSBoolean(false);
    
    private Boolean value;
    
    /**
     * No arg constructor needed by jaxb.  Use valueOf() when constructing
     * this class directly.
     */
    public CSBoolean() {
    }

    /**
     * Use valueOf() instead of this
     * @param value the value
     */
    private CSBoolean(boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static CSBoolean valueOf(Boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
}
