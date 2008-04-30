/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.tightvncmodule.server.cell;

import com.sun.sgs.app.ClientSession;
import java.util.HashSet;
import java.util.Set;
import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellSetup;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;

/**
 * @author nsimpson
 */
public class TightVNCModuleCellGLO extends SharedApp2DImageCellGLO
        implements BeanSetupGLO, CellMessageListener {
    
    private BasicCellGLOSetup<TightVNCModuleCellSetup> setup;
    
    public TightVNCModuleCellGLO() {
        this(null, null, null, null);
    }
    
    public TightVNCModuleCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, TightVNCModuleCellGLO.class.getName());
    }
    
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCModuleCell";
    }
    
    public TightVNCModuleCellSetup getSetupData() {
        return setup.getCellSetup();
    }
    
    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<TightVNCModuleCellSetup>) setupData;
        
        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());
        
        Matrix4d o = new Matrix4d(rot, origin, setup.getScale() );
        setOrigin(o);
        
        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float)setup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }
    }
    
    /**
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        setupCell(setupData);
    }
    
    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        return new BasicCellGLOSetup<TightVNCModuleCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }
    
    
    public void receivedMessage(ClientSession client, CellMessage message) {
        TightVNCModuleCellMessage ntcm = (TightVNCModuleCellMessage) message;
        
        // send a message to all clients except the sender to notify of
        // the updated selection
        TightVNCModuleMessage msg = new TightVNCModuleMessage();
        
        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());
    }
}
