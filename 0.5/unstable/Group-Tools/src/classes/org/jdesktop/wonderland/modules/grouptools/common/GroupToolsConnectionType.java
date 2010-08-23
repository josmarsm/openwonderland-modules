/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 *
 * @author Ryan Babiuch
 */
public class GroupToolsConnectionType extends ConnectionType  {
    public static final GroupToolsConnectionType GROUP_TYPE = new GroupToolsConnectionType("__GroupTools");

    private GroupToolsConnectionType(String typename) {
        super(typename);
    }

}
