/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.client;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jmex.model.collada.ColladaAnimation;
import com.jmex.model.collada.ColladaAnimationGroup;
import com.jmex.model.collada.ColladaRootNode;
import com.wonderbuilders.modules.animation.common.Animation;
import static com.wonderbuilders.modules.animation.common.Animation.AnimationPlayType.EZSCRIPT_FUNCTIONS;
import static com.wonderbuilders.modules.animation.common.Animation.AnimationPlayType.FORWARD_REVERSE;
import static com.wonderbuilders.modules.animation.common.Animation.AnimationPlayType.FRAME_RANGE;
import static com.wonderbuilders.modules.animation.common.Animation.AnimationPlayType.LOOP;
import static com.wonderbuilders.modules.animation.common.Animation.AnimationPlayType.PLAY_ONCE;
import com.wonderbuilders.modules.animation.common.AnimationComponentClientState;
import com.wonderbuilders.modules.animation.common.AnimationComponentMessage;
import com.wonderbuilders.modules.animation.common.AnimationConstants;
import com.wonderbuilders.modules.animation.common.EZScriptAnimationControl;
import com.wonderbuilders.modules.animation.common.FrameRange;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent;

/**
 * Base class for Animation component.
 *
 * @author Vladimir Djurovic
 * @author Abhishek Upadhyay
 */
public class AnimationComponent extends CellComponent implements ProximityListener, ContextMenuActionListener {

    /**
     * Resource bundle.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com/wonderbuilders/modules/animation/client/resources/strings");

    /**
     * EZScript function to play animation once.
     */
    private static final String PLAY_ANIMATION_FUNCTION = "playAnimation()";

    /**
     * EZScript function to play animation in reverse.
     */
    private static final String PLAY_REVERSE_FUNCTION = "playReverse()";

    /**
     * EZScript function to play animation in a loop.
     */
    private static final String PLAY_LOOP_FUNCTION = "playLoop";

    /**
     * Mouse event handler.
     */
    private MouseEventListener mouseListener;

    /**
     * Animation control state.
     */
    private Animation animation;

    /**
     * Indicates that animation played at least once.
     */
    private boolean playedOnce = false;

    /**
     * Proximity component.
     */
    @UsesCellComponent
    private ProximityComponent proximity;

    /**
     * Component for right-lick context menu.
     */
    @UsesCellComponent
    private ContextMenuComponent ctxMenuComp;

    /**
     * Factory for context menu items.
     */
    private ContextMenuFactorySPI ctxMenuFactory;

    /**
     * Menu items for animation control.
     */
    private ContextMenuItem[] ctxMenuItems;

    /**
     * Current animation group for this cell.
     */
    private ColladaAnimationGroup animationGroup;

    /**
     * EZScript component of the cell (if any).
     */
    private EZScriptComponent ezScript;

    /**
     * Default play direction for EZScript.
     */
    private ColladaAnimation.PlayDirection scriptDirection = ColladaAnimation.PlayDirection.FORWARD;

    /**
     * Creates new instance.
     *
     * @param cell parent cell
     */
    public AnimationComponent(Cell cell) {
        super(cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (status == CellStatus.RENDERING && increasing) {
            if (animation == null) {
                animation = new Animation();
            }
            CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
            Node sceneRoot = rc.getSceneRoot();
            if (sceneRoot instanceof RenderComponent.AttachPointNode) {
                sceneRoot = (Node) ((RenderComponent.AttachPointNode) sceneRoot).getChild(0);
            }
            TreeScan.findNode(sceneRoot, new ProcessNodeInterface() {

                @Override
                public boolean processNode(Spatial node) {
                    if (node instanceof ColladaRootNode) {
                        animationGroup = ((ColladaRootNode) node).getCurrentGroup();
                        if (animationGroup != null) {
                            // UGLY HACK!!!!! This is workaround to the problem animations sometimes don't initialize properly.
                            // There's a delay between the time cell is loaded and animation component is applied. Animation starts
                            // running immediatelly, and stops when component is added. That's why it appears not in correct position.
                            // This will start animation for a short time to return it to starting position
                            if (animationGroup.isPlaying()) {
                                animationGroup.setPlaybackSpeed(100f);
                                animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
                            }
                        }
                        return false;
                    }
                    return true;
                }
            });
            setTrigger();
            ezScript = cell.getComponent(EZScriptComponent.class);
            // add message receiver for synchronizing state
            ChannelComponent channel = cell.getComponent(ChannelComponent.class);
            channel.addMessageReceiver(AnimationComponentMessage.class, new AnimationMessageReceiver());
        } else if (status == CellStatus.VISIBLE && increasing) {
            /* HACK!On some angle of camera the objects get disappeared which has animation component.
             * We are not updating the bounds after animating the object. We tried it but never get the
             * exact time when animation get finished. So we set the cull state to Never for th object
             * when we add animation component in it.
             */
            Node node;
            CellRendererJME cr = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (cr != null) {
                RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);
                if (rc != null) {
                    node = (Node) rc.getSceneRoot();
                    //Check for CullHint status, set it to Never, if not
                    if (node.getCullHint() != Spatial.CullHint.Never) {
                        node.setCullHint(Spatial.CullHint.Never);
                    }
                }
            }
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        AnimationComponentClientState accs = (AnimationComponentClientState) clientState;
        animation = accs.getAnimation();
        if (animationGroup != null) {
            if (animation.getPlayType() == Animation.AnimationPlayType.LOOP) {
                animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.REPEAT);
            } else {
                animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
            }
        }
        if (cell.getStatus() == CellStatus.VISIBLE) {
            setTrigger();
        }

    }

