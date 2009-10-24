/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import java.util.Timer;
import java.util.TimerTask;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author morrisford
 */
public class ControllerInterface
    {
    private ControllerEnvironment ce;
    private Controller            theController;
    private Component             component12;
    private Component             component11;
    private Component             component10;
    private boolean               keepRunning = true;

    public ControllerInterface(String controllerName)
        {
        ce = ControllerEnvironment.getDefaultEnvironment();
        System.out.println("Controller Env = "+ce.toString());


        Controller[] ca = ce.getControllers();
        for(int i = 0; i < ca.length; i++)
            {
//            System.out.println(ca[i].getName());
//            System.out.println("Type: "+ca[i].getType().toString());
            net.java.games.input.Component[] components = ca[i].getComponents();
            System.out.println("Looking for controller " + controllerName + " - checking " + ca[i].getName());
            if(ca[i].getName().indexOf(controllerName) > 0)
//            if(ca[i].getName().equals(controllerName))
                {
                System.out.println("Found it");
                theController = ca[i];
                component12 = components[12];
                component11 = components[11];
                component10 = components[10];
                }
            }
        }

    public float getComponent10()
        {
        theController.poll();
        return component10.getPollData();
        }
    public float getComponent11()
        {
        theController.poll();
        return component11.getPollData();
        }
    public float getComponent12()
        {
        theController.poll();
        return component12.getPollData();
        }
    }
