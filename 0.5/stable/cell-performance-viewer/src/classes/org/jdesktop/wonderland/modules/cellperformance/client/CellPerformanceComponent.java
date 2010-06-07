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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellStatistics.CellStat;
import org.jdesktop.wonderland.common.ExperimentalAPI;

@ExperimentalAPI
public class CellPerformanceComponent extends CellComponent {
    private static final Logger logger =
            Logger.getLogger(CellPerformanceComponent.class.getName());

    private final Map<String, CellStat> stats =
            new LinkedHashMap<String, CellStat>();

    public CellPerformanceComponent(Cell cell) {
        super(cell);
    }

    public void add(CellStat stat) {
        stats.put(stat.getId(), stat);
    }

    public CellStat get(String id) {
        return stats.get(id);
    }

    public Collection<CellStat> getAll() {
        return new LinkedHashSet<CellStat>(stats.values());
    }

    public CellStat remove(String id) {
        return stats.remove(id);
    }
}
