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
package org.jdesktop.wonderland.modules.instructortools.client;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupUtils;

/**
 *
 * @author Ryan
 */
public class InstructorSecurityModel {
    
    public static boolean isInstructor(String userName) {
        
        for(String group: getGroups(userName)) {
            if(group.equals("admin")
            || group.equals("guide")) {
                return true;
            }
        }
        
        return false;
        
    }
    
    public static boolean IAmAnInstructor() {
        
        return isInstructor(LoginManager.getPrimary().getUsername());
    }
    
    private static Set<String> getGroups(String userName) {
        Set<GroupDTO> userGroups = new LinkedHashSet<GroupDTO>();
        Set<String> groups = new LinkedHashSet<String>();
        
        ServerSessionManager manager = LoginManager.getPrimary();
//        String username = LoginManager.getPrimary().getUsername();
        
        try {

            userGroups.addAll(GroupUtils.getGroupsForUser(
                                               manager.getServerURL(),
                                               userName,
                                               false,
                                               manager.getCredentialManager()));
            for (GroupDTO d : userGroups) {                
                groups.add(d.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return groups;
        }

    }
    
}
