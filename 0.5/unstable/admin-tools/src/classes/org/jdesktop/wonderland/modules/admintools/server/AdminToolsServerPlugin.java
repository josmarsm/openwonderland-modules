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
package org.jdesktop.wonderland.modules.admintools.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.modules.security.server.service.GroupMemberResource;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
import org.jdesktop.wonderland.server.security.ActionMap;
import org.jdesktop.wonderland.server.security.ResourceMap;
import org.jdesktop.wonderland.server.security.SecureTask;
import org.jdesktop.wonderland.server.security.SecurityManager;
import org.jdesktop.wonderland.server.spatial.CellMOListener;
import org.jdesktop.wonderland.server.spatial.UniverseManager;

/**
 * Server plugin for admin tools
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class AdminToolsServerPlugin 
        implements ServerPlugin, CellMOListener, Serializable
{
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsServerPlugin.class.getName());

    public void initialize() {
        AppContext.getManager(UniverseManager.class).addCellListener(this);
    }

    public void cellAdded(CellMO cell) {
        if (cell instanceof AvatarCellMO) {
            ResourceMap rm = new ResourceMap();
            GroupMemberResource gmr = new GroupMemberResource("admin");
            rm.put(gmr.getId(), new ActionMap(gmr, new ViewAction()));

            SecurityManager sm = AppContext.getManager(SecurityManager.class);
            sm.doSecure(rm, new AddAdminToolsTask(cell));
        }
    }

    public void cellRemoved(CellMO cell) {
    }

    private static class AddAdminToolsTask implements SecureTask, Serializable {
        private final ManagedReference<CellMO> cellRef;

        public AddAdminToolsTask(CellMO cell) {
            cellRef = AppContext.getDataManager().createReference(cell);
        }

        public void run(ResourceMap granted) {
            ActionMap am = granted.values().iterator().next();
            if (am.isEmpty()) {
                return;
            }

            CellMO cell = cellRef.get();
            cell.addComponent(new AdminToolsComponentMO(cell));

            LOGGER.warning("Added admin tools to " + cell);
        }
    }
}