    /**
     * Set configured animation trigger.
     */
    private void setTrigger() {
        switch (animation.getTrigger()) {
            case LEFT_CLICK:
                //set mouse listener, remove proximity and right click listeners
                if (mouseListener == null) {
                    mouseListener = new MouseEventListener(this);
                }
                mouseListener.addToEntity(((CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
                if (proximity != null) {
                    proximity.removeProximityListener(this);
                }
                break;
            case PROXIMITY:
                //set proximity trigger, remove the others
                if (proximity != null) {
                    BoundingVolume bv = new BoundingSphere(animation.getProximityRange(), Vector3f.ZERO);
                    proximity.addProximityListener(this, new BoundingVolume[]{bv});
                }
                if (mouseListener != null) {
                    mouseListener.removeFromEntity(((CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
                }
                break;
            case RIGHT_CLICK:
                if (proximity != null) {
                    proximity.removeProximityListener(this);
                }
                if (mouseListener != null) {
                    mouseListener.removeFromEntity(((CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
                }
                break;
        }
        if (ctxMenuComp != null) {
            // set up animation commands
            ctxMenuComp.removeContextMenuFactory(ctxMenuFactory);
            switch (animation.getPlayType()) {
                case PLAY_ONCE:
                    SimpleContextMenuItem item = new SimpleContextMenuItem(animation.getName(), this);
                    ctxMenuItems = new ContextMenuItem[]{item};
                    break;
                case FORWARD_REVERSE:
                    // forward menu item
                    SimpleContextMenuItem fwdItem = new SimpleContextMenuItem(animation.getName(), this);
                    // reverse menu item
                    String revLabel = animation.getReverseCommand();
                    if (revLabel == null || revLabel.isEmpty()) {
                        revLabel = AnimationConstants.DEFAULT_REVERSE_COMMAND;
                    }
                    SimpleContextMenuItem revItem = new SimpleContextMenuItem(revLabel, this);
                    ctxMenuItems = new ContextMenuItem[]{fwdItem, revItem};
                    break;
                case LOOP:
                    String startCmd = animation.getStartLoop();
                    if (startCmd == null || startCmd.isEmpty()) {
                        startCmd = AnimationConstants.DEFAULT_START_COMMAND;
                    }
                    SimpleContextMenuItem startItem = new SimpleContextMenuItem(startCmd, this);
                    String stopCmd = animation.getStopLoop();
                    if (startCmd == null || startCmd.isEmpty()) {
                        stopCmd = AnimationConstants.DEFAULT_STOP_COMMAND;
                    }
                    SimpleContextMenuItem stopItem = new SimpleContextMenuItem(stopCmd, this);
                    stopItem.setEnabled(false);
                    ctxMenuItems = new ContextMenuItem[]{startItem, stopItem};
                    break;
                case FRAME_RANGE:
                    SortedSet<FrameRange> ranges = animation.getRanges();
                    ctxMenuItems = new ContextMenuItem[ranges.size()];
                    int i = 0;
                    for (FrameRange fr : ranges) {
                        SimpleContextMenuItem menuItem = new SimpleContextMenuItem(fr.getCommand(), this);
                        ctxMenuItems[i] = menuItem;
                        i++;
                    }
                    break;
                case EZSCRIPT_FUNCTIONS:
                    List<EZScriptAnimationControl> controls = animation.getScriptFunctions();
                    ctxMenuItems = new ContextMenuItem[controls.size()];
                    int k = 0;
                    for (EZScriptAnimationControl ctrl : controls) {
                        SimpleContextMenuItem menuItem = new SimpleContextMenuItem(ctrl.getCommand(), this);
                        ctxMenuItems[k] = menuItem;
                        k++;
                    }
                    break;
            }
            ctxMenuFactory = new ContextMenuFactorySPI() {

                public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                    return ctxMenuItems;
                }
            };
            ctxMenuComp.addContextMenuFactory(ctxMenuFactory);
        }
    }

    /**
     * Play animation in a manner which depends to currently set animation
     * controls. This method will look up the contents of {@code animation}
     * field and play animation accordingly.
     * <ul>
     * <li>
     * If {@code PLAY_ONCE} is set -when method is invoked, animation will play
     * once till the end and stop
     * </li>
     * <li>
     * If {@code FORWARD_REVERSE} is set - when method is invoked, animation
     * will play once till the end and stop. On each subsequent method call,
     * animation will play once in alternating direction
     * (forward-backward-forward-etc.)
     * </li>
     * <li>
     * If {@code LOOP} is set - when method is invoked, if animation is stopped,
     * it will start running in a loop. On each subsequent method call,
     * animation will start or stop playing, depending on current state.
     * </li>
     * <li>
     * If {@code FRAME_RANGE} is set - when method is invoked, the next
     * specified frame range will start. After the last frame range is played,
     * on subsequent method call, first frame range will start again.
     * </li>
     * </ul>
     */
    public void toggleAnimation() {
        Animation.AnimationPlayType type = animation.getPlayType();
        boolean playing = false;
        boolean hasAnimation = false;
        if (animationGroup != null) {
            // revert original playing speed
            animationGroup.setPlaybackSpeed(1.0f);
            playing = animationGroup.isPlaying();
            hasAnimation = true;
        }
        switch (type) {
            case PLAY_ONCE:
                if (hasAnimation) {
                    if (!playing) {
                        animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
                        animationGroup.setPlayDirection(ColladaAnimation.PlayDirection.FORWARD);
                        // reset time for all animations
                        for (ColladaAnimation anim : animationGroup.getAnimations()) {
                            anim.setCurrentTime(0);
                        }
                        animationGroup.setPlaying(true);
                        if (animation.isIncludeEZScript() && hasEZScriptFunction(PLAY_ANIMATION_FUNCTION)) {
                            ezScript.executeScript(PLAY_ANIMATION_FUNCTION + ";");
                        }
                    }
                } else {
                    if (animation.isIncludeEZScript() && hasEZScriptFunction(PLAY_ANIMATION_FUNCTION)) {
                        ezScript.executeScript(PLAY_ANIMATION_FUNCTION + ";");
                    }
                }
                break;
            case FORWARD_REVERSE:
                if (hasAnimation) {
                    ColladaAnimation.PlayDirection direction = ColladaAnimation.PlayDirection.FORWARD;
                    if (!playing) {
                        // change play direction and reset animation times accordingly
                        if (playedOnce) {
                            direction = animationGroup.getPlayDirection();
                        } else {
                            // initially, set direction to backward. This will be flipped correctly bellow
                            direction = ColladaAnimation.PlayDirection.BACKWARD;
                        }
                        if (direction == ColladaAnimation.PlayDirection.FORWARD) {
                            direction = ColladaAnimation.PlayDirection.BACKWARD;
                            for (ColladaAnimation anim : animationGroup.getAnimations()) {
                                anim.setCurrentTime(anim.getAnimationTime());
                            }
                        } else {
                            direction = ColladaAnimation.PlayDirection.FORWARD;
                            for (ColladaAnimation anim : animationGroup.getAnimations()) {
                                anim.setCurrentTime(0);
                            }
                        }

                        animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
                        animationGroup.setPlayDirection(direction);
                        animationGroup.setPlaying(true);
                        // indicate that animation played at least once
                        playedOnce = true;

                        // handle EZScript animation
                        if (animation.isIncludeEZScript()) {
                            if (direction == ColladaAnimation.PlayDirection.FORWARD && hasEZScriptFunction(PLAY_ANIMATION_FUNCTION)) {
                                ezScript.executeScript(PLAY_ANIMATION_FUNCTION + ";");
                            } else if (direction == ColladaAnimation.PlayDirection.BACKWARD && hasEZScriptFunction(PLAY_REVERSE_FUNCTION)) {
                                ezScript.executeScript(PLAY_REVERSE_FUNCTION + ";");
                            }
                        }
                    }
                } else {
                    if (animation.isIncludeEZScript()) {
                        if (scriptDirection == ColladaAnimation.PlayDirection.FORWARD && hasEZScriptFunction(PLAY_ANIMATION_FUNCTION)) {
                            ezScript.executeScript(PLAY_ANIMATION_FUNCTION + ";");
                            scriptDirection = ColladaAnimation.PlayDirection.BACKWARD;
                        } else if (scriptDirection == ColladaAnimation.PlayDirection.BACKWARD && hasEZScriptFunction(PLAY_REVERSE_FUNCTION)) {
                            ezScript.executeScript(PLAY_REVERSE_FUNCTION + ";");
                            scriptDirection = ColladaAnimation.PlayDirection.FORWARD;
                        }
                    }
                }

                break;
            case LOOP:
                // set loop mode, if not already set
                if (animationGroup.getLoopMode() != ColladaAnimationGroup.LoopMode.REPEAT) {
                    animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.REPEAT);
                }
                animationGroup.setPlayDirection(ColladaAnimation.PlayDirection.FORWARD);
                animationGroup.setPlaying(!playing);
                // handle EZScript animation
                if (hasEZScriptFunction(PLAY_LOOP_FUNCTION)) {
                    String script = PLAY_LOOP_FUNCTION + "(" + !playing + ");";
                    ezScript.executeScript(script);
                }
                break;
            case FRAME_RANGE:
                FrameRange range = animation.getNextFrameRange();
                if (range != null) {
                    Thread th = new Thread(new FrameRangePlayer(animationGroup, range.getStart(), range.getEnd()));
                    th.start();
                }
                break;
            case EZSCRIPT_FUNCTIONS:
                String script = animation.getNextScriptFunction();
                System.out.println("SCript function: " + script);
                if (script != null && animation.isIncludeEZScript()) {
                    ezScript.executeScript(script + ";");
                }
                break;
        }
    }

    /**
     * Check if cell has EZScript function with given name defined.
     *
     * @param functionName name of the function to check
     * @return <code>true</code> if function exists, <code>false</code>
     * otherwise
     */
    private boolean hasEZScriptFunction(String functionName) {
        boolean state = false;
        // check if EZScript and present and try get it if not
        if (ezScript == null) {
            ezScript = cell.getComponent(EZScriptComponent.class);
        }
        if (ezScript != null) {
            String script = ezScript.getScriptMap().get("editor").toString();
            state = script.contains("function " + functionName);
        }
        return state;
    }

    /**
     * Runs animation configured by specified command. If command does not
     * exist, an {@code IllegalArgumentException} is thrown. If specified
     * command is not valid for currently configured animation play type, an {@code IllegalStateException] is thrown.
     *
     * @param cmdName command to execute
     */
    public void executeAnimationCommand(String cmdName) {
        if (!animation.commandExists(cmdName)) {
            throw new IllegalArgumentException("Command does not exist: " + cmdName);
        }
        switch (animation.getPlayType()) {
            case PLAY_ONCE:
                if (cmdName.equals(animation.getName())) {
                    toggleAnimation();

                } else {
                    throw new IllegalStateException("Animation is configured to play once. Please change play type.");
                }
                break;
            case FORWARD_REVERSE:
                if (cmdName.equals(animation.getName())) {
                    // play forward
                    playAnimation(ColladaAnimation.PlayDirection.FORWARD);
                    // handle EZScript animation
                    if (animation.isIncludeEZScript() && hasEZScriptFunction(PLAY_ANIMATION_FUNCTION)) {
                        ezScript.executeScript(PLAY_ANIMATION_FUNCTION + ";");
                    }
                } else if (cmdName.equals(animation.getReverseCommand()) || cmdName.equals(AnimationConstants.DEFAULT_REVERSE_COMMAND)) {
                    // play reverse
                    playAnimation(ColladaAnimation.PlayDirection.BACKWARD);
                    // handle EZScript animation
                    if (animation.isIncludeEZScript() && hasEZScriptFunction(PLAY_REVERSE_FUNCTION)) {
                        ezScript.executeScript(PLAY_REVERSE_FUNCTION + ";");
                    }
                } else {
                    throw new IllegalStateException("Animation is configured to play forward and reverse. Please change play type.");
                }
                break;
            case LOOP:
                if (cmdName.equals(animation.getStartLoop()) || cmdName.equals(AnimationConstants.DEFAULT_START_COMMAND)) {
                    // play animation loop
                    toggleAnimation();
                } else if (cmdName.equals(animation.getStopLoop()) || cmdName.equals(AnimationConstants.DEFAULT_STOP_COMMAND)) {
                    // stop animation loop
                    toggleAnimation();
                } else {
                    throw new IllegalStateException("Animation is configured to play in a loop. Please change play type.");
                }
                break;
            case FRAME_RANGE:
                animationGroup.setPlaybackSpeed(1.0f);
                // verify if we can play this command
                if (animation.isFrameRangePlayCommand(cmdName)) {
                    FrameRange fr = animation.getFrameRangeForCommand(cmdName);
                    if (fr != null) {
                        Thread th = new Thread(new FrameRangePlayer(animationGroup, fr.getStart(), fr.getEnd()));
                        th.start();
                    } else {
                        throw new IllegalStateException("No valid frame range found for command " + cmdName);
                    }
                } else {
                    throw new IllegalStateException("Animation is configured to play a frame range. Please supply valid command.");
                }
                break;
            case EZSCRIPT_FUNCTIONS:
                if (animation.isScriptCommand(cmdName) && animation.isIncludeEZScript()) {
                    ezScript.executeScript(animation.getScriptFunction(cmdName));
                } else if (animation.isFrameRangePlayCommand(cmdName) && animation.isIncludeEZScript()) {
                    // play frame range
                    FrameRange fr = animation.getFrameRangeForCommand(cmdName);
                    if (fr != null) {
                        Thread th = new Thread(new FrameRangePlayer(animationGroup, fr.getStart(), fr.getEnd()));
                        th.start();
                    } else {
                        throw new IllegalStateException("No valid frame range found for command " + cmdName);
                    }
                } else {
                    throw new IllegalStateException("Animation is configured to play EZScript function. Please supply valid function.");
                }
                break;
        }
    }

    /**
     * Play animation once in specified direction.
     *
     * @param direction play direction
     */
    private void playAnimation(final ColladaAnimation.PlayDirection direction) {
        if (animationGroup != null) {
            animationGroup.setPlaybackSpeed(1.0f);
            animationGroup.setPlayDirection(direction);
            for (ColladaAnimation anim : animationGroup.getAnimations()) {
                if (direction == ColladaAnimation.PlayDirection.FORWARD) {
                    anim.setCurrentTime(0);
                } else {
                    anim.setCurrentTime(anim.getAnimationTime());
                }

            }
            animationGroup.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
            animationGroup.setPlaying(true);
        }
    }

    /**
     * EVent handler for proximity events. When avatar enters view, it will
     * trigger configured animation.
     *
     * @param entered <code>true</code> if avatar entered view,
     * <code>false</code> otherwise
     * @param cell component cell
     * @param viewCellID ID of view cell
     * @param proximityVolume volume of proximity sphere
     * @param proximityIndex index
     */
    @Override
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if (entered) {
            toggleAnimation();
            sendUpdateMessage("toggleAnimation", null);
        } else {
            // if animation loops, exiting proximity will toggle it on/off
            if (animation.getPlayType() == Animation.AnimationPlayType.LOOP) {
                toggleAnimation();
                sendUpdateMessage("toggleAnimation", null);
            }
        }
    }

    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        String cmd = event.getContextMenuItem().getLabel();
        executeAnimationCommand(cmd);
        // enable/disable animation controls
        if (animation.getPlayType() == Animation.AnimationPlayType.LOOP) {
            if (cmd.equals(animation.getStartLoop())) {
                //disable start, enable stop
                ctxMenuItems[0].setEnabled(false);
                ctxMenuItems[1].setEnabled(true);
            } else if (cmd.equals(animation.getStopLoop())) {
                //disable stop, enable sart
                ctxMenuItems[0].setEnabled(true);
                ctxMenuItems[1].setEnabled(false);
            }
        }
        sendUpdateMessage("executeAnimationCommand", cmd);
    }

    private class AnimationMessageReceiver implements ChannelComponent.ComponentMessageReceiver {

        @Override
        public void messageReceived(CellMessage message) {
            AnimationComponentMessage msg = (AnimationComponentMessage) message;
            if (!msg.getSenderID().equals(cell.getCellCache().getSession().getID())) {
                if ("toggleAnimation".equals(msg.getMethodCall())) {
                    toggleAnimation();
                } else if ("executeAnimationCommand".equals(msg.getMethodCall())) {
                    executeAnimationCommand(msg.getParameter());
                }
            }
        }
    }

    /**
     * Send message to server that cell is updated.
     *
     * @param method method to call
     * @param param method parameter
     */
    void sendUpdateMessage(String method, String param) {
        AnimationComponentMessage msg = new AnimationComponentMessage(cell.getCellID(), method, param);
        cell.sendCellMessage(msg);
    }

}
