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
package org.jdesktop.wonderland.modules.cmu.client.ui.hud;

import javax.swing.JPanel;

/**
 * Abstract parent class for CMU HUD panels.
 * @author kevin
 */
public abstract class CMUPanel extends JPanel {

    /**
     * Convenience method to convert units from one scale to another.
     * @param value The current value in unit type 1
     * @param minValue A minimum value in unit type 1
     * @param maxValue A maximum value in unit type 1
     * @param minResult The corresponding minimum value in unit type 2
     * @param maxResult The corresponding maximum value in unit type 2
     * @return The corresponding current value in unit type 2
     */
    protected static final float linearScale(float value, float minValue,
            float maxValue, float minResult, float maxResult) {
        return minResult + (((value - minValue) / (maxValue - minValue)) * (maxResult - minResult));
    }
}
