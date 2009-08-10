/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.math.Vector2f;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.marbleous.client.cell.MarbleousCell;

/*********************************************
 * MarbleousWindowRun: The run window.
 * @author deronj@dev.java.net
 */

public class MarbleousWindowRun
    extends WindowSwing 
{
    /* The cell. */
    private MarbleousCell cell;

    /** The Swing panels. */
    //private MarbleousRunPanel runPanel;

    public MarbleousWindowRun (MarbleousCell cell, App2D app, int width, int height, boolean decorated,
                               Vector2f pixelScale) {
        super(app, Window2D.Type.PRIMARY, width, height, decorated, pixelScale);
        this.cell = cell;

        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    //controlPanel = new JothControlPanel();
                    //controlPanel.setContainer(JothWindow.this);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create run window");
        }

	// Parent to Wonderland main window for proper focus handling 
       	//JmeClientMain.getFrame().getCanvas3DPanel().add(controlPanel);

    	//setComponent(controlPanel);
        setTitle("Run Controls");
    }


    /** Control the visibility of the window. */
    public void setVisible (boolean visible) {
        if (visible) {
            setVisibleApp(true);
            setVisibleUser(cell, true);
        } else {
            setVisibleApp(false);
        }
    }
}
