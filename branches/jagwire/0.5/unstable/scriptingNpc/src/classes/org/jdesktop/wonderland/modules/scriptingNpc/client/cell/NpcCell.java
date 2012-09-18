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
package org.jdesktop.wonderland.modules.scriptingNpc.client.cell;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import imi.character.CharacterMotionListener;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.statemachine.GameContext;
import imi.scene.PMatrix;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.avatarbase.client.imi.ImiAvatarLoaderFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigMessage;
import org.jdesktop.wonderland.modules.scriptingNpc.client.cell.NpcControllerFrame;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcCellChangeMessage;
import org.jdesktop.wonderland.modules.scriptingComponent.client.IntercellEvent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingActionClass;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingRunnable;

/**
 *
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 */
public class NpcCell extends Cell {

    private final JMenuItem menuItem;
    boolean menuAdded = false;
    private AvatarImiJME renderer;
    @UsesCellComponent
    private ProximityComponent proximityComp;
    @UsesCellComponent
    private MovableAvatarComponent movableAvatar;
    @UsesCellComponent
    private ScriptingComponent scriptingComponent;
    private NPCProximityListener listenerProx;
    private Vector3f npcPosition;
    private GoTo myGoTo;
    private boolean moveFinished = false;
    private String[] nameList = null;

    private Runnable[] cmdArray = new Runnable[20];

