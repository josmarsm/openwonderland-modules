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
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Abstract superclass of all sheet configurations.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
public abstract class SheetDetails implements Serializable {
    /** get the name of this sheet type */
    @XmlTransient
    public abstract String getTypeName();

    /** get a description of this sheet type */
    @XmlTransient
    public String getTypeDescription() {
        return null;
    }

    /** get the display name of this sheet */
    public abstract String getName();

    /** get the URL to edit the sheet, relative to the isocial base URL */
    public abstract String getEditURL();

    /**
     * Get a list of headings for result tables. The corresponding result
     * will provide the values to fill in the table based on these headers.
     */
    @XmlTransient
    public abstract List<String> getResultHeadings();
}
