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
package org.jdesktop.wonderland.modules.layout.api.common.spi;

import java.util.Properties;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;

/**
 * A layout factory helps generate a new layout. Each layout should have a
 * factory. The factory returns a display name to be used in some graphical
 * display. It also returns a layout config that can be used to create the
 * layout itself.
 * <p>
 * Each layout factory should be annotated with @LayoutFactory.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public interface LayoutFactorySPI {
    /**
     * Returns the display name that describes the layout
     * @return the layout display name
     */
    public String getDisplayName();

    /**
     * Returns a default configuration for the layout. This configuration can
     * be used to create a layout.
     * @param properties a set of properties to use during creation
     * @return a new, default layout configuration
     */
    public LayoutConfig getDefaultLayoutConfig(Properties properties);
}
