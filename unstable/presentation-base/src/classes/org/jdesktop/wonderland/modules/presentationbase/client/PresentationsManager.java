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

import com.jme.math.Vector3f;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationsManager {

    private static final Logger logger =
        Logger.getLogger(PresentationsManager.class.getName());


    private Set<PresentationCell> presentationCells = new HashSet<PresentationCell>();

    private static PresentationsManager manager = null;

    public static PresentationsManager getPresentationsManager() {
        
        if(manager==null)
            manager = new PresentationsManager();
        
        return manager;
    }

    private PresentationsManager() {
        
    }

    public void addPresentationCell(PresentationCell pc) {
        presentationCells.add(pc);
    }

    public Set<PresentationCell> getPresentationsCells() {
        return new HashSet<PresentationCell>(presentationCells);
    }

    void removePresentationCell(PresentationCell pc) {
        presentationCells.remove(pc);
    }

    /**
     * Passthrough method that looks for the first platform that contains the
     * specified point and returns it. If there are no presentation cells,
     * or those presentation cells don't have platforms that contain pos.
     * 
     * @param pos
     * @return
     */
    public Cell getParentCellByPosition(Vector3f pos) {
        for(PresentationCell pc : this.presentationCells) {
            
            logger.warning("checking presentationcell: " + pc);
            Cell parent = pc.getParentCellByPosition(pos);

            if(parent != null)
                return parent;
        }

        // If we get through the whole search without having returned a platform,
        // then just return null.
        return null;
    }
}
