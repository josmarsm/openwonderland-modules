/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.bestview.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.imageviewer.server.cell.ImageViewerCellMO;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

/**
 * Server plugin to automatically add best view to all
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@Plugin
public class BestViewServerPlugin implements ServerPlugin {
    public void initialize() {
        CellManagerMO cm = CellManagerMO.getCellManager();
        cm.registerCellComponent(App2DCellMO.class, BestViewComponentMO.class);
        cm.registerCellComponent(ImageViewerCellMO.class, BestViewComponentMO.class);
    }
}
