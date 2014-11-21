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
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;

/**
 * Base class for parts of the iSocial model.
 *
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public abstract class ISocialModelBase 
        implements Serializable, Comparable<ISocialModelBase>
{
    /** the id of this object */
    private String id;

    /** the creation time for this object */
    private Date created;

    /** the creator of this object */
    private String creator;

    /** the last update time for this object */
    private Date updated;

    /** the last user to update this object */
    private String updater;

    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @XmlElement
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @XmlElement
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @XmlElement
    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ISocialModelBase other = (ISocialModelBase) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    /**
     * Sort by creation date, then last update date, then ID
     */
    public int compareTo(ISocialModelBase o) {
        // first sort by creation date. We return the opposite of the value
        // so that we sort newest to oldest
        if (getCreated() != null && o.getCreated() != null) {
            int res = getCreated().compareTo(o.getCreated());
            if (res != 0) {
                return 0 - res;
            }
        }
        
        // next sort by updated date. Again newest to oldest.
        if (getUpdated() != null && o.getUpdated() != null) {
            int res = getUpdated().compareTo(o.getUpdated());
            if (res != 0) {
                return 0 - res;
            }
        }

        // if everything else is equal, compare ids
        return getId().compareTo(o.getId());
    }
}
