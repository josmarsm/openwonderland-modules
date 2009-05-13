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
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentTransformMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
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
    
    private String info;
    private String cellName;
    private String scriptURL;
    private String[] eventNames;
    private String[] eventScriptType;
    private int      iceCode;
    private String   payload;
    private Vector3f  translateTransform;
    private Quaternion  rotateTransform;
    private Vector3f  scaleTransform;
    
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
            } 
        else 
            {
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentChangeMessage.class);
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentICEMessage.class);
            channelComponentRef.getForUpdate().removeMessageReceiver(ScriptingComponentTransformMessage.class);
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
        System.out.println("ScriptingComponentMO : In getClientState - cellName = " + cellName + " scriptURL = " + scriptURL);

        if (state == null) 
            {
            state = new ScriptingComponentClientState();
            }
        ((ScriptingComponentClientState)state).setInfo(info);
        ((ScriptingComponentClientState)state).setCellName(cellName);
        ((ScriptingComponentClientState)state).setScriptURL(scriptURL);
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
        ((ScriptingComponentServerState)state).setCellName(cellName);
        ((ScriptingComponentServerState)state).setScriptURL(scriptURL);
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
        cellName = ((ScriptingComponentServerState)state).getCellName();
        scriptURL = ((ScriptingComponentServerState)state).getScriptURL();
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
                cellMO.cellName = ent.getCellName();
                cellMO.scriptURL = ent.getScriptURL();
                cellMO.eventNames = ent.getEventNames();
                cellMO.eventScriptType = ent.getScriptType();        
                chanMO.sendAll(clientID, message);
                System.out.println("ScriptingComponentMO.messageReceived - Change message - "+ cellMO.cellName + " URL = " + cellMO.scriptURL + " - Client ID = " + clientID);
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
            }
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) 
            {
            ScriptingComponentMO cellMO = compRef.getForUpdate();
            
            System.out.println("ScriptingComponentMO.recordMessage - cellName = " + cellMO.cellName);
            }
        }
    }
