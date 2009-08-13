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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.wonderland.modules.marbleous.common.BumpTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.TCBKeyFrame;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;

/**
 * TableModel that adapts to a list of knots
 * @author Bernard Horan
 */
public class KnotTableModel extends AbstractTableModel{
    private static final String[] COLUMN_NAMES = {"Knot", "Position", "Rotation", "Tension", "Bias", "Continuity"};
    private static final String[] FIELD_NAMES = {"knot", "position", "quat", "tension", "bias", "continuity"};
    private TrackSegment segment;

    public KnotTableModel(TrackSegment aSegment) {
        setSegment(aSegment);
    }

    public KnotTableModel() {
        
    }

    public void setSegment(TrackSegment aSegment) {
        segment = aSegment;        
    }

    public int getRowCount() {
        return segment.getKeyFrames().length;
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }


    public Object getValueAt(int row, int column) {
        TCBKeyFrame keyFrame = segment.getKeyFrames()[row];
        String fieldName = FIELD_NAMES[column];
        Field keyFrameField;
        try {
            keyFrameField = keyFrame.getClass().getField(fieldName);
            return keyFrameField.get(keyFrame);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        TCBKeyFrame keyFrame = segment.getKeyFrames()[row];
        String fieldName = FIELD_NAMES[col];
        Field keyFrameField;
        try {
            keyFrameField = keyFrame.getClass().getField(fieldName);
            keyFrameField.set(keyFrame, value);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(KnotTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        fireTableCellUpdated(row, col);
        }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        //Edit any cell except the first column
        return columnIndex != 0;
    }

    public TrackSegment getSegment() {
        return segment;
    }

    //TODO remove after testing
    void setDefaultValues() {
        setSegment(new BumpTrackSegmentType().createSegment());
    }

}
