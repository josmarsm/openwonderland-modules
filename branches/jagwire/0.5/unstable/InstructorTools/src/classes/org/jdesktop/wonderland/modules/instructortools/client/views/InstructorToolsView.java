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
package org.jdesktop.wonderland.modules.instructortools.client.views;

import java.awt.event.ActionListener;
import org.jdesktop.wonderland.modules.userlist.client.views.UserListView;

/**
 *
 * @author Ryan
 */
public interface InstructorToolsView extends UserListView {
    
    public void addPullToMeButtonActionListener(ActionListener listener);
    
    public void addSecureClientButtonActionListener(ActionListener listener);
    
    public void addAdjustStudentAudioButtonActionListener(ActionListener listener);
    
    public void addGetScreenshotButtonActionListener(ActionListener listener);
    
    public void addReconnectSoftphoneButtonActionListener(ActionListener listener);
}
