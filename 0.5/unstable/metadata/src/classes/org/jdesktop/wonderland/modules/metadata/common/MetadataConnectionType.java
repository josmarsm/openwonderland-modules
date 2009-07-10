/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 *
 * @author mabonner
 */
public class MetadataConnectionType extends ConnectionType {
    public static final MetadataConnectionType CONN_TYPE =
            new MetadataConnectionType("__MetadataConn");

    private MetadataConnectionType(String typeName) {
        super (typeName);
    }
}
