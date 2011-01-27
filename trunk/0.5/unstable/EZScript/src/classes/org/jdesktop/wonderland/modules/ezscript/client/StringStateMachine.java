/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.HashMap;
import java.util.Map;

/**
 * DESIGN TEMPLATE:
 *
 * var x = new StringStateMachine();
 *
 * function one() {
 * x.state = "two";
 * }
 *
 * function two() {
 * x.state = "three";
 * }
 *
 * function three() {
 * x.state = "one";
 * }
 *
 *
 * x.addNewState("one", one);
 * x.addNewState("two", two);
 * x.addNewState("three", three);
 *
 * while(true) {
 *  x.fireCurrentState();
 * }
 *
 * Shortcomings: linear progression of states; what if more than one option
 * is listed as a "next" progression.
 */

/**
 *
 * @author JagWire
 */
public class StringStateMachine {

    private Map<String, Runnable> states;
    private String state;
    public StringStateMachine() {
        states = new HashMap<String, Runnable>();
    }

    public StringStateMachine(int stateAmount) {

    }

    public void addNewState(String name, Runnable r) {
        states.put(name, r);
    }

    public void fireState(String name) {
        states.get(name).run();
    }

    /**
     * Continuously calling this method in a loop will cycle through the machine
     * as designed by the developer.
     */
    public void fireCurrentState() {
        states.get(state).run();
    }

    public String getCurrentState() {
        return state;
    }

    public void setCurrentState(String state) {
        this.state = state;
    }
}
