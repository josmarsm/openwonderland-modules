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

package org.jdesktop.wonderland.modules.presentationbase.client;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Utility interface so presentation-base doesn't have to depend on the PDF
 * spreading module, but can still call into tha object to get data out of it.
 *
 * Provides methods that tell presentation base about the PDF's layout to figure
 * out where to put cameras, platforms, etc.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public interface SlidesCell {
    
    public CellTransform getTransform();
    
    public int getNumSlides();
    
    public float getInterslideSpacing();

    public String getCreatorName();

    public CellID getCellID();
}
