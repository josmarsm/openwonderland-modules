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
package org.jdesktop.wonderland.modules.mediaboard.client;

/**
 *
 * @author jbarratt
 * @author nsimpson
 */
public interface WhiteboardCellMenuListener {

    public void newDoc();

    public void openDoc();

    public void selector();

    public void line();

    public void rect();

    public void ellipse();

    public void text();

    public void fill();

    public void draw();

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

    public void save();

    public void open();

    public void picture();
}
