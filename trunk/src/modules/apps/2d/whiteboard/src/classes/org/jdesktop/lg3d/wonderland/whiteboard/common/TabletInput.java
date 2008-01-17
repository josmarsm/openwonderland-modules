/**
 * Project Looking Glass
 *
 * $RCSfile: TabletInput.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2 $
 * $Date: 2007/10/15 23:49:18 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;

/**
 *
 * @author paulby
 */
public class TabletInput {
    
    private Controller tablet = null;
    
    private Component xComponent = null;
    private Component yComponent = null;
    private Component pressureComponent = null;
    private float x=Float.POSITIVE_INFINITY;
    private float y=Float.POSITIVE_INFINITY;
    private float pressure=Float.POSITIVE_INFINITY;
    
    private WhiteboardDrawingSurface drawingSurface;

    public TabletInput(WhiteboardDrawingSurface drawingSurface) {
        this.drawingSurface = drawingSurface;
        ControllerEnvironment ce =
		ControllerEnvironment.getDefaultEnvironment();

	// retrieve the available controllers
	Controller[] controllers = ce.getControllers();  
        
        for(Controller control : controllers) {
            System.out.println(control.getName() +" "+control.getName().indexOf("Wacom"));
            
            if (control.getName().indexOf("Wacom")!=-1) {
                System.out.println("Found tablet");
                tablet = control;
                Component[] components = control.getComponents();
                for(Component subC : components) {
                    System.out.println("Component "+subC.getName());
                    if (subC.getName().equals("x"))
                        xComponent = subC;
                    else if (subC.getName().equals("y"))
                        yComponent = subC;
                    else if (subC.getName().equals("slider") && pressureComponent==null)
                        pressureComponent = subC;
                }
                
                new Timer(10,new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        tablet.poll();
                        float newX = xComponent.getPollData();
                        float newY = yComponent.getPollData();
                        float newPressure = pressureComponent.getPollData();
                        
                        if (newX!=x || newY!=y || newPressure!=pressure)
                            updatePen(newX, newY, newPressure);
                        
                    }
                }).start();
            }
        }
    }
    
    private void updatePen(float x, float y, float pressure) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        
        if (pressure>0) {
            // Draw
            drawingSurface.penDrag(x, y);
        } else {
            // Move
            drawingSurface.penMove(x, y);
        }
    }

}
