/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.common.layout;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.layout.api.common.Layout;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;

/**
 * No-op layout configuration for timeline layout
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineLayoutConfig extends LayoutConfig {
    @Override
    public <T extends Layout> Class<T> getLayoutClass() {
        return (Class<T>) TimelineLayout.class;
    }

    @Override
    public BoundingVolume getBounds() {
        return new BoundingBox(new Vector3f(0f, 0f, 0f), 50f, 50f, 50f);
    }
}
