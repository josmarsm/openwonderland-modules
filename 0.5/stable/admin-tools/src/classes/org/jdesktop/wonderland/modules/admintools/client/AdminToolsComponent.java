/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.admintools.common.AdminToolsComponentClientState;
import org.jdesktop.wonderland.modules.admintools.common.DisconnectMessage;
import org.jdesktop.wonderland.modules.admintools.common.InvisibleMessage;
import org.jdesktop.wonderland.modules.admintools.common.MuteMessage;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * Cell component for admin tools
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class AdminToolsComponent extends CellComponent
    implements ComponentMessageReceiver
{
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsComponent.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.admintools.client.Bundle");

    @UsesCellComponent
    private ChannelComponent channel;

    // whether this user is invisible
    private boolean invisible = false;

    // invisibility effect
    private Node invisibleNode;
    private ProcessorComponent invisibleProcessor;
    
    public AdminToolsComponent(Cell cell) {
        super (cell);
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        channel.send(new InvisibleMessage(cell.getCellID(), invisible));
    }

    public void disconnect(CellID avatarCellID) {
        // determine the username from the cell ID
        String username = "unknown";
        WonderlandSession session = cell.getCellCache().getSession();
        PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);
        PresenceInfo pi = pm.getPresenceInfo(avatarCellID);
        if (pi != null) {
            username = pi.getUserID().getUsername();
        }

        // confirm
        int res = JOptionPane.showConfirmDialog(JmeClientMain.getFrame().getFrame(),
            MessageFormat.format(BUNDLE.getString("Disconnect_Check"), username),
            BUNDLE.getString("Confirm_Disconnect"), JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            BigInteger clientID = getClientID(avatarCellID);
            if (clientID != null) {
                channel.send(new DisconnectMessage(cell.getCellID(), clientID));
            }
        }
    }

    public void mute(CellID avatarCellID) {
        BigInteger clientID = getClientID(avatarCellID);
        if (clientID != null) {
            channel.send(new MuteMessage(cell.getCellID(), clientID));
        }
    }

    private BigInteger getClientID(CellID avatarCellID) {
        WonderlandSession session = cell.getCellCache().getSession();
        PresenceManager pm = PresenceManagerFactory.getPresenceManager(session);
        PresenceInfo pi = pm.getPresenceInfo(avatarCellID);
        if (pi != null && pi.getClientID() != null) {
            return pi.getClientID();
        } else {
            JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(),
                                          BUNDLE.getString("No_Presence"),
                                          BUNDLE.getString("Error"),
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);

        doSetInvisible(((AdminToolsComponentClientState) clientState).isInvisible());
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.ACTIVE && increasing) {
            channel.addMessageReceiver(InvisibleMessage.class, this);
        } else if (status == CellStatus.INACTIVE && !increasing) {
            channel.removeMessageReceiver(InvisibleMessage.class);
        } else if (status == CellStatus.RENDERING && increasing) {
            doSetInvisible(invisible);
        }
    }

    @Override
    public void messageReceived(CellMessage message) {
        if (message instanceof InvisibleMessage) {
            doSetInvisible(((InvisibleMessage) message).isInvisible());
        }
    }

    private void doSetInvisible(boolean invisible) {
        LOGGER.warning("doSetInvisible " + invisible);

        this.invisible = invisible;

        if (status.ordinal() < CellStatus.RENDERING.ordinal()) {
            return;
        }

        LOGGER.warning("Updating effects");

        if (invisibleProcessor != null) {
            removeProcessor(invisibleProcessor);
        }

        if (invisibleNode != null) {
            detachNode(invisibleNode);
        }

        if (invisible) {
            invisibleNode = new Node();
            invisibleNode.attachChild(loadInvisibleEffect());
            attachNode(invisibleNode);

            invisibleProcessor = new InvisibleProcessorComponent(invisibleNode);
            addProcessor(invisibleProcessor);
        }
    }

    private Node loadInvisibleEffect() {
        try {
            URL modelURL =  AssetUtils.getAssetURL("wla://admin-tools/tube.kmz/tube.kmz.dep");

            // create a node
            LoaderManager lm = LoaderManager.getLoaderManager();
            DeployedModel dm = lm.getLoaderFromDeployment(modelURL);
            return dm.getModelLoader().loadDeployedModel(dm, null);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "URL error loading effect", ex);
            return null;
        }
    }

    private void attachNode(final Node node) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                if (renderer instanceof AvatarImiJME) {
                    Node root = ((AvatarImiJME) renderer).getAvatarCharacter().getJScene().getExternalKidsRoot();
                    root.attachChild(node);

                    node.setLocalTranslation(new Vector3f(0f, 1f, 0f));
                    node.setLightCombineMode(LightCombineMode.Off);
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }
            }
        });
    }

    private void detachNode(final Node node) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                if (renderer instanceof AvatarImiJME) {
                    Node root = ((AvatarImiJME) renderer).getAvatarCharacter().getJScene().getExternalKidsRoot();
                    root.detachChild(node);
                }
            }
        });
    }
    
    private void addProcessor(ProcessorComponent processor) {
        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (renderer instanceof AvatarImiJME) {
            Entity e = ((AvatarImiJME) renderer).getAvatarCharacter();
            ProcessorCollectionComponent pcc =
                    e.getComponent(ProcessorCollectionComponent.class);
            if (pcc == null) {
                pcc = new ProcessorCollectionComponent();
                e.addComponent(pcc.getClass(), pcc);
            }

            pcc.addProcessor(processor);
        }
    }

    private void removeProcessor(ProcessorComponent processor) {
        CellRenderer renderer = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (renderer instanceof AvatarImiJME) {
            Entity e = ((AvatarImiJME) renderer).getAvatarCharacter();
            ProcessorCollectionComponent pcc =
                    e.getComponent(ProcessorCollectionComponent.class);
            if (pcc != null) {
                pcc.removeProcessor(processor);
            }
        }
    }

    static class InvisibleProcessorComponent extends ProcessorComponent {
        // radians per millisecond
        private static final float RATE = (float) Math.PI / 10000f;

        private final Node node;
        private float rotation = 0f;
        private long lastTime = System.currentTimeMillis();

        public InvisibleProcessorComponent(Node node) {
            this.node = node;
        }

        @Override
        public void initialize() {
            setArmingCondition(new NewFrameCondition(this));
        }

        @Override
        public void compute(ProcessorArmingCollection pac) {
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {
            long now = System.currentTimeMillis();
            long dTime = now - lastTime;

            rotation += RATE * dTime;
            rotation %= 2 * Math.PI;

            Quaternion q = new Quaternion();
            q.fromAngleAxis(rotation, new Vector3f(0, 1, 0));
            node.setLocalRotation(q);
            ClientContextJME.getWorldManager().addToUpdateList(node);

            lastTime = now;
        }
    }
}
