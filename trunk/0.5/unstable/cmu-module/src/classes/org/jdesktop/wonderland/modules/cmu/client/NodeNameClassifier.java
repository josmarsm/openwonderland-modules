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
package org.jdesktop.wonderland.modules.cmu.client;

/**
 * Collections that can be used to classify CMU nodes by their name.
 * @author kevin
 */
public final class NodeNameClassifier {

    private static final String[] groundPlaneNames = {
        "seaSurface.m_sgVisual",
        "grassyGround.m_sgVisual",
        "moonSurface.m_sgVisual",
        "sandyGround.m_sgVisual",
        "Ground.m_sgVisual",    // Suffixes
        "Surface.m_sgVisual"};
    /**
     * Should never be instantiated.
     */
    private NodeNameClassifier() {
    }

    /**
     * Decide whether the given name is a ground plane name.
     * @param name The name to check
     * @return True if the name is a ground plane name
     */
    public static boolean isGroundPlaneName(String name) {
        //TODO: do this more generally

        for (String planeName : groundPlaneNames) {
            if (name.endsWith(planeName)) {
                return true;
            }
        }
        return false;
    }
}
