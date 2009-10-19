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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.SegmentSettings;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegmentType;

/**
 *
 * @author paulby
 */
public abstract class SegmentEditor extends JPanel {

    private TrackCell cell;
    protected TrackSegment segment;


    /**
     * Create and initialize the editor, return null if there is none
     * @param segment
     * @return
     */
    public static SegmentEditor createSegmentEditor(TrackCell cell, TrackSegment segment) {
        SegmentSettings settings = segment.getSegmentSettings();
        if (settings!=null) {
             try {
                Class<SegmentEditor> segEditorClazz = (Class<SegmentEditor>) Class.forName(settings.getEditorClassname());
                SegmentEditor editor = segEditorClazz.newInstance();
                editor.setSegment(segment);
                editor.setCell(cell);
                return editor;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TrackSegment.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(TrackSegment.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(TrackSegment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SegmentEditor noEditor = new NoSegmentEditor();

        return noEditor;
    }

    /**
     * Set the segment to edit
     * @param segment
     */
    public abstract void setSegment(TrackSegment segment);

    private void setCell(TrackCell cell) {
        this.cell = cell;
    }

    protected void notifySegmentChanged() {
        cell.modifySegment(segment);
    }
}