    public NpcCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        
        // Create a menu item to control the NPC
        menuItem = new JMenuItem("NPC " + cellID + " controls...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NpcControllerFrame ncf = new NpcControllerFrame(NpcCell.this,
                        renderer.getAvatarCharacter());
                ncf.setVisible(true);
            }
        });

        // Create a proximity listener that will be added in setStatus()
        listenerProx = new NPCProximityListener();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if(status == CellStatus.RENDERING)
        {
            System.out.println("In NPC - rendering");
// If I put the calls to register the message listener here, it doesn't register because on the MacBook
// RENDERING for status doesn't happen
        }
        // If the Cell is being made active and increasing, then add the menu
        // item. Also add the proximity listener
        if (status == CellStatus.ACTIVE && increasing == true) {
            JmeClientMain.getFrame().addToEditMenu(menuItem, -1);
//            BoundingVolume bv[] = new BoundingVolume[] { getLocalBounds() };
//            proximityComp.addProximityListener(listenerProx, bv);
            NpcCellMessageReceiver recv = new NpcCellMessageReceiver();
            ChannelComponent channel = getComponent(ChannelComponent.class);
            channel.addMessageReceiver(NpcCellChangeMessage.class, recv);

            ScriptingActionClass sac = new ScriptingActionClass();
            sac.setName("NPC");
            sac.insertCmdMap("testit", testitRun);
            sac.insertCmdMap("move", moveRun);
            sac.insertCmdMap("selectAvatar", avatarSelectAvatarRun);
            sac.insertCmdMap("startForward", avatarStartForwardRun);
            sac.insertCmdMap("stopForward", avatarStopForwardRun);
            sac.insertCmdMap("startBack", avatarStartBackRun);
            sac.insertCmdMap("stopBack", avatarStopBackRun);
            sac.insertCmdMap("startLeft", avatarStartLeftRun);
            sac.insertCmdMap("stopLeft", avatarStopLeftRun);
            sac.insertCmdMap("startRight", avatarStartRightRun);
            sac.insertCmdMap("stopRight", avatarStopRightRun);
            sac.insertCmdMap("startUp", avatarStartUpRun);
            sac.insertCmdMap("stopUp", avatarStopUpRun);
            sac.insertCmdMap("startDown", avatarStartDownRun);
            sac.insertCmdMap("stopDown", avatarStopDownRun);
            sac.insertCmdMap("dumpAnimations", avatarDumpAnimationsRun);
            sac.insertCmdMap("runAnimation", avatarRunAnimationRun);
            sac.insertCmdMap("stopAnimation", avatarStopAnimationRun);
            System.out.println("in sac stuff - nameList = " + nameList);
            scriptingComponent.putActionObject(sac);
            return;
        }

        // if the Cell is being brought back down through the ACTIVE state,
        // then remove the menu item
        if (status == CellStatus.ACTIVE && increasing == false) {
            JmeClientMain.getFrame().removeFromEditMenu(menuItem);
            return;
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType)
        {
        CellRenderer ret = null;
        System.out.println("Enter createCellRenderer");

        switch (rendererType)
            {
            case RENDERER_2D:
                // No 2D Renderer yet
                break;
            case RENDERER_JME:
                try
                    {
                    ServerSessionManager session = getCellCache().getSession().getSessionManager();
                    System.out.println("After session = " + session);

                    ret = ClientContextJME.getAvatarRenderManager().createRenderer(session, this);

                    if (ret instanceof AvatarImiJME)
                        {
                        renderer = (AvatarImiJME) ret;
                        System.out.println("renderer = " + renderer);
                        }
                    }
                catch (RendererUnavailable ex)
                    {
                    System.out.println("catch for createCellRenderer");
                    Logger.getLogger(NpcCell.class.getName()).log(Level.SEVERE, null, ex);
                    ret = new AvatarJME(this);
                    }
                break;
            }

        return ret;
        }

    public void testit(float x, float y, float z)
        {
        System.out.println("testit x = " + x + " y = " + y + " z = " + z);
        }

    ScriptingRunnable testitRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            testit(x, y, z);
            System.out.println("ScriptingActionClass - enter testit");
            }
        };

    public void avatarSelectAvatar(String avatar)
        {
        ChannelComponent cc = NpcCell.this.getComponent(ChannelComponent.class);

        // From the partial URI, add the module prefix
        String uri = "wla://avatarbaseart/" + avatar;
        String urlString = null;
        try {
            urlString = AssetUtils.getAssetURL(uri, NpcCell.this).toExternalForm();
        } catch (java.net.MalformedURLException excp) {
            logger.log(Level.WARNING, "Unable to form URL from " + uri, excp);
            return;
        }

        // Form up a message and send
        String className = ImiAvatarLoaderFactory.class.getName();
        AvatarConfigInfo info = new AvatarConfigInfo(urlString, className);
        cc.send(AvatarConfigMessage.newRequestMessage(info));
        }

    ScriptingRunnable avatarSelectAvatarRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarSelectAvatar(avatar);
            System.out.println("ScriptingActionClass - enter avatarStartForward");
            }
        };

    public void avatarStartForward()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Forward);
        }

    ScriptingRunnable avatarStartForwardRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartForward();
            System.out.println("ScriptingActionClass - enter avatarStartForward");
            }
        };

    public void avatarRunAnimation(String animation)
        {
        renderer.getAvatarCharacter().playAnimation(animation);
        }

    ScriptingRunnable avatarRunAnimationRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarRunAnimation(animation);
            System.out.println("ScriptingActionClass - enter avatarRunAnimation");
            }
        };

    public void avatarStopAnimation()
        {
        renderer.getAvatarCharacter().stop();
        }

    ScriptingRunnable avatarStopAnimationRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopAnimation();
            System.out.println("ScriptingActionClass - enter avatarStopAnimation");
            }
        };
        
    public String[] avatarDumpAnimations()
        {
        int     i = 0;
        nameList = new String[50];

        for(String anim : renderer.getAvatarCharacter().getAnimationNames())
            {
                System.out.println("Avatar animation = " + anim);
                nameList[i] = anim;
                i++;
            }
        System.out.println("in avatarDumpAnimations - list = " + nameList);
        return nameList;
        }

    ScriptingRunnable avatarDumpAnimationsRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            String[] list = avatarDumpAnimations();

            setNameArray(list);
            System.out.println("In npc avatarDumpAnimationsRun - exit avatarDumpAnimations - list = " + list + " first = " + list[0]);
            }
        };

    public void avatarStopForward()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Forward);
        }

    ScriptingRunnable avatarStopForwardRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopForward();
            System.out.println("ScriptingActionClass - enter avatarStopForward");
            }
        };

    public void avatarStartBack()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Back);
        }

    ScriptingRunnable avatarStartBackRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartBack();
            System.out.println("ScriptingActionClass - enter avatarStartBack");
            }
        };

    public void avatarStopBack()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Back);
        }

    ScriptingRunnable avatarStopBackRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopBack();
            System.out.println("ScriptingActionClass - enter avatarStopBack");
            }
        };

    public void avatarStartLeft()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Left);
        }

    ScriptingRunnable avatarStartLeftRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartLeft();
            System.out.println("ScriptingActionClass - enter avatarStartLeft");
            }
        };

    public void avatarStopLeft()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Left);
        }

    ScriptingRunnable avatarStopLeftRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopLeft();
            System.out.println("ScriptingActionClass - enter avatarStopLeft");
            }
        };

    public void avatarStartRight()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Right);
        }

    ScriptingRunnable avatarStartRightRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartRight();
            System.out.println("ScriptingActionClass - enter avatarStartRight");
            }
        };

    public void avatarStopRight()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Right);
        }

    ScriptingRunnable avatarStopRightRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopRight();
            System.out.println("ScriptingActionClass - enter avatarStopRight");
            }
        };

    public void avatarStartUp()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Up);
        }

    ScriptingRunnable avatarStartUpRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartUp();
            System.out.println("ScriptingActionClass - enter avatarStartUp");
            }
        };

    public void avatarStopUp()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Up);
        }

    ScriptingRunnable avatarStopUpRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopUp();
            System.out.println("ScriptingActionClass - enter avatarStopUp");
            }
        };

    public void avatarStartDown()
        {
        renderer.getAvatarCharacter().triggerActionStart(TriggerNames.Move_Down);
        }

    ScriptingRunnable avatarStartDownRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStartDown();
            System.out.println("ScriptingActionClass - enter avatarStartDown");
            }
        };

    public void avatarStopDown()
        {
        renderer.getAvatarCharacter().triggerActionStop(TriggerNames.Move_Down);
        }

    ScriptingRunnable avatarStopDownRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            avatarStopDown();
            System.out.println("ScriptingActionClass - enter avatarStopDown");
            }
        };

    public void move(int x, int y, int z)
        {
        npcPosition = new Vector3f(x, y, z);
        moveFinished = false;

        System.out.println("In move before goTo - npcPosition = " + npcPosition);
        goTo();

        System.out.println("In move after goTo - npcPosition = " + npcPosition);

        CellTransform transform = new CellTransform(null, npcPosition, null);
        NpcCellChangeMessage msg = new NpcCellChangeMessage(getCellID(), transform);
        sendCellMessage(msg);

        CharacterMotionListener motionListener = new CharacterMotionListener()
            {

            public void transformUpdate(Vector3f translation, PMatrix rotation)
                {
                //Check if NPC has reached his destination
//                if (!myGoTo.verify())
                if (myGoTo.verify())
                    {
                    System.out.println("in verify - translation = " + translation);
                    
//                    CellTransform transform = new CellTransform(rotation.getRotationJME(), translation);
//                    movableAvatar.localMoveRequest(transform, 0, false, null, null);
                    }
                else
                    {
                    ClientContext.getInputManager().postEvent(new IntercellEvent("This is a test", 400));
                    moveFinished = true;
                    }
                }
            };

        renderer.getAvatarCharacter().getController().addCharacterMotionListener(motionListener);
        }

    public boolean getMoveFinished()
        {
        return moveFinished;
        }

    public void move(float x, float y, float z)
        {
        npcPosition = new Vector3f(x, y, z);

        System.out.println("In move before goTo - npcPosition = " + npcPosition);
        goTo();

        System.out.println("In move after goTo - npcPosition = " + npcPosition);

        CellTransform transform = new CellTransform(null, npcPosition, null);
        NpcCellChangeMessage msg = new NpcCellChangeMessage(getCellID(), transform);
        sendCellMessage(msg);

        CharacterMotionListener motionListener = new CharacterMotionListener()
            {

            public void transformUpdate(Vector3f translation, PMatrix rotation)
                {
                //Check if NPC has reached his destination
//                if (!myGoTo.verify())
                if (myGoTo.verify())
                    {
                    System.out.println("in verify - translation = " + translation);

//                    CellTransform transform = new CellTransform(rotation.getRotationJME(), translation);
//                    movableAvatar.localMoveRequest(transform, 0, false, null, null);
                    }
                }
            };

        renderer.getAvatarCharacter().getController().addCharacterMotionListener(motionListener);
        }

    public void translateAvatar(float x, float y, float z)
        {
        System.out.println("Enter translate Avatar to " + x + ", " + y + ", " + z);
        npcPosition = new Vector3f(x, y, z);

        CellTransform oneTransform = new CellTransform(null, npcPosition, null);
        NpcCellChangeMessage msg = new NpcCellChangeMessage(getCellID(), oneTransform);
 //       sendCellMessage(msg);
        movableAvatar.localMoveRequest(oneTransform);
        }
    
    public void goTo()
        {
        GameContext context = renderer.getAvatarCharacter().getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        myGoTo = new GoTo(npcPosition, context);
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(myGoTo);
        }

    ScriptingRunnable moveRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            System.out.println("ScriptingActionClass - enter moveRun");
            move(x, y, z);
            }
        };

    private class NpcCellMessageReceiver implements ComponentMessageReceiver 
        {

        public void messageReceived(CellMessage message) 
            {
            System.out.println("In NPC MessageReceived - change message = message" + message);
            NpcCellChangeMessage sccm = (NpcCellChangeMessage) message;
            if (!sccm.getSenderID().equals(getCellCache().getSession().getID())) 
                {
                //npcPosition = sccm.getNpcPosition();
                npcPosition = sccm.getCellTransform().getTranslation(null);
                goTo();
                }
            }
        }

    /**
     * A class that notifies when avatars have moved within proximity of the
     * NPC Cell.
     */
    private class NPCProximityListener implements ProximityListener {

        /**
         * {@inheritDoc}
         */
        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID,
                BoundingVolume proximityVolume, int proximityIndex) {
            if (entered) {
                //Do here whatever you want
                System.out.println("*****IN");
            } else {
                //Do here whatever you want
                System.out.println("************OUT");
            }

        }
    }
}
