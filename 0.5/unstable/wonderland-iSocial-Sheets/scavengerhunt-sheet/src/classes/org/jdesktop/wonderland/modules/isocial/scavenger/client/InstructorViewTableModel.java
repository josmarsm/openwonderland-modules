/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;

/** Table model for instructor view table.
 *
 * @author Vladimir Djurovic
 */
public class InstructorViewTableModel extends DefaultTableModel {
    
    public static final int FOUND_COLUMN_INDEX = 1;
    public static final int POSITION_COLUMN_INDEX = 2;
    public static final int NAME_COLUMN_INDEX = 3;
    public static final int ID_COLUMN_INDEX = 4;

    public InstructorViewTableModel(List<ScavengerHuntItem> items) {
        addColumn("1");
        addColumn("2");
        addColumn("3");
        addColumn("Item (Click to view)");
        addColumn("id");

        for (ScavengerHuntItem item : items) {
            addRow(new Object[]{0, 0, null, item.getName(), item.getCellId()});
        }
        
    }
}
