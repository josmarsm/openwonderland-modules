/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

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
package org.jdesktop.wonderland.modules.whiteboard.client;

/**
 *
 * @author jbarratt
 * @author nsimpson
 * @author Abhishek Upadhyay
 */
public interface WhiteboardCellMenuListener {

    public void remove();

    public void openDoc();

    public void selector();

    public void line();

    public void rect(boolean filled);

    public void ellipse(boolean filled);

    public void black();

    public void white();

    public void red();

    public void green();

    public void blue();

    public void zoomIn();

    public void zoomOut();

    public void sync();

    public void unsync();

    public void toggleHUD();
    
    public void backgroundImage();
    
    public void changeFont();
    
    public void image();
    
    public void newText();
    
    public void colorChooser();
    
    public void sendToBack();
    
    public void bringToFront();
}
