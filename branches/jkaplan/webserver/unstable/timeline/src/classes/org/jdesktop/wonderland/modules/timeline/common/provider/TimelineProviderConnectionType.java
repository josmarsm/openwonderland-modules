/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.common.provider;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * This class defines the ConnectionType used to send messages between the
 * TimelineProviderConnection and the TimelineProviderConnectionHandler. Use
 * TimelineProviderConnectionType.TYPE for a constant version of this type.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderConnectionType extends ConnectionType {
    /** A unique name for this connection type */
    private static final String NAME = "__TimelineConnection";

    /** The type constant for this connection type */
    public static final TimelineProviderConnectionType TYPE =
            new TimelineProviderConnectionType();

    /**
     * Default constructor
     */
    public TimelineProviderConnectionType() {
        super (NAME);
    }
}
