/**
 * Project Looking Glass
 * 
 * $RCSfile: SampleModuleCellGLO.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2008/02/06 20:20:44 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.samplemodule.server.cell;

import com.sun.sgs.app.ClientSession;
import java.util.HashSet;
import java.util.Set;
import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleCellMessage;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleCellSetup;
import org.jdesktop.lg3d.wonderland.samplemodule.common.SampleModuleMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;

/**
 * A server cell that demonstrates simple networking
 * @author jkaplan
 */
public class SampleModuleCellGLO extends StationaryCellGLO
        implements BeanSetupGLO, CellMessageListener {
    
    // The setup object contains the current state of the application,
    // which is the currently selected cube. It's updated every time a 
    // client makes a change to the document so that when new clients join, 
    // they receive the current state.
    private BasicCellGLOSetup<SampleModuleCellSetup> setup;

    public SampleModuleCellGLO() {
        this(null, null);
    }

    public SampleModuleCellGLO(Bounds bounds, Matrix4d center) {
        super(bounds, center);
    }

    public String getSelection() {
        return setup.getCellSetup().getSelectionID();
    }

    public void setSelection(String selection) {
        setup.getCellSetup().setSelectionID(selection);
    }

    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }

    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.samplemodule.client.cell.SampleModuleCell";
    }

    public SampleModuleCellSetup getSetupData() {
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
        setup = (BasicCellGLOSetup<SampleModuleCellSetup>) setupData;

        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());

        Matrix4d o = new Matrix4d(rot, origin, setup.getScale());
        setOrigin(o);

        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float) setup.getBoundsRadius()));
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
        return new BasicCellGLOSetup<SampleModuleCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    public void receivedMessage(ClientSession client, CellMessage message) {
        SampleModuleCellMessage ntcm = (SampleModuleCellMessage) message;

        // update setup data with the latest shared state
        setSelection(ntcm.getSelectionID());
        
        // send a message to all clients except the sender to notify of 
        // the updated selection
        SampleModuleMessage msg = new SampleModuleMessage(ntcm.getSelectionID(),
                SampleModuleMessage.Action.SELECT);

        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());
    }
}
