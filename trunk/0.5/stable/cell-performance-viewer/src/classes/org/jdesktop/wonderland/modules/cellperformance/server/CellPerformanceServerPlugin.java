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
package org.jdesktop.wonderland.modules.cellperformance.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

/**
 * Plugin to ensure the component is installed on all cells.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class CellPerformanceServerPlugin implements ServerPlugin {
    public void initialize() {
        CellManagerMO.getCellManager().registerCellComponent(CellMO.class,
                                                             CellPerformanceComponentMO.class);
    }
}
