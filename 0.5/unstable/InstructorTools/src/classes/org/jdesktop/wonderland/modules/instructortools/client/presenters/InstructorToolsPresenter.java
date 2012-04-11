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
package org.jdesktop.wonderland.modules.instructortools.client.presenters;

import com.jme.math.Vector3f;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.modules.instructortools.client.InstructorClientConnection;
import org.jdesktop.wonderland.modules.instructortools.client.views.InstructorToolsView;
import org.jdesktop.wonderland.modules.userlist.client.WonderlandUserList;
import org.jdesktop.wonderland.modules.userlist.client.presenters.WonderlandUserListPresenter;

/**
 *
 * @author Ryan
 */
public class InstructorToolsPresenter extends WonderlandUserListPresenter {
    
    
    private static Logger logger = Logger.getLogger(InstructorToolsPresenter.class.getName());
    
    public InstructorToolsPresenter(InstructorToolsView view, HUDComponent hc) {
        super(view, hc);
    }
    
    
    private void handlePullToMeButtonPressed(ActionEvent e) {
        
        ViewCell cell = ViewManager.getViewManager().getPrimaryViewCell();
        
        Vector3f position = cell.getLocalTransform().getTranslation(null);
        
        
        InstructorClientConnection client = InstructorClientConnection.getInstance();//.sendPullToMeMessage(position.x,
         
//        client.sendPullToMeMessage(position.x, position.y, position.z);
        Set<BigInteger> IDs = new LinkedHashSet<BigInteger>();
        for(Object name: view.getSelectedEntries()) {
            BigInteger bi = WonderlandUserList.INSTANCE.getSessionIDFromName(name.toString());
            IDs.add(bi);
        }
        
        client.sendPullToMeMessage(IDs, position.x, position.y, position.z);
        
        logger.warning("Pull-To-Me button pressed!");
    }
    
    private void handleSecureClientButtonPressed(ActionEvent e) {
        logger.warning("Secure Client Toggle button pressed!");
    }
    
    private void handleAdjustStudentAudioButtonPressed(ActionEvent e) {
        logger.warning("Adjust student audio button pressed!");
        InstructorClientConnection client = InstructorClientConnection.getInstance();
        
        Set<BigInteger> IDs = new LinkedHashSet<BigInteger>();
        for(Object name: view.getSelectedEntries()) {
            BigInteger bi = WonderlandUserList.INSTANCE.getSessionIDFromName(name.toString());
            IDs.add(bi);
        }
        
        client.sendAudioRequestMessage(IDs);
        
    }
    
    private void handleGetScreenSnapshotButtonPressed(ActionEvent e) {
        logger.warning("Take screenshot button pressed!");
        InstructorClientConnection client = InstructorClientConnection.getInstance();
        
        Set<BigInteger> IDs = new LinkedHashSet<BigInteger>();
        for(Object name: view.getSelectedEntries()) {
            BigInteger bi = WonderlandUserList.INSTANCE.getSessionIDFromName(name.toString());
            IDs.add(bi);
        }
        
        client.sendScreenShotRequestMessage(IDs);
    }
    
    private void handleReconnectSoftphoneButtonPressed(ActionEvent e) {
        logger.warning("Reconnect Softphone button pressed!");
        InstructorClientConnection client = InstructorClientConnection.getInstance();
        
        Set<BigInteger> IDs = new LinkedHashSet<BigInteger>();
        for(Object name: view.getSelectedEntries()) {
            
            BigInteger bi = WonderlandUserList.INSTANCE.getSessionIDFromName(name.toString());
            IDs.add(bi);
        }
        
        client.sendReconnectSoftphoneMessage(IDs);
        
    }
    
    @Override
    protected void addListeners() {
        super.addListeners();
       
        InstructorToolsView itView = (InstructorToolsView)view;
        
        itView.addPullToMeButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handlePullToMeButtonPressed(ae);
            }
        });
        
        itView.addSecureClientButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleSecureClientButtonPressed(ae);
            }
        });
        
        itView.addAdjustStudentAudioButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleAdjustStudentAudioButtonPressed(ae);
            }
        });
        
        itView.addGetScreenshotButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleGetScreenSnapshotButtonPressed(ae);
            }
        });
        
        itView.addReconnectSoftphoneButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleReconnectSoftphoneButtonPressed(ae);
            }
        });
        
         
    }
}
