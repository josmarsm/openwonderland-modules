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

package org.jdesktop.wonderland.modules.layout.common;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;
import org.jdesktop.wonderland.modules.layout.api.common.annotation.LayoutFactory;
import org.jdesktop.wonderland.modules.layout.api.common.spi.LayoutFactorySPI;

/**
 * A factory for the table layout.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@LayoutFactory
public class TableLayoutFactory implements LayoutFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/layout/common/resources/Bundle");
    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        // Display name for the table layout factory, from I18N resource
        // bundle
        return BUNDLE.getString("Table_Layout_Display_Name");
    }

    /**
     * {@inheritDoc}
     */
    public LayoutConfig getDefaultLayoutConfig(Properties properties) {
        // XXX
        // Set the initial size to something reasonable but hard-coded.
        // XXX
        return new TableLayoutConfig(new BoundingBox(new Vector3f(), 4, 4, 4),
                4, 4);
    }
}
