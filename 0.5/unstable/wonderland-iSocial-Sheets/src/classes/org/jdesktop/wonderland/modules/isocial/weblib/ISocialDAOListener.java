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
package org.jdesktop.wonderland.modules.isocial.weblib;

import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;

/**
 * Listener that is notified when the ISocialDAO changes
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public interface ISocialDAOListener {
    /**
     * Called when an object is added to the DAO
     * @param obj the object that was added
     */
    public void added(ISocialModelBase obj);

    /**
     * Called when an object in the DAO is updated
     * @param oldObj the original object (before modification)
     * @param newObj the object after modification
     */
    public void updated(ISocialModelBase oldObj, ISocialModelBase newObj);

    /**
     * Called when an object is removed from the DAO
     * @param obj the object that was removed
     */
    public void removed(ISocialModelBase obj);

    /**
     * Called when the current instance changes
     * @param instanceId the ID of the new current instance
     */
    public void currentInstanceChanged(String instanceId);
}
