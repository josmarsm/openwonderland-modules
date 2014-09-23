/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Contains state data fro animation.
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "animation")
public class Animation implements Serializable{
    
    /**
     * Enumeration of possible ways to play animation.
     */
    public enum AnimationPlayType {
        /** Play animation once from start to end and then stop. */
        PLAY_ONCE, 
        /** Play animation forward and then backward. */
        FORWARD_REVERSE, 
        /** Play animation continously in a loop. */
        LOOP, 
        /** Play specific frame range of animation. */
        FRAME_RANGE,
        /** Use specified EZScript functions for animation. */
        EZSCRIPT_FUNCTIONS;
    }

    /**
     * Possible ways to trigger animation play.
     */
    public enum AnimationTrigger {
        /** Left mouse click as a trigger. */
        LEFT_CLICK,
        /** Right mouse click as a trigger. */
        RIGHT_CLICK,
        /** Avatar proximity as a trigger. */
        PROXIMITY;
    }
    
    /** Current way to play animation. */
    private AnimationPlayType playType;
    
    /** Animation trigger type. */
    private AnimationTrigger trigger;
    
    /** Name of animation. */
    private String name;
    
    /** Proximity range in meters. */
    private float proximityRange;
    
    /** Command to plat animation in reverse. */
    private String reverseCommand;
    
    /** Command to start playing animation in a loop. */
    private String startLoop;
    
    /**
     * Command to stop playing animation loop.
     */
    private String stopLoop;
    
    /** Whether to take EZScript animations into account. */
    private boolean includeEZScript;
    
    /** Defines frame ranges for animation. */
    private SortedSet<FrameRange> ranges;
    
    /** Iterator over frame range. */
    private Iterator<FrameRange> rangeIterator;
    
    /** List of defined EZScript functions. */
    private List<EZScriptAnimationControl> scriptFunctions;
    
    /** Index of next script to invoke. */
    private int nextScriptIndex = 0;
    
    /**
     * Create new instance with default values. Animation play type is set to {@code PLAY_ONCE},
     * and trigger type is set to {@code LEFT_CLICK}.
     */
    public Animation(){
        playType = AnimationPlayType.PLAY_ONCE;
        trigger = AnimationTrigger.LEFT_CLICK;
        ranges = new TreeSet<FrameRange>();
        scriptFunctions = new ArrayList<EZScriptAnimationControl>();
        includeEZScript = true;
    }

    /**
     * Returns play type for animation.
     * 
     * @return play type
     */
    public AnimationPlayType getPlayType() {
        return playType;
    }

    /**
     * Set current play type of animation.
     * 
     * @param playType  play type to set
     */
    public void setPlayType(AnimationPlayType playType) {
        this.playType = playType;
    }

    /**
     * Set name for animation. Default is Collada animation name or, if it does
     * not exist, cell name.
     * 
     * @param name name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get animation name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Specify whether to include EZScript animation functions or not.
     * 
     * @param includeEZScript <code>true</code> if EZScript should be included, <code>false</code> otherwise
     */
    public void setIncludeEZScript(boolean includeEZScript) {
        this.includeEZScript = includeEZScript;
    }

    /**
     * Returns whether EZScript should be included or not.
     * 
     * @return <code>true</code> if EZScript should be included, <code>false</code> otherwise
     */
    public boolean isIncludeEZScript() {
        return includeEZScript;
    }

    /**
     * Sets desired animation trigger type.
     * 
     * @param trigger trigger
     */
    public void setTrigger(AnimationTrigger trigger) {
        this.trigger = trigger;
    }

    /**
     * Returns configured animation trigger type.
     * 
     * @return trigger type
     */
    public AnimationTrigger getTrigger() {
        return trigger;
    }

    /**
     * Set proximity range for animation.
     * 
     * @param range range in meters
     *
     */
    public void setProximityRange(float range) {
        this.proximityRange = range;
    }

    /**
     * Returns configured proximity trigger range.
     * 
     * @return proximity range in meters
     */
    public float getProximityRange() {
        return proximityRange;
    }

    /**
     * Set the name of reverse play command.
     * 
     * @param reverseCommand  reverse command name
     */
    public void setReverseCommand(String reverseCommand) {
        this.reverseCommand = reverseCommand;
    }

    /**
     * Returns the name of reverse play command.
     * 
     * @return command name
     */
    public String getReverseCommand() {
        return reverseCommand;
    }

    /**
     * Set the command to start playing in a loop.
     * 
     * @param startLoop command name
     */
    public void setStartLoop(String startLoop) {
        this.startLoop = startLoop;
    }

    /**
     * Returns command to start playing the loop.
     * 
     * @return command name
     */
    public String getStartLoop() {
        return startLoop;
    }

    /**
     * Set the command to stop plating the loop.
     * 
     * @param stopLoop command name
     */
    public void setStopLoop(String stopLoop) {
        this.stopLoop = stopLoop;
    }

