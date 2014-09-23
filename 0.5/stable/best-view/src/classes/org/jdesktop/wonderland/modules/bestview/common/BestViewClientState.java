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
package org.jdesktop.wonderland.modules.bestview.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Best view client state
 * 
 * @author Abhishek Upadhyay
 */
public class BestViewClientState extends CellComponentClientState {

    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private float lookDirX;
    private float lookDirY;
    private float lookDirZ;
    private float lookDirW;
    private float zoom;
    private int trigger = 1;
    private float oldObjPosX;
    private float oldObjPosY = 999;
    private float oldObjPosZ;

    public BestViewClientState() {
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

    public void setLookDirW(float lookDirW) {
        this.lookDirW = lookDirW;
    }

    public float getLookDirW() {
        return lookDirW;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setOldObjPosX(float oldObjPosX) {
        this.oldObjPosX = oldObjPosX;
    }

    public float getOldObjPosX() {
        return oldObjPosX;
    }

    public void setOldObjPosY(float oldObjPosY) {
        this.oldObjPosY = oldObjPosY;
    }

    public float getOldObjPosY() {
        return oldObjPosY;
    }

    public void setOldObjPosZ(float oldObjPosZ) {
        this.oldObjPosZ = oldObjPosZ;
    }

    public float getOldObjPosZ() {
        return oldObjPosZ;
    }
}
