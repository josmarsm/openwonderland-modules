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
import org.jdesktop.wonderland.modules.wonderOSC.client.*;
import com.illposed.osc.*;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.*;
import imi.character.avatar. AvatarContext.TriggerNames;




/**
 * @author Sergio Galan sgalan@inv.it.uc3m.es Universidad Carlos III de Madrid
 *
 */

public abstract class wonderOSCListener implements OSCListener {
    WlAvatarCharacter avatarCharacter;

    public wonderOSCListener(WlAvatarCharacter avatar){
        avatarCharacter=avatar;
        return;
    }
    public wonderOSCListener(){return;}

    public void addAvatar(WlAvatarCharacter avatar){
        avatarCharacter=avatar;
    }


    final public void acceptMessage(java.util.Date time, OSCMessage message) {
		//	System.out.println("Message received!");
            act(message.getAddress(),message.getArguments());
            return;
    }

    abstract void act(String address,  java.lang.Object[] args );//{
        //System.out.println("recv message " + address);
        //return;
    //}

    public void stopAll(){
       avatarCharacter.triggerActionStop(TriggerNames.Move_Back);
       avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);
       avatarCharacter.triggerActionStop(TriggerNames.Move_Left);
       avatarCharacter.triggerActionStop(TriggerNames.Move_Right);
    }

}
class avatarMoveForward extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        avatarCharacter.triggerActionStop(TriggerNames.Move_Back);
        avatarCharacter.triggerActionStart(TriggerNames.Move_Forward);
        //avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);
        return;
    }
}

class avatarMoveBack extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);
        avatarCharacter.triggerActionStart(TriggerNames.Move_Back);
        //avatarCharacter.triggerActionStop(TriggerNames.Move_Back);
        return;
    }
}

class avatarStop extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        stopAll();
        return;
    }
}

class avatarMoveLeft extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        avatarCharacter.triggerActionStop(TriggerNames.Move_Right);
        avatarCharacter.triggerActionStart(TriggerNames.Move_Left);
        return;
    }
}

class avatarMoveRight extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        avatarCharacter.triggerActionStop(TriggerNames.Move_Left);
        avatarCharacter.triggerActionStart(TriggerNames.Move_Right);
        return;
    }
}

class sunSpotXYZ extends wonderOSCListener{
    @Override
    void act(String address,  java.lang.Object[] args){
        Float X= (Float) args[0];
        Float Y= (Float) args[1];
        Float Z= (Float) args[2];
        
        if(Y>0.4){
            avatarCharacter.triggerActionStop(TriggerNames.Move_Back);
            avatarCharacter.triggerActionStart(TriggerNames.Move_Forward);            
        }
        else if(Y<-0.5){
             avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);
             avatarCharacter.triggerActionStart(TriggerNames.Move_Back);                
        }
        else{
            avatarCharacter.triggerActionStop(TriggerNames.Move_Forward);
            avatarCharacter.triggerActionStop(TriggerNames.Move_Back);
        }
        if(X>0.4){           //right
            avatarCharacter.triggerActionStop(TriggerNames.Move_Left);
            avatarCharacter.triggerActionStart(TriggerNames.Move_Right);
        }
        else if(X<-0.4){
             avatarCharacter.triggerActionStop(TriggerNames.Move_Right);
             avatarCharacter.triggerActionStart(TriggerNames.Move_Left);
        }
        else{
            avatarCharacter.triggerActionStop(TriggerNames.Move_Left);
            avatarCharacter.triggerActionStop(TriggerNames.Move_Right);
        }
        return;
    }
}



class listenerFactory{
    WlAvatarCharacter avatarCharacter;
    public listenerFactory(WlAvatarCharacter avatar){
        avatarCharacter=avatar;
    }

    wonderOSCListener myListener;
    public wonderOSCListener getListener(String name){
        try {
            Class clase = Class.forName("org.jdesktop.wonderland.modules.wonderOSC.client."+name);
            myListener=(wonderOSCListener) clase.newInstance();
            myListener.addAvatar(avatarCharacter);
            return myListener;
        }
		catch (ClassNotFoundException e) {		// class not found
			System.out.println("class not found:"+ name);
			return null;
		}
        catch (Exception e) {					// No puedo instanciar la clase
			 System.out.println("class not instantiated");
			return null;
		}
    }

}

