/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.navigateto.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Best view client state
 *
 * @author nilang shah
 * @author Abhishek Upadhyay
 */
public class NavigateToClientState extends CellComponentClientState {

    public NavigateToClientState() {
    }
    private int trigger = 0;
    private float offsetX = 1f;
    private float offsetY = 2f;
    private float offsetZ = 1f;
    private float lookDirX = 999;
    private float lookDirY;
    private float lookDirZ;
    private boolean bestView = true;

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void setLookDirX(float lookDirX) {
        this.lookDirX = lookDirX;
    }

    public float getLookDirX() {
        return lookDirX;
    }

    public void setLookDirY(float lookDirY) {
        this.lookDirY = lookDirY;
    }

    public float getLookDirY() {
        return lookDirY;
    }

    public void setLookDirZ(float lookDirZ) {
        this.lookDirZ = lookDirZ;
    }

    public float getLookDirZ() {
        return lookDirZ;
    }

    public void setBestView(boolean bestView) {
        this.bestView = bestView;
    }

    public boolean getBestView() {
        return bestView;
    }
}
