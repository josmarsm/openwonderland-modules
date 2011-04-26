/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 *
 * @author JagWire
 */
public class CommonConnectionType extends ConnectionType {

    public static final CommonConnectionType COMMON_TYPE = new CommonConnectionType("__CommonClient");

    public CommonConnectionType(String connectionType) {
        super(connectionType);
    }
}
