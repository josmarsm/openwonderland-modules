/**
 * Project Looking Glass
 *
 * $RCSfile: SpotForwarder.java,v $
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
 * $Date: 2008/08/21 16:11:24 $
 * $State: Exp $
 */

package org.jdesktop.lg3d.wonderland.spot;

import com.sun.spot.io.j2me.radiogram.*;

import java.io.*;


import java.util.logging.Logger;
import javax.microedition.io.*;
import com.illposed.osc.*;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author sgalan <sgalan@inv.it.uc3m.es> Universidad Carlos III Madrid.
 * Based on previus work from
 * @author: David Mercier <david.mercier@sun.com>
 * @author: Bernard Horan <bernard.horan@sun.com>
 * 
 * Use the Sun SPOT to control a Wonderland Client using OSC protocol messages
 * 
 * This class receives the data from the SPOT and forwards to the Wonderland client.
 * 

 * 
 */
public class SpotForwarder {
    private double tiltX,tiltY,tiltZ = 0.0;


    private int alertChange = 0;
    private static String BASESTATION_HOST = "localhost";
    private static int BASESTATION_PORT = 3010;
    private final static Logger LOGGER = Logger.getLogger(SpotForwarder.class.getName());
    private OSCPortOut sender;
    private final BlockingQueue<Double> queue;



    /**
     * Print out our radio address.
     */
    public SpotForwarder() {
        queue=new LinkedBlockingQueue<Double>();
        Thread receiverThread = createReceiverThread();
        receiverThread.start();
        connect();
        startServiceThread();
    }

    /**
     * Start up the host application.
     * The arguments are given by the property 'main.args'
     * iin the build.properties file.
     *
     * @param args any command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            BASESTATION_HOST = args[0];
            LOGGER.info("Read basestation host from args: " + BASESTATION_HOST);
        }
        if (args.length > 1) {
            BASESTATION_PORT = Integer.parseInt(args[1]);
            LOGGER.info("Read basestation port from args: " + BASESTATION_PORT);
        }
        SpotForwarder app = new SpotForwarder();
    }
    
    private Thread createReceiverThread() {
        return new Thread() {
             
            public void run() {
               
                RadiogramConnection dgConnection = null;
                Datagram dg = null;
                try {
                    dgConnection = (RadiogramConnection) Connector.open("radiogram://:35");
                    dgConnection.setTimeout(500);
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                while (true) {
                    consume(dg, dgConnection);
                }
            }
        };
    }
    
    private void consume(Datagram dg, RadiogramConnection dgConnection) {
        try {
            dg.reset();
            dgConnection.receive(dg);
            tiltX = dg.readDouble();
            tiltY = dg.readDouble();
            tiltZ = dg.readDouble();
            try{
                queue.put(tiltX);
                queue.put(tiltY);
                queue.put(tiltZ);
            }
            catch(InterruptedException e){e.printStackTrace();}
            alertChange = dg.readInt();
            LOGGER.info("Received " + tiltX + " " + tiltY + " " +tiltZ +" " + alertChange);
        } catch (IOException e) {            

            tiltX = 0.0;
            tiltY = 0.0;
            tiltZ= 0.0;
            alertChange = 0;
        }
    }
    
    private void connect(){
        InetAddress address;
        try {
            address = InetAddress.getByName( "localhost" );
        
        sender = new OSCPortOut(address, 3010);
        }catch(Exception e){System.out.println("error conectando");};
    }
    
    
    public void startServiceThread() {
        new Thread() {
            public void run() {
                LOGGER.info("I'm service and I start");
                while (true) {
                    produce();
                }
            }
        }.start();
    }
    
    private synchronized void produce() {

 
        Object args[] = new Object[3];
        try{
            args[0] = new Float(queue.take());
            args[1] = new Float(queue.take());
            args[2] = new Float(queue.take());
            OSCMessage msg = new OSCMessage("/raw/xyz", args);
             try {
                sender.send(msg);
             } catch (Exception e) {
               System.out.println("Couldn't send");
             }
        }
        catch (Exception e) {
           e.printStackTrace();
        }

        LOGGER.info("Sending " + args[0].toString()+ " " + args[1].toString() + " " +args[2].toString()  +" " + alertChange);
        notify();
    }

}
