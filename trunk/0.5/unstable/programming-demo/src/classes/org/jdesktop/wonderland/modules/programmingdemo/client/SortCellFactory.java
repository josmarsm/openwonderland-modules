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
package org.jdesktop.wonderland.modules.programmingdemo.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellServerState;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellServerState;

/**
 * Cell factory for sorting demo
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@CellFactory
public class SortCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        SortCellServerState state = new SortCellServerState();

        // for a new cell, also add a corresponding code cell
        state.setCodeState(new CodeCellServerState());

        return (T) state;
    }

    public String getDisplayName() {
        return "Sorting Demo";
    }

    public Image getPreviewImage() {
        URL url = SortCellFactory.class.getResource("resources/Programming-demo-icon.png");
        return Toolkit.getDefaultToolkit().getImage(url);
   }
}
