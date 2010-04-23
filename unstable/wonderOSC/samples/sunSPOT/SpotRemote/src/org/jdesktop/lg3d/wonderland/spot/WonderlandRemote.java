/**
 * Project Looking Glass
 *
 * $RCSfile: WonderlandRemote.java,v $
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
 * $Revision: 1.4 $
 * $Date: 2008/08/21 16:10:02 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.spot;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.midlet.MIDletStateChangeException;


import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ISwitchListener;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;

/**
 * @author: David Mercier <david.mercier@sun.com>
 * @author: Bernard Horan <bernard.horan@sun.com>
 * 
 * Use the Sun SPOT to control a Wonderland Client. 
 * This also relies on the SpotForwarder host application to
 * forward the data to the wonderland client.
 * 
 * There is one thread (startSenderThread) that sends data on a particular 
 * channel.
 * 
 */
public class WonderlandRemote extends MIDlet {

    private Clipped3DAccelerometers accels = null;
    private ISwitch sw1, sw2;
    private ILightSensor light = null;
    private BlinkenLights led = null;
    private static final LEDColor ON_COLOR = LEDColor.GREEN;
    private static final LEDColor OFF_COLOR = LEDColor.RED;
    private static int LIGHT_THRESHOLD = 30;
    private int alertChange = 0;
    private IAccelerometer3D acc = null;
    protected void startApp() throws MIDletStateChangeException {

        System.out.println("Starting WonderlandRemote.\nPlease place on a horizontal surface.");

        EDemoBoard eDemo = EDemoBoard.getInstance();
        
        led = new BlinkenLights(eDemo);
        led.startPsilon();
        
        sw1 = eDemo.getSwitches()[EDemoBoard.SW1];
        sw2 = eDemo.getSwitches()[EDemoBoard.SW2];
        light = eDemo.getLightSensor();
        calibrateLightThreshold();
        addSwitchListeners();                   
        
        System.out.println("Turning LEDS on");
        led.setColor(ON_COLOR);
       
        accels = new Clipped3DAccelerometers(eDemo);
        acc = eDemo.getAccelerometer();
        startSenderThread();
    }

    
    /**
     * The sender thread sends the data each 20ms
     */
    synchronized public void startSenderThread() {
        new Thread() {

            public void run() {
                // We create a DatagramConnection 
                DatagramConnection dgConnection = null;
                Datagram dg = null;
                try {
                    // The Connection is a broadcast so we specify it in the creation string
                    dgConnection = (DatagramConnection) Connector.open("radiogram://broadcast:35");
                    // Then, we ask for a datagram with the maximum size allowed
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                    System.out.println("Opened datagram connection");
                } catch (IOException ex) {
                    System.out.println("Could not open radiogram broadcast connection");
                    ex.printStackTrace();
                    return;
                }

                while (true) {
                    try {
                        dg.reset();
                        if (!inUse()) {
                            led.setColor(OFF_COLOR);
                        } else {
                            led.setColor(ON_COLOR);



                            double x = acc.getAccelX();
                            double y = acc.getAccelY();
                            double z = acc.getAccelZ();


                            double TiltX = acc.getTiltX() ;
                            double TiltY = acc.getTiltY();
                            double TiltZ = acc.getTiltZ();

                            dg.writeDouble(TiltX);
                            dg.writeDouble(TiltY);
                            dg.writeDouble(TiltZ);
  
                            dg.writeInt(alertChange);
                            dgConnection.send(dg);

                            alertChange = 0;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    Utils.sleep(10);
                }
            }
        }.start();
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }


    private void calibrateLightThreshold() {
        System.out.println("Calibrating light threshold.");
        System.out.println("Place your fingers on the switches and press both of them at the same time");
        try {
            int oldLightValue = light.getAverageValue();
            //System.out.println("Light value beforehand: " + oldLightValue);
            while (sw1.isOpen() || sw2.isOpen()) {};
            int lightValue = light.getAverageValue();
            //System.out.println("New light value: " + lightValue);
            LIGHT_THRESHOLD = lightValue + ((oldLightValue - lightValue)/2);
            //System.out.println("New light threshold: " + LIGHT_THRESHOLD);
        } catch (IOException ex) {
            System.err.println("Failed to get light value");
            ex.printStackTrace();
        }
        
    }
    
    private boolean inUse() {
        try {
            //System.out.println("Light value: " + light.getValue());
            return light.getAverageValue() < LIGHT_THRESHOLD;
        } catch (IOException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    private void addSwitchListeners() {
        sw1.addISwitchListener(new ISwitchListener() {

            public void switchPressed(ISwitch arg0) {
            }

            public void switchReleased(ISwitch arg0) {
                alertChange = -1;
            }
        });
        sw2.addISwitchListener(new ISwitchListener() {

            public void switchPressed(ISwitch arg0) {
            }

            public void switchReleased(ISwitch arg0) {
                alertChange = 1;
            }
        });     
    }
    
}