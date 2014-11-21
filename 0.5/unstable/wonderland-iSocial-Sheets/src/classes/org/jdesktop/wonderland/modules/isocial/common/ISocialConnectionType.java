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
package org.jdesktop.wonderland.modules.isocial.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * The connection type for the isocial sheets
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialConnectionType extends ConnectionType {
    /** The admin tools client type */
    public static final ConnectionType CONNECTION_TYPE =
            new ISocialConnectionType();
    
    /** Use the static CLIENT_TYPE, not this constructor */
    public ISocialConnectionType() {
        super ("__iSocialConnection");
    }

}
