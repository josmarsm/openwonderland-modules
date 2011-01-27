/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author JagWire
 */
public class IntStateMachine {
    Map<Integer, Runnable> states;
    Queue<Runnable> pending;
    boolean locked = false;
    int state;
    public IntStateMachine() {
        states = new HashMap();
        pending = new LinkedList<Runnable>();
        
        state = 0;
    }

    public void addNewState(int i, Runnable r) {
       if(!states.containsKey(Integer.valueOf(i))) {
           states.put(Integer.valueOf(i), r);
       }
    }
    /**
     * Linear model.
     */
    public void fireCurrentState() {
        if(locked) {
            return;
        }
        states.get(Integer.valueOf(state)).run();
        state += 1;
    }

    public void fireState(int state) {
        if(locked && state != this.state) {
            //enqueue next method if possible
            pending.offer(states.get(Integer.valueOf(state)));
            return;
        } else if(state != this.state) {
            this.state = state;
            fireCurrentState();
        }

    }

    public int getCurrentState() {
        return state;
    }

    public void setCurrentState(int state) {
           this.state = state;
    }

    public void unlock() {
        while(!pending.isEmpty()) {
            Runnable r = pending.poll();
            if(r != null) {
                r.run();
            }
        }
        locked = false;
    }

    public void lock() {
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }

    public void addTransition(Runnable r) {
        pending.offer(r);
    }
}
