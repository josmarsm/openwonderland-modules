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
package org.jdesktop.wonderland.modules.layout.api.common;

import com.jme.bounding.BoundingVolume;
import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Configuration for a layout.  The configuration data is specific to
 * the particular layout, but this base class defines the properties that
 * are common to all layouts.
 * <p>
 * LayoutConfig objects should be serializable both using Java serialization
 * and using XML.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public abstract class LayoutConfig implements Serializable {
    /**
     * Get the layout class this configuration is for.
     * @return the layout class associated with this configuration.
     */
    public abstract <T extends Layout> Class<T> getLayoutClass();


    /**
     * Get the bounds of this layout. The bounds should encompass the area
     * this layout has to work with. Most layouts will attempt to layout the
     * given objects within these bounds.
     * @return the bounds of this layout.
     */
    public abstract BoundingVolume getBounds();
}
