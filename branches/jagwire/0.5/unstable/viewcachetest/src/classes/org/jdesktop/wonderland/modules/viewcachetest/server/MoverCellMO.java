/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.viewcachetest.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.Random;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class MoverCellMO extends ViewCellMO {
    private static final Logger LOGGER =
            Logger.getLogger(MoverCellMO.class.getName());
    private static final Random RANDOM = new Random();

    private final ManagedReference<MoverViewCellCacheMO> cacheRef;
    private final Vector3f offset;

    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> movableRef;

    private CellTransform target;
    private boolean taskScheduled = false;

    public MoverCellMO(CellTransform xform) {
        super (new BoundingBox(), xform);

        this.offset = xform.getTranslation(null);

        MoverViewCellCacheMO cache = new MoverViewCellCacheMO(this);
        cacheRef = AppContext.getDataManager().createReference(cache);

        addTransformChangeListener(new TransformChangeListener());
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.viewcachetest.client.MoverCell";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        if (!live) {
            cacheRef.get().logout();
        }
    }

    @Override
    public MoverViewCellCacheMO getCellCache() {
        return cacheRef.get();
    }
    
    public void moveTo(CellTransform target) {
        this.target = target;
        
        if (!taskScheduled) {
            LOGGER.fine("Scheduling move for " + getCellID());

            int time = RANDOM.nextInt(1000);
            AppContext.getTaskManager().scheduleTask(new MoveTask(this), time);
        }
        
        taskScheduled = true;
    }
    
    private void doMove() {
        LOGGER.fine("Cell " + getCellID() + " moving to " + target);
        
        target.setTranslation(target.getTranslation(null).add(offset));
        movableRef.get().moveRequest(null, target);
    
        taskScheduled = false;
    }

    private static class MoveTask implements Task, Serializable {
        private final ManagedReference<MoverCellMO> cellRef;

        public MoveTask(MoverCellMO cell) {
            cellRef = AppContext.getDataManager().createReference(cell);
        }

        public void run() throws Exception {
            cellRef.get().doMove();
        }
    }

    private static class TransformChangeListener implements TransformChangeListenerSrv {

        public void transformChanged(ManagedReference<CellMO> cellRef,
                                     CellTransform localTransform,
                                     CellTransform worldTransform)
        {
            MoverCellMO cellMO = (MoverCellMO) cellRef.get();
            cellMO.getCellCache().login();
            cellMO.removeTransformChangeListener(this);
        }
    }
}
