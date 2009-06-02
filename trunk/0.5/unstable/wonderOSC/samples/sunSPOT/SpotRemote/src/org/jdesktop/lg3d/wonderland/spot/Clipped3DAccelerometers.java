/**
 * Project Looking Glass
 *
 * $RCSfile: Clipped3DAccelerometers.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2 $
 * $Date: 2008/08/14 16:19:16 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.spot;

import java.io.IOException;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.sensorboard.peripheral.LIS3L02AQAccelerometer;

/**
 * @author: David Mercier <david.mercier@sun.com>
 * @author: Bernard Horan <bernard.horan@sun.com>
 * 
 * Filter the results of the accelerometer so that they are in the
 * range 0.02 <= x <= 1.0.
 * 
 */
public class Clipped3DAccelerometers {

    private IAccelerometer3D device = null;
    private final static double ABSOLUTE_ERROR = 0.02;

    public Clipped3DAccelerometers(EDemoBoard eDemo) {
        this.device = eDemo.getAccelerometer();
        try {
            System.out.println("Resetting offsets for acceleromater");
            device.setRestOffsets();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ((LIS3L02AQAccelerometer) this.device).setScale(LIS3L02AQAccelerometer.SCALE_2G);
    }

    public double getAccelX() {
        double value = 0;

        try {
            value = device.getRelativeAccelX();
        } catch (IOException e) {
            return 0;
        }
        if (Math.abs(value) < ABSOLUTE_ERROR) {
            return 0;
        }


        if (value > 0) {
            return Math.min(value, 1.0);
        } else {
            return Math.max(value, -1.0);
        }
    }

    public double getAccelY() {
        double value = 0;
        try {
            value = device.getRelativeAccelY();
        } catch (IOException e) {
            return 0;
        }
        if (Math.abs(value) < ABSOLUTE_ERROR) {
            return 0;
        }

        if (value > 0) {
            return Math.min(value, 1.0);
        } else {
            return Math.max(value, -1.0);
        }
    }

    public double getAccelZ() {
        double value = 0;
        try {
            value = device.getRelativeAccelZ();
        } catch (IOException e) {
            return 0;
        }
        if (Math.abs(value) < ABSOLUTE_ERROR) {
            return 0;
        }
        if (value > 0) {
            return Math.min(value, 1.0);
        } else {
            return Math.max(value, -1.0);
        }
    }
}
