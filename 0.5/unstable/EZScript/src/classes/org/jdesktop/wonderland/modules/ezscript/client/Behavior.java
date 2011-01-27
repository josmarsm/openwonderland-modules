/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.HashMap;
import java.util.Map;

/**
 *  A collection of BehaviorFields.
 * @author JagWire
 */

public class Behavior {

    private Runnable onClick;
    private Runnable onMouseEnter;
    private Runnable onMouseExit;
    private Runnable onApproach;
    private Runnable onLeave;
    private Map<String, Runnable> keyPresses;
    private String behaviorName;
    private String behaviorDescription;


    public Behavior(String name, String description) {
        this.behaviorName = name;
        this.behaviorDescription = description;
        keyPresses = new HashMap<String, Runnable>();
    }
    

    public Runnable onKeyPress(String key) {
        return keyPresses.get(key);
    }

    public Runnable onClick() {
        return onClick;
    }

    public Runnable onMouseEnter() {
        return onMouseEnter;
    }

    public Runnable onMouseExit() {
        return onMouseExit;
    }

    public Runnable onApproach() {
        return onApproach;
    }

    public Runnable onLeave() {
        return onLeave;
    }

    public String getName() {
        return behaviorName;
    }

    public Map getKeyPresses() {
        return keyPresses;
    }

    public String getDescription() {
        return behaviorDescription;
    }
}
