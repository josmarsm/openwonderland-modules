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

import java.util.Collection;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
import org.jdesktop.wonderland.server.spatial.UniverseManagerFactory;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class MoverViewCellCacheMO extends ViewCellCacheMO {
    public MoverViewCellCacheMO(MoverCellMO view) {
        super (view);
    }

    public void login() {
        UniverseManagerFactory.getUniverseManager().viewLogin(getViewCell());
    }

    public void logout() {
        UniverseManagerFactory.getUniverseManager().viewLogout(getViewCell());
    }

    @Override
    public boolean isLoaded(CellID cellID) {
        return super.isLoaded(cellID);
    }

    @Override
    protected void sendLoadMessages(Collection<CellDescription> cells) {
        for (CellDescription cell : cells) {
            setLoaded(cell.getCellID());
        }
    }

    @Override
    protected void sendUnloadMessages(Collection<CellDescription> unload) {
        for (CellDescription cell : unload) {
            setUnloaded(cell.getCellID());
        }
    }
}
