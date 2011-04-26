/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 *
 * @author ryan
 */
public class CommonWebConnectionType extends ConnectionType{
    public static final CommonWebConnectionType COMMON_WEB_TYPE = new CommonWebConnectionType("__CommonWebClient");

    private CommonWebConnectionType(String connectionType) {
        super(connectionType);
    }
}
