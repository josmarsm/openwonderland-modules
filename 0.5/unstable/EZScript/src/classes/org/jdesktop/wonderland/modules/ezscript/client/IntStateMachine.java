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
     * We're going to assume the runnable will release the lock when it's done
     */
    public void fireCurrentState() {
    
        states.get(Integer.valueOf(state)).run();
    }

    public void fireState(int state) {
        states.get(Integer.valueOf(state)).run();
    }

    public int getCurrentState() {
        return state;
    }

    public void setCurrentState(int state) {
           this.state = state;
    }

    public void addTransition(Runnable r) {
        pending.offer(r);
    }
}
