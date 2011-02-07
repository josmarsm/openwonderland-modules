/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A behavior is meant to provide a wrapper for cell-user interaction events.
 * Further, an update method is provided to be run 1 per frame per behavior.
 *
 * @author JagWire
 */

public class Behavior {

    private Runnable onClick;
    private Runnable onMouseEnter;
    private Runnable onMouseExit;
    private Runnable onApproach;
    private Runnable onLeave;
    private Map<String, Runnable> events;
    private Map<String, Runnable> keyPresses;
    private String behaviorName;
    private String behaviorDescription;
    private String category;
    private Runnable update; //let the script initialize this one.


    public Behavior(String name, String description) {
        this.behaviorName = name;
        this.behaviorDescription = description;

        events = new HashMap();
        keyPresses = new HashMap<String, Runnable>();
    }
    
    public Runnable onEvent(String key) {
        return events.get(key);
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

    public Runnable onUpdate() {
        return update;
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

    public String getCategory() {
        return category;
    }
}
