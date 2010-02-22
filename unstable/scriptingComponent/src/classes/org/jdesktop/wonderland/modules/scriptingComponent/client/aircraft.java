/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

/**
 *
 * @author morrisford
 */
public class aircraft extends vehicle
    {
    public aircraft(ScriptingComponent SC)
        {
        super(SC);
        }
    
    public void configureAircraft(String command, float value1)
        {
        if(command.equals("totalRollIn"))
            {
            totalRollIn = value1;
            System.err.println("totalRollIn set to + totalRollIn");
            }
        }

    }
