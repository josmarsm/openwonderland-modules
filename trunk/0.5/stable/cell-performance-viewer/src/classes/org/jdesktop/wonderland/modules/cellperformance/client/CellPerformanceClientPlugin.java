/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
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

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.client.assetmgr.AssetManager.AssetStat;
import org.jdesktop.wonderland.client.assetmgr.AssetManager.AssetStatisticsSPI;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellStatistics.CellStat;
import org.jdesktop.wonderland.client.cell.CellStatistics.CellStatisticsSPI;
import org.jdesktop.wonderland.client.cell.CellStatistics.TimeCellStat;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * Plugin that implements CellStatisticsSPI
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class CellPerformanceClientPlugin 
        implements ClientPlugin, SessionLifecycleListener, CellStatisticsSPI,
                   AssetStatisticsSPI, CellStatusChangeListener
{
    private static final Logger LOGGER =
            Logger.getLogger(CellPerformanceClientPlugin.class.getName());
    
    private ServerSessionManager sessionManager;
    private final Map<AssetURI, WeakReference<Cell>> cellMap =
            new LinkedHashMap<AssetURI, WeakReference<Cell>>();
    
    public void initialize(ServerSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        // make sure mtgame stats are on
        System.setProperty("mtgame.entityStats", "true");

        sessionManager.addLifecycleListener(this);
        AssetManager.getAssetManager().setStatsProvider(this);
        CellManager.getCellManager().addCellStatusChangeListener(this);
    }

    public void cleanup() {
        if (sessionManager != null) {
            sessionManager.removeLifecycleListener(this);
        }
        
        if (AssetManager.getAssetManager().getStatsProvider() == this) {
            AssetManager.getAssetManager().setStatsProvider(null);
        }
        
        CellManager.getCellManager().removeCellStatusChangeListener(this);
        
        cellMap.clear();
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

    public void assetStatistic(AssetURI uri, AssetStat stat, long time) {
        Cell cell = getCell(uri, stat);
        if (cell != null) {
            // if there is a cell, associate the stat with the cell
            String id = uri + " " + stat.name();
            
            // is there already a stat for this id?
            TimeCountCellStat tcs = (TimeCountCellStat) get(cell, id);
            if (tcs == null) {
                tcs = new TimeCountCellStat(id);
                tcs.setValue(time);
            } else {
                tcs.changeValue(time);
            }
            
            add(cell, tcs);
        } else {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "{0} {1}: {2} ms.", 
                           new Object[]{uri, stat.name(), time});
            }
        }
    }
    
    private Cell getCell(AssetURI uri, AssetStat stat) {
        Cell out = null;
        WeakReference<Cell> cellRef = null;
        
        switch (stat) {
            case SUBMIT:
                // on a submit, find the current cell (if any) from the cell
                // cache, and record it in the map for later user
                out = CellCacheBasicImpl.getCurrentActiveCell();
                if (out != null) {
                    cellMap.put(uri, new WeakReference<Cell>(out));
                }
                break;
                
            case OPEN_STREAM:
                // on open stream, see if there is an entry in the map
                cellRef = cellMap.get(uri);
                if (cellRef != null) {
                    out = cellRef.get();
                }
                break;
                
            case GET_FROM_CACHE:
            case GET_FROM_SERVER:
                // this is the final call for this item, so remove it from
                // the map
                cellRef = cellMap.remove(uri);
                if (cellRef != null) {
                    out = cellRef.get();
                }
                break;
        }
        
        return out;
    }

    public void cellStatusChanged(Cell cell, CellStatus status) {
        // print statistics when the cell becomes active
        if (LOGGER.isLoggable(Level.INFO) && status == CellStatus.ACTIVE) {
            StringBuilder out = new StringBuilder("Stats for cell ")
                                    .append(cell.getCellID())
                                    .append(" (")
                                    .append(cell.getName())
                                    .append(") ")
                                    .append(cell.getClass().getSimpleName())
                                    .append("\n");
            
            for (CellStat stat : getAll(cell)) {
                out.append("    ")
                   .append(stat.getDescription())
                   .append(": ")
                   .append(stat.getValue())
                   .append("\n");
            }
            
            out.append("End cell ").append(cell.getCellID());
            LOGGER.info(out.toString());
        }
    }
    
    private static class TimeCountCellStat extends TimeCellStat {
        int count = 1;
        
        public TimeCountCellStat(String id) {
            super (id);
        }

        @Override
        public synchronized Long changeValue(Long amount) {
            count++;
            return super.changeValue(amount);
        }
        
        
        @Override
        public String getValue() {
            return super.getValue() + " (" + count + "x)";
        }
    }
}