    /**
     * Returns the command to stop playing the loop.
     * 
     * @return command name
     */
    public String getStopLoop() {
        return stopLoop;
    }
    
    /**
     * Adds new frame range to currently defined set.
     * 
     * @param range range to add
     */
    public void addFrameRange(FrameRange range){
        ranges.add(range);
    }

    /**
     * Set defined frame ranges for this animation.
     * 
     * @param ranges animation ranges
     */
    public void setRanges(SortedSet<FrameRange> ranges) {
        this.ranges = ranges;
    }

    /**
     * Get defined animation ranges.
     * 
     * @return animation ranges
     */
    public SortedSet<FrameRange> getRanges() {
        return ranges;
    }
    
    /**
     * Get next frame range to play.
     *
     * @return frame range
     */
    public FrameRange getNextFrameRange(){
        FrameRange fr = null;
        if(!ranges.isEmpty()){
            if(rangeIterator == null){
                rangeIterator = ranges.iterator();
                fr = rangeIterator.next();
            } else {
                if(rangeIterator.hasNext()){
                    fr = rangeIterator.next();
                } else {
                    // start over
                    rangeIterator = ranges.iterator();
                    fr = rangeIterator.next();
                }
            }
        }
        return fr;
    }

    /**
     * Set defined EZScript functions.
     * 
     * @param scriptFunctions  list of functions
     */
    public void setScriptFunctions(List<EZScriptAnimationControl> scriptFunctions) {
        this.scriptFunctions = scriptFunctions;
    }

    /**
     * Get defined EZScript functions.
     * 
     * @return  list of functions
     */
    public List<EZScriptAnimationControl> getScriptFunctions() {
        return scriptFunctions;
    }
    
    /**
     * Returns next EZScript function in sequence.
     * 
     * @return EZscript function to invoke
     */
    public String getNextScriptFunction(){
        String script = null;
        if(!scriptFunctions.isEmpty()){
            if(nextScriptIndex < scriptFunctions.size()){
                script = scriptFunctions.get(nextScriptIndex).getFunction();
                nextScriptIndex++;
            } else {
                nextScriptIndex = 0;
                script = scriptFunctions.get(nextScriptIndex).getFunction();
            }
        }
        return script;
    }
    
    /**
     * Checks if a given command exists. This method will check if any of the class command fields
     * matches the given command and return <code>true</code> if it does.
     * 
     * @param cmd command to check
     * @return <code>true</code> if command exists, <code>false</code> otherwise
     */
    public boolean commandExists(String cmd){
        boolean status = cmd.equals(name) || cmd.equals(reverseCommand)
                || cmd.equals(startLoop) || cmd.equals(stopLoop)
                || cmd.equals(AnimationConstants.DEFAULT_REVERSE_COMMAND)
                || cmd.equals(AnimationConstants.DEFAULT_START_COMMAND)
                || cmd.equals(AnimationConstants.DEFAULT_STOP_COMMAND);
        // examine frame range commands
        for(FrameRange fr : ranges){
            status |= cmd.equals(fr.getCommand());
        }
        // examine EZScript commans
        for(EZScriptAnimationControl ctrl : scriptFunctions){
            status |= cmd.equals(ctrl.getCommand());
        }
        return status;
    }
    
    /**
     * Verify that given command is to play frame range.
     * 
     * @param cmd command to check
     * @return <code>true</code> if command can be used to play range, <code>false</code> otherwise
     */
    public boolean isFrameRangePlayCommand(String cmd){
        boolean status = false;
        for(FrameRange fr : ranges){
            if(cmd.equals(fr.getCommand())){
                status = true;
                break;
            }
        }
        return status;
    }
    
    /**
     * Verifies that given command is to play EZScript animation.
     * 
     * @param cmd command to check
     * @return <code>true</code> if command can be used to play EZScript, <code>false</code> otherwise
     */
    public boolean isScriptCommand(String cmd){
        boolean status = false;
        for(EZScriptAnimationControl ctrl : scriptFunctions){
            if(cmd.equals(ctrl.getCommand())){
                status = true;
                break;
            }
        }
        return status;
    }
    
    /**
     * Returns EZScript function for a given command.
     * 
     * @param command command mapped to function
     * @return EZScript function
     */
    public String getScriptFunction(String command){
        String script = "";
        for(EZScriptAnimationControl ctrl: scriptFunctions){
            if(command.equals(ctrl.getCommand())){
                // append trailing semicolon
                script = ctrl.getFunction() + ";";
                break;
            }
        }
        return script;
    }
    
    /**
     * Returns frame range associated with given command.
     * 
     * @param cmd command name
     * @return  frame range for given command
     */
    public FrameRange getFrameRangeForCommand(String cmd){
        FrameRange range = null;
        for(FrameRange fr : ranges){
            if(cmd.equals(fr.getCommand())){
                range = fr;
                break;
            }
        }
        return range;
    }
    
}
