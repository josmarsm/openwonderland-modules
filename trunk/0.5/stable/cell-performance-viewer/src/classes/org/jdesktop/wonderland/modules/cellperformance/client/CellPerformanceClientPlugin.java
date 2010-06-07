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
package org.jdesktop.wonderland.modules.cellperformance.client;

import java.util.Collection;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellStatistics.CellStat;
import org.jdesktop.wonderland.client.cell.CellStatistics.CellStatisticsSPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin that implements CellStatisticsSPI
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class CellPerformanceClientPlugin 
        implements ClientPlugin, SessionLifecycleListener, CellStatisticsSPI
{
    ServerSessionManager sessionManager;

    public void initialize(ServerSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        // make sure mtgame stats are on
        System.setProperty("mtgame.entityStats", "true");

        sessionManager.addLifecycleListener(this);
    }

    public void cleanup() {
        if (sessionManager != null) {
            sessionManager.removeLifecycleListener(this);
        }
    }

    public void add(Cell cell, CellStat stat) {
        CellPerformanceComponent perfComp =
                cell.getComponent(CellPerformanceComponent.class);
        perfComp.add(stat);
    }

    public CellStat get(Cell cell, String id) {
        CellPerformanceComponent perfComp =
                cell.getComponent(CellPerformanceComponent.class);
        return perfComp.get(id);
    }

    public Collection<CellStat> getAll(Cell cell) {
        CellPerformanceComponent perfComp =
                cell.getComponent(CellPerformanceComponent.class);
        return perfComp.getAll();
    }

    public CellStat remove(Cell cell, String id) {
        CellPerformanceComponent perfComp =
                cell.getComponent(CellPerformanceComponent.class);
        return perfComp.remove(id);
    }

    public void sessionCreated(WonderlandSession session) {
        CellCache cache = ClientContext.getCellCache(session);
        cache.getStatistics().setProvider(this);
    }

    public void primarySession(WonderlandSession session) {
        // ignore
    }
}
