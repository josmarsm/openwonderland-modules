/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author ryan
 */
public class TimeRecord {

    private CellTransform transform;
    private long sendTime;

    public TimeRecord(CellTransform transform, long sendTime) {
        this.transform = transform;
        this.sendTime = sendTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public CellTransform getTransform() {
        return transform;
    }
}
