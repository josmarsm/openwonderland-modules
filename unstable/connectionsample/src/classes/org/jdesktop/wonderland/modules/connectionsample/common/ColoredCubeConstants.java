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
package org.jdesktop.wonderland.modules.connectionsample.common;

/**
 * Utility class to share constants between the ColoredCubeCell and the
 * ColoredCubeCellMO.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ColoredCubeConstants {
    /** the name of the shared map to use */
    public static final String MAP_NAME =
            ColoredCubeConstants.class.getPackage().getName();

    /** the key to use for storing the color in that map */
    public static final String COLOR_KEY = "color";
}
