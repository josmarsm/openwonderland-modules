/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.generic.client;

import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.generic.common.GenericCellServerState;

/**
 * A Cell Factory for the "generic" Cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class GenericCellFactory implements CellFactorySPI {

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[] {};
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(Properties properties) {
        GenericCellServerState state = new GenericCellServerState();
        Class clazz = getClientCellClass();
        state.setClientCellClassName(clazz.getName());

        return (T)state;
    }

    /**
     * Over-ridden by the specific implementation to returns the Class of the
     * client-side Cell.
     *
     * @return The client-side Cell Class
     */
    public abstract Class getClientCellClass();
}
