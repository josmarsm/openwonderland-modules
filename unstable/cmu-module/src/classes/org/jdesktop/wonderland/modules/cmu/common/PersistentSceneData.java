/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.cmu.common;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;

/**
 * Collection of data which should be loaded for a scene across cell
 * instances, e.g. event data.  Scenes are matched with these objects
 * based on filenames in the content repository; file.a3p's data is stored
 * as file.a3p.data in the appropriate directory in the content repository.
 * @author kevin
 */
public class PersistentSceneData implements Serializable {

    private EventResponseList eventList = null;

    public PersistentSceneData() {
        
    }

    public PersistentSceneData(EventResponseList eventList) {
        this.setEventList(eventList);
    }

    public EventResponseList getEventList() {
        return eventList;
    }

    public void setEventList(EventResponseList eventList) {
        this.eventList = eventList;
    }
}
