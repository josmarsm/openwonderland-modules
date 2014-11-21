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
 * A unit
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ISocialModel
@XmlRootElement(name="unit")
public class Unit extends ISocialModelBase {
    /** the display name of this unit */
    private String name;

    /**
     * Default no-arg constructor used by JAXB
     */
    public Unit() {
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(ISocialModelBase o) {
        if (o instanceof Unit) {
            int res = getName().compareTo(((Unit) o).getName());
            if (res != 0) {
                return res;
            }
        }

        return super.compareTo(o);
    }
}
