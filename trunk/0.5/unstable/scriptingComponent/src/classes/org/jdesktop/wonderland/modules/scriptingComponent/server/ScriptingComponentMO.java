/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.scriptingComponent.server;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentClientState;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentServerState;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentChangeMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentICEMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentNpcMoveMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentTransformMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author morrisford
 */
public class ScriptingComponentMO extends CellComponentMO  
    {
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef;

    private ManagedReference<ScriptingComponentChangeReceiver> receiverRef;

    private int totalEvents = 30;

    private String info;
//    private String[] eventNames;
    private String[] eventNames = new String[totalEvents];
//    private String[] eventScriptType;
    private String[] eventScriptType = new String[totalEvents];;
    private int      iceCode;
    private String   payload;
    private Vector3f  translateTransform;
    private Quaternion  rotateTransform;
    private Vector3f  scaleTransform;

    public static final int MOUSE1_EVENT = 0;
    public static final int MOUSE2_EVENT = 1;
    public static final int MOUSE3_EVENT = 2;
    public static final int MOUSE1S_EVENT = 3;
    public static final int MOUSE2S_EVENT = 4;
    public static final int MOUSE3S_EVENT = 5;
    public static final int MOUSE1C_EVENT = 6;
    public static final int MOUSE2C_EVENT = 7;
    public static final int MOUSE3C_EVENT = 8;
    public static final int MOUSE1A_EVENT = 9;
    public static final int MOUSE2A_EVENT = 10;
    public static final int MOUSE3A_EVENT = 11;

    public static final int TIMER_EVENT = 12;
    public static final int STARTUP_EVENT = 13;
    public static final int PROXIMITY_EVENT = 14;

    public static final int MESSAGE1_EVENT = 15;
    public static final int MESSAGE2_EVENT = 16;
    public static final int MESSAGE3_EVENT = 17;
    public static final int MESSAGE4_EVENT = 18;

    public static final int INTERCELL_EVENT = 19;
    public static final int CHAT_EVENT = 20;
    public static final int PRESENCE_EVENT = 21;

    private Vector3f npcPosition;


     /**
     * Create a ScriptingComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */

    
    public ScriptingComponentMO(CellMO cell)
        {
        super(cell);
        System.out.println("ScriptingComponentMO : In constructor");
        // set up the reference to the receiver
        ScriptingComponentChangeReceiver receiver = new ScriptingComponentChangeReceiver(cellRef, this);
        receiverRef = AppContext.getDataManager().createReference(receiver);

        info = "Default";

        eventNames[MOUSE1_EVENT] = "mouse1.js";
        eventNames[MOUSE2_EVENT] = "mouse2.js";
        eventNames[MOUSE3_EVENT] = "mouse3.js";
        eventNames[MOUSE1S_EVENT] = "mouse1s.js";
        eventNames[MOUSE2S_EVENT] = "mouse2s.js";
        eventNames[MOUSE3S_EVENT] = "mouse3s.js";
        eventNames[MOUSE1C_EVENT] = "mouse1c.js";
        eventNames[MOUSE2C_EVENT] = "mouse2c.js";
        eventNames[MOUSE3C_EVENT] = "mouse3c.java";
        eventNames[MOUSE1A_EVENT] = "mouse1a.js";
        eventNames[MOUSE2A_EVENT] = "mouse2a.js";
        eventNames[MOUSE3A_EVENT] = "mouse3a.js";
        eventNames[TIMER_EVENT] = "timer.js";
        eventNames[STARTUP_EVENT] = "startup.js";
        eventNames[PROXIMITY_EVENT] = "prox.js";
        eventNames[MESSAGE1_EVENT] = "message1.js";
        eventNames[MESSAGE2_EVENT] = "message2.js";
        eventNames[MESSAGE3_EVENT] = "message3.js";
        eventNames[MESSAGE4_EVENT] = "message4.js";
        eventNames[INTERCELL_EVENT] = "ice.js";
        eventNames[CHAT_EVENT] = "chat.js";
        eventNames[PRESENCE_EVENT] = "presence.js";

        eventScriptType[MOUSE1_EVENT] = "javascript";
        eventScriptType[MOUSE2_EVENT] = "javascript";
        eventScriptType[MOUSE3_EVENT] = "javascript";
        eventScriptType[MOUSE1S_EVENT] = "javascript";
        eventScriptType[MOUSE2S_EVENT] = "javascript";
        eventScriptType[MOUSE3S_EVENT] = "javascript";
        eventScriptType[MOUSE1C_EVENT] = "javascript";
        eventScriptType[MOUSE2C_EVENT] = "javascript";
        eventScriptType[MOUSE3C_EVENT] = "java";
        eventScriptType[MOUSE1A_EVENT] = "javascript";
        eventScriptType[MOUSE2A_EVENT] = "javascript";
        eventScriptType[MOUSE3A_EVENT] = "javascript";
        eventScriptType[TIMER_EVENT] = "javascript";
        eventScriptType[STARTUP_EVENT] = "javascript";
        eventScriptType[PROXIMITY_EVENT] = "javascript";
        eventScriptType[MESSAGE1_EVENT] = "javascript";
        eventScriptType[MESSAGE2_EVENT] = "javascript";
        eventScriptType[MESSAGE3_EVENT] = "javascript";
        eventScriptType[MESSAGE4_EVENT] = "javascript";
        eventScriptType[INTERCELL_EVENT] = "javascript";
        eventScriptType[CHAT_EVENT] = "javascript";
        eventScriptType[PRESENCE_EVENT] = "javascript";

        }

    @Override
    protected void setLive(boolean live) 
        {
        super.setLive(live);
        ChannelComponentMO channelComponent = (ChannelComponentMO) cellRef.get().getComponent(ChannelComponentMO.class);
        if (live) 
            {
            
            channelComponentRef.getForUpdate().addMessageReceiver(ScriptingComponentChangeMessage.class, new ScriptingComponentChangeReceiver(cellRef, this));
            channelComponentRef.getForUpdate().addMessageReceiver(ScriptingComponentICEMessage.class, new ScriptingComponentChangeReceiver(cellRef, this));
            channelComponentRef.getForUpdate().addMessageReceiver(ScriptingComponentTransformMessage.class, new ScriptingComponentChangeReceiver(cellRef, this));
            channelComponentRef.getForUpdate().addMessageReceiver(ScriptingComponentNpcMoveMessage.class, new ScriptingComponentChangeReceiver(cellRef, this));
            } 
        else 
            {
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentChangeMessage.class);
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentICEMessage.class);
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentTransformMessage.class);
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentNpcMoveMessage.class);
            }
        System.out.println("ScriptingComponentMO : In setLive = live = " + live);
        }

    @Override
    protected String getClientClass() 
        {
        System.out.println("ScriptingComponentMO : In getClientClass");
        return "org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent";
        }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) 
        {

        if (state == null) 
            {
            state = new ScriptingComponentClientState();
            }
        ((ScriptingComponentClientState)state).setInfo(info);
        ((ScriptingComponentClientState)state).setScriptType(eventScriptType);
        ((ScriptingComponentClientState)state).setEventNames(eventNames);
        return super.getClientState(state, clientID, capabilities);
        }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) 
        {
        if (state == null) 
            {
            state = new ScriptingComponentServerState();
            }
        ((ScriptingComponentServerState)state).setInfo(info);
        ((ScriptingComponentServerState)state).setScriptType(eventScriptType);
        ((ScriptingComponentServerState)state).setEventNames(eventNames);
        System.out.println("ScriptingComponentMO : In getServerState");
        return super.getServerState(state);
        }

    @Override
    public void setServerState(CellComponentServerState state) 
        {
        super.setServerState(state);
        info = ((ScriptingComponentServerState)state).getInfo();
        eventNames = ((ScriptingComponentServerState)state).getEventNames();
        eventScriptType = ((ScriptingComponentServerState)state).getScriptType();
        System.out.println("ScriptingComponentMO - : In setServerState");
        }
    
     private static class ScriptingComponentChangeReceiver implements ComponentMessageReceiver, ManagedObject
        {
        private ManagedReference<ScriptingComponentMO> compRef;
        private ManagedReference<CellMO> cellRef;
        
        public ScriptingComponentChangeReceiver(ManagedReference<CellMO> cellRef, ScriptingComponentMO comp) 
            {
//            super(cellMO);
            compRef = AppContext.getDataManager().createReference(comp);
            this.cellRef = cellRef;
            }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) 
            {
            ScriptingComponentMO cellMO = compRef.getForUpdate();
            ChannelComponentMO chanMO = cellMO.channelComponentRef.getForUpdate();
            
            if(message instanceof ScriptingComponentChangeMessage)
                {
                ScriptingComponentChangeMessage ent = (ScriptingComponentChangeMessage) message;
                cellMO.eventNames = ent.getEventNames();
                cellMO.eventScriptType = ent.getScriptType();        
                chanMO.sendAll(clientID, message);
                System.out.println("ScriptingComponentMO.messageReceived - Change message - Client ID = " + clientID);
                }
            else if(message instanceof ScriptingComponentICEMessage)
                {
                ScriptingComponentICEMessage ent = (ScriptingComponentICEMessage) message;
                cellMO.iceCode = ent.getIceCode();
                cellMO.payload = ent.getPayload();
                chanMO.sendAll(clientID, message);
                System.out.println("ScriptingComponentMO.messageReceived - ICE message - code = "+ cellMO.iceCode + " payload = " + cellMO.payload + " - Client ID = " + clientID);
                }
            else if(message instanceof ScriptingComponentTransformMessage)
                {
                ScriptingComponentTransformMessage ent = (ScriptingComponentTransformMessage) message;
                int transformType = ent.getTransformCode();
                switch(transformType)
                    {
                    case ScriptingComponentTransformMessage.TRANSLATE_TRANSFORM:
                        {
                        cellMO.translateTransform = ent.getVector();
                        System.out.println("ScriptingComponentMO.messageReceived - Translate transform message - code = "+ ent.getTransformCode() + " transform = " + cellMO.translateTransform + " - Client ID = " + clientID);
                        break;
                        }
                    case ScriptingComponentTransformMessage.ROTATE_TRANSFORM:
                        {
                        cellMO.rotateTransform = ent.getTransform();
                        System.out.println("ScriptingComponentMO.messageReceived - Rotate transform message - code = "+ ent.getTransformCode() + " transform = " + cellMO.rotateTransform + " - Client ID = " + clientID);
                        break;
                        }
                    case ScriptingComponentTransformMessage.SCALE_TRANSFORM:
                        {
                        System.out.println("ScriptingComponentMO.messageReceived - Scale transform message - code = "+ ent.getTransformCode() + " transform = " + cellMO.scaleTransform + " - Client ID = " + clientID);
                        cellMO.scaleTransform = ent.getVector();
                        break;
                        }
                    default:
                        {
                        break;
                        }
                    }
                chanMO.sendAll(clientID, message);
                }
            else if(message instanceof ScriptingComponentNpcMoveMessage)
                {
                ScriptingComponentNpcMoveMessage ent = (ScriptingComponentNpcMoveMessage) message;
                System.out.println("CellMO with npc move message");
                cellMO.npcPosition = ent.getCellTransform().getTranslation(null);
                CellMO underlyingCellMO = cellRef.getForUpdate();
//                underlyingCellMO.setLocalTransform(ent.getCellTransform());
                chanMO.sendAll(clientID, message);
                }
            }
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) 
            {
            ScriptingComponentMO cellMO = compRef.getForUpdate();
            
            System.out.println("ScriptingComponentMO.recordMessage");
            }
        }
    }
