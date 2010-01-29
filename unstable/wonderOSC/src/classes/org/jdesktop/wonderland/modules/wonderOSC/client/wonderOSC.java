/* This code was developed with funding from the project "España Virtual"
*
* The contents of this file are subject to the GNU General Public
* License, Version 2 (the "License"); you may not use this file
* except in compliance with the License. A copy of the License is
* available at http://www.opensource.org/licenses/gpl-license.php.
*
* "España Virtual es un proyecto de I+D, subvencionado por el CDTI dentro del
* programa Ingenio 2010, orientado a la definición de la arquitectura,
* protocolos y estándares del futuro Internet 3D, con un foco especial en lo
* relativo a visualización 3D, inmersión en mundos virtuales, interacción
* entre usuarios y a la introducción de aspectos semánticos, sin dejar de lado
* el estudio y maduración de las tecnologías para el procesamiento masivo y
* almacenamiento de datos geográficos.
*
* Con una duración de cuatro años, el proyecto está liderado por DEIMOS Space
* y cuenta con la participación del Centro Nacional de Información Geográfica
* (IGN/CNIG), Grid Systems, Indra Espacio, GeoVirtual, Androme Ibérica,
* GeoSpatiumLab, DNX y una decena de prestigiosos centros de investigación y
* universidades nacionales."
*/

package org.jdesktop.wonderland.modules.wonderOSC.client;



import com.illposed.osc.*;
import imi.character.avatar. AvatarContext.TriggerNames;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.wonderland.modules.wonderOSC.client.*;

/**
 * @author Sergio Galan sgalan@inv.it.uc3m.es Universidad Carlos III de Madrid
 *
 */
final public class wonderOSC extends Thread {
    public static int SERVERPORT=3010;
    private WlAvatarCharacter avatarCharacter;
    Iterable<String> actions;
    private DataInputStream is = null;
    Logger logger;
    OSCPortIn receiver;
    boolean isRunning=false;

    public wonderOSC(/*WlAvatarCharacter avatar*/){

        logger = Logger.getLogger("wonderOSC");
        try{        
            receiver=new OSCPortIn(SERVERPORT);
        }catch( java.net.SocketException e){ 
            logger.severe(e.toString());
        }
        return;        
    }

    @Override
    public void run(){
        
       // receiver.startListening();

         try{
              receiver.run();
         }catch(Exception e){
             logger.severe("Error in OSC-run");
         }

        
        receiver.close();
            return;
    }

    public void addListener(String messageType,  wonderOSCListener listener ){
            if(receiver.isListening()){
                receiver.stopListening();

            }
            this.receiver.addListener(messageType, listener);
            receiver.startListening();
            return;
    }


    public void closeForever(){
        if(this.receiver.isListening()){
            try{
                receiver.stopListening();
                 receiver.close();
             }catch(Exception e){
                logger.severe("Error in OSC-run");
            }
        }
    }
}



