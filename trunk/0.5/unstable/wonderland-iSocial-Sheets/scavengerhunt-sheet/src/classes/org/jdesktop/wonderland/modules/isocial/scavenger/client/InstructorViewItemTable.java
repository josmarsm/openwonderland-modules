/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntResult;

/**
 *
 * @author Vladimir Djurovic
 */


public class InstructorViewItemTable extends JTable {
    
    private static final int ICON_COLUMN_WIDTH = 30;
    private static final int ROW_HEIGHT = 25;
    
    // table column indexes
    private static final int HINTS_COLUMN_INDEX = 0;
    private static final int FOUND_COLUMN_INDEX = 1;
    private static final int ORDER_COLUMN_INDEX = 2;
    private static final int ITEM_NAME_COLUMN_INDEX = 3;
    private static final int ITEM_ID_COLUMN_INDEX = 4;
    
    private Map<String, Set<String>> usersFoundItemMap;
    private Map<String, Integer> totalHintsMap;
    private Map<String, Map<String, Integer>> hintsPerUserMap;
    
    /** Flag to indicate that  table data is currently being updated. */
    private boolean updatingTable = false;

    public InstructorViewItemTable(List<ScavengerHuntItem> items) {
        usersFoundItemMap = new HashMap<String, Set<String>>();
        totalHintsMap = new HashMap<String, Integer>();
        hintsPerUserMap = new HashMap<String, Map<String, Integer>>();
        
        setModel(new InstructorViewTableModel(items));
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(false);
        setShowGrid(false);
        setRowHeight(ROW_HEIGHT);
        setForeground(Color.BLUE);
        // remove ID column
        removeColumn(getColumnModel().getColumn(4));
        TableColumnModel colModel = getColumnModel();
        // set width for icon 
        for (int i = 0; i < 3; i++) {
            colModel.getColumn(i).setMinWidth(ICON_COLUMN_WIDTH);
            colModel.getColumn(i).setMaxWidth(ICON_COLUMN_WIDTH);
            colModel.getColumn(i).setPreferredWidth(ICON_COLUMN_WIDTH);
        }
       
        
        getColumn("1").setHeaderRenderer(new HeaderCellRenderer(getClass().getResource(ScavengerHuntConstants.HINT_1_ICON_PATH)));
        getColumn("1").setCellRenderer(new InstructorCellRenderer());
        getColumn("2").setHeaderRenderer(new HeaderCellRenderer(getClass().getResource(ScavengerHuntConstants.USER_ICON_PATH)));
        getColumn("2").setCellRenderer(new InstructorCellRenderer());
        getColumn("3").setHeaderRenderer(new HeaderCellRenderer(getClass().getResource(ScavengerHuntConstants.ORDER_ICON_PATH)));
        // set special cell renderer for up/down icon cell
        getColumn("3").setCellRenderer(new DirectionCellRenderer());
        getColumnModel().getColumn(ITEM_NAME_COLUMN_INDEX).setCellRenderer(new InstructorCellRenderer());
        
        addMouseListener(new TableMouseListener());
    }
    
    /**
     * Removes this item from table.
     * 
     * @param item  item to remove
     */
    public void removeItem(ScavengerHuntItem item){
        int count = getModel().getRowCount();
        for(int i = 0;i < count;i++){
            String id = (String)getModel().getValueAt(i, InstructorViewTableModel.ID_COLUMN_INDEX);
            if(item.getCellId().equals(id)){
                ((InstructorViewTableModel)getModel()).removeRow(i);
                return;
            }
        }
    }
    
    /**
     * Updates existing item in table.
     * 
     * @param item  new item
     */
    public void updateItem(ScavengerHuntItem item){
        int count = getModel().getRowCount();
        for(int i = 0;i < count;i++){
            String id = (String)getModel().getValueAt(i, InstructorViewTableModel.ID_COLUMN_INDEX);
            if(item.getCellId().equals(id)){
                ((InstructorViewTableModel)getModel()).setValueAt(item.getName(), i, InstructorViewTableModel.NAME_COLUMN_INDEX);
                return;
            }
        }
    }


