/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.wonderOSC.client;
import org.jdesktop.wonderland.modules.wonderOSC.client.*;
import com.illposed.osc.*;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.*;
import imi.character.avatar. AvatarContext.TriggerNames;




/**
 *
 * @author sergio2
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
        System.out.println("recibido ");
        Float X= (Float) args[0];
        Float Y= (Float) args[1];
        Float Z= (Float) args[2];
        System.out.println( X +" " +Y+" " +Z);
        
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

