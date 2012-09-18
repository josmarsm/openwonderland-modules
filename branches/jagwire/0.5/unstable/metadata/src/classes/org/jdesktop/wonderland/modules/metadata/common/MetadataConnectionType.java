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

package org.jdesktop.wonderland.modules.metadata.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * Connection for clients to message MetadataService. Currently used for search and adding/removing 
 * metadataModificationListeners
 * @author mabonner
 */
public class MetadataConnectionType extends ConnectionType {
    public static final MetadataConnectionType CONN_TYPE =
            new MetadataConnectionType("__MetadataConn");

    private MetadataConnectionType(String typeName) {
        super (typeName);
    }
}