    /**
     * Disable cell editing.
     * 
     * @param row 
     * @param column
     * @return <code>true</code> if the cell can be edited, <code>false</code> otherwise
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
    

    private class HeaderCellRenderer extends DefaultTableCellRenderer {

        private ImageIcon icon;

        public HeaderCellRenderer(URL iconUrl) {
            icon = new ImageIcon(iconUrl);
        }

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int i, int i1) {
            Component comp = super.getTableCellRendererComponent(jtable, o, bln, bln1, i, i1);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setIcon(icon);
                ((JLabel) comp).setText(null);
            }
            return comp;
        }
    }
    
    private class DirectionCellRenderer implements TableCellRenderer{
        
        private JPanel panel;

        public DirectionCellRenderer() {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel up = new JLabel
                        (new ImageIcon(getClass().getResource(ScavengerHuntConstants.ARROW_UP_ICON_PATH)), 
                        SwingConstants.CENTER);
            JLabel down = new JLabel(
                        new ImageIcon(getClass().getResource(ScavengerHuntConstants.ARROW_DOWN_ICON_PATH)), 
                    SwingConstants.CENTER);
            panel.add(up);
            panel.add(down);
        }
        
        

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return panel;
        }
    }
    
    private class InstructorCellRenderer implements TableCellRenderer{

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = (value != null) ? value.toString() : "";
            JLabel label = new JLabel(text);
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLUE);
            return label;
        }
        
    }
    
    /**
     * Updates instructor view table with data from result. This method will coalesce processing, ie. once called, 
     * it will ignore any future calls until current processing is done.
     * 
     * @param results 
     */
    public void updateTableFromResults(final Collection<Result> results) {
        if (!updatingTable) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    // set blocking flag
                    updatingTable = true;
                    int count = getModel().getRowCount();
                    String itemName = null;
                    for (int i = 0; i < count; i++) {
                        itemName = getModel().getValueAt(i, ITEM_NAME_COLUMN_INDEX).toString();
                        Set<String> users = new HashSet<String>();
                        // total number of hints shown for this item
                        int totalHints = 0;
                        Map<String, Integer> hintsPerUser = new HashMap<String, Integer>();
                        for (Result r : results) {
                            ScavengerHuntResult resultDetail = ((ScavengerHuntResult) r.getDetails());
                            if (resultDetail.isItemFound(itemName)) {
                                users.add(r.getCreator());
                            }
                            totalHints += resultDetail.getHintCountForItem(itemName);
                            if(resultDetail.getHintCountForItem(itemName) > 0){
                                hintsPerUser.put(r.getCreator(), resultDetail.getHintCountForItem(itemName));
                            }
                            
                        }
                        getModel().setValueAt(users.size(), i, FOUND_COLUMN_INDEX);
                        getModel().setValueAt(totalHints, i, HINTS_COLUMN_INDEX);
                        usersFoundItemMap.put(itemName, users);
                        totalHintsMap.put(itemName, totalHints);
                        hintsPerUserMap.put(itemName, hintsPerUser);
                    }
                    updatingTable = false;
                }
            });
        }
    }
    
    private class TableMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = getSelectedRow();
            if(row < 0 || row >= getRowCount()){
                return;
            }
            int col = getSelectedColumn();
            JPopupMenu popup = new JPopupMenu();
            String itemName = (String) getValueAt(row, ITEM_NAME_COLUMN_INDEX);
            switch (col) {
                case FOUND_COLUMN_INDEX:
                    // display users who found the item
                    Set<String> users = usersFoundItemMap.get(itemName);
                    for (String u : users) {
                        popup.add(new JMenuItem(u));
                    }
                    popup.show(InstructorViewItemTable.this, e.getX(), e.getY());
                    break;
                case HINTS_COLUMN_INDEX:
                    // display hints menu
                    for (Map.Entry<String, Integer> entry : hintsPerUserMap.get(itemName).entrySet()) {
                        popup.add(new JMenuItem(entry.getValue().toString() + " " + entry.getKey()));
                    }
                    popup.show(InstructorViewItemTable.this, e.getX(), e.getY());
                    break;
                case ORDER_COLUMN_INDEX:
                    // find out if user clicked up or down arrow
                    Rectangle cellRect = getCellRect(row, col, false);
                    if(e.getY() <= cellRect.getY() + cellRect.getHeight()/2){
                        ((InstructorViewTableModel)getModel()).moveRow(row, row, (row > 0) ? row - 1 : 0);
                    } else {
                        ((InstructorViewTableModel)getModel()).moveRow(row, row, (row < getRowCount() - 1) ? row + 1 : row);
                    }
                    break;
                case ITEM_NAME_COLUMN_INDEX:
                    // move avatar to item
                    ScavengerHuntUtils.moveAvatarToItem((String) getModel().getValueAt(row, ITEM_ID_COLUMN_INDEX));
                    break;
            }
            
        }
    }
}
