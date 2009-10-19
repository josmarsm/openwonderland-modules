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

package org.jdesktop.wonderland.modules.metadata.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;

/**
 *
 * A tabbed view of Metadata. Requests a list of currently registered Metadata
 * types from client MetadataPlugin. Each type has its own tab, where that
 * type's fields are presented in a table. Add Metadata to the dipslay using
 * addMetadata.
 *
 * ListSelectionListener's for each table (ex: for a remove button that activates
 * when an entry is selected) are aded using registersListSelectionListener.
 *
 * @author mabonner
 */
public class MetadataTypesTable extends JTabbedPane {
  // used to map pieces of metadata to their appropriate table
  private HashMap<Class, JTable> metatypeMap = new HashMap<Class, JTable>();
  private static Logger logger = Logger.getLogger(MetadataTypesTable.class.getName());
  private ArrayList<ListSelectionListener> tableSelectionListeners = new ArrayList<ListSelectionListener>();
  private ArrayList<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();
  /**
   * MetadataID's to highlight (by default none, set by using addMetadata)
   */
  private Set<MetadataID> highlights = new HashSet<MetadataID>();
//  private HashMap<JTable, Class> defaultMetadata = new HashMap<JTable, Class>();

  /**
   * set the metadata table to allow editing of table cells always, never, or check
   * if the metadata type the cell is part of allows editing. 
   */
  public enum AllowEdits { ALWAYS, NEVER, CHECK }

  /**
   * policy for determining whether table cells are user-editable
   */
  private AllowEdits enforceEditable;

  // colors for table selections
  /** orange-red highlight color*/
  private static Color highlightColor = new Color(255, 170, 150);
  /** neutral blue select color */
  private static Color selectedColor = new Color(85, 110, 185);




  /**
   *
   *
   */
  public MetadataTypesTable() {
    // this panel is dynamically populated
    enforceEditable = AllowEdits.CHECK;
    updateTypeTabs();
  }

  /**
   * set the metadata table to allow editing of table cells always, never, or check
   * if the metadata type the cell is part of allows editing.
   *
   * turned off, for example, in the search panel to allow user to edit any attribute
   * in their search query.
   */
  public void setTableCellsEditable(AllowEdits ae) {
    enforceEditable = ae;
  }

  

  /**
   * Clear all tabs from table, request the current list of Metadata types from
   * the client plugin, and rebuild each tab.
   */
  private void updateTypeTabs() {
//    Iterator<Class> iterator = MetadataPlugin.getMetadataTypes().iterator();
    Iterator<Metadata> iterator = MetadataClientUtils.getTypesIterator();
    removeAll();
    metatypeMap.clear();
    while(iterator.hasNext()){
      Metadata type = iterator.next();
        // create a new table for this type
        JTable typeTable = new JTable(new MetadataTableModel(type));
        // set renderer to alternate row colors and highlight hits
        typeTable.setDefaultRenderer(String.class, new CustomTableCellRenderer());
//        defaultMetadata.put(typeTable, clazz);

        // listeners
        for(ListSelectionListener l: tableSelectionListeners){
          logger.log(Level.INFO,"adding selection listener");
          typeTable.getSelectionModel().addListSelectionListener(l);
        }
        for(TableModelListener l: tableModelListeners){
          logger.log(Level.INFO,"adding model listener");
          typeTable.getModel().addTableModelListener(l);
        }

        typeTable.setMinimumSize(new Dimension(21, 21));
        addTab(type.simpleName(), new JScrollPane(typeTable));
        typeTable.setFillsViewportHeight(true);
        metatypeMap.put(type.getClass(), typeTable);
        logger.log(Level.INFO,"adding tab for type:" + type.getClass().getName());
    }
    repaint();
  }

  /**
   * debugging
   * @return total number of metadata objects in this table
   */
  public int getMetadataCount(){
    int total = 0;
    for(Component c : getComponents() ){
      JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
      JTable tab = (JTable) vp.getView();
      total += tab.getRowCount();
    }
    return total;
  }



  /**
   * Erase all entries on each tab.
   */
  public void clearTabs(){
    logger.log(Level.INFO,"[MTT] clearing tabs");
    int idx = 0;
    for(Component c : getComponents() ){
      JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
      JTable tab = (JTable) vp.getView();
      logger.log(Level.INFO,"[MTT]name at idx is" + getTitleAt(idx++));
      MetadataTableModel mod = (MetadataTableModel) getCurrentTable().getModel();
      mod.removeAllRows();
    }
    updateTypeTabs();
  }

  /**
   * Add Metadata objects to table. Each object will be mapped to the
   * appropriate tab. Whether each field is displayed or editable is defined
   * in the Metadata type.
   *
   * The tabs are not cleared, and no entries are overwriten. The passed in
   * Metadata is simply added.
   *
   * Clears list of MetadataID's to highlight, will highlight none.
   *
   * @param newMetadata The collection of Metadata to add.
   */
  public void addMetadata(Collection<Metadata> newMetadata){
    for(Metadata m : newMetadata)
    {
        // match to appropriate model
        JTable table = metatypeMap.get(m.getClass());
        if(table == null){
            logger.severe("Unrecognized Metadata type \"" +
                                m.getClass().getName() + "\" " + m.getClass());
            continue;
        }
//        logger.info("metadata: " + m + " class: " + m.getClass().getName() + " type: " + m.simpleName() + "table: " + t);
        MetadataTableModel mod = (MetadataTableModel) table.getModel();
        logger.info("metadata added is: " + m + " with id " + m.getID());
        
        mod.addRow(m);
    }     
  }

  /**
   * Add Metadata objects to table. Each object will be mapped to the
   * appropriate tab. Whether each field is displayed or editable is defined
   * in the Metadata type.
   *
   * The tabs are not cleared, and no entries are overwriten. The passed in
   * Metadata is simply added.
   *
   * Replaces old list of MetadataID's to highlight.
   *
   * @param newMetadata The collection of Metadata to add.
   * @param highlight set of MetadataID's to highlight
   */
  public void addMetadata(Collection<Metadata> newMetadata, Set<MetadataID> newHighlights){
    for(Metadata m : newMetadata)
    {
        // match to appropriate model
        JTable table = metatypeMap.get(m.getClass());
        if(table == null){
            logger.severe("Unrecognized Metadata type \"" +
                                m.getClass().getName() + "\" " + m.getClass());
            continue;
        }
//        logger.info("metadata: " + m + " class: " + m.getClass().getName() + " type: " + m.simpleName() + "table: " + t);
        MetadataTableModel mod = (MetadataTableModel) table.getModel();
        logger.info("metadata added is: " + m + " with id " + m.getID());

        mod.addRow(m);
    }

    highlights = newHighlights;
  }

  /**
   * helper function, creates blank new metadata object of the same type as the
   * current tab.
   * @return
   */
  private Metadata createNewMetadata(){
    JTable tab = getCurrentTable();
    MetadataTableModel mod = (MetadataTableModel) tab.getModel();
    Metadata res = null;
    try {
      res = (Metadata) mod.getMetadataClass().newInstance();
    } catch (InstantiationException ex) {
      Logger.getLogger(MetadataTypesTable.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(MetadataTypesTable.class.getName()).log(Level.SEVERE, null, ex);
    }
    return res;
  }
  /**
   * Add a new (mostly) blank row/Metadata to the currently selected tab. Any fields
   * that have a default value will be filled in. Editable/not editable will be
   * enforced.
   *
   * Default, non-user editable fields like Creator and Created will be filled.
   *
   */
  public void createNewDefaultMetadataOnCurrentTab(){
    Metadata meta = createNewMetadata();
    // prepare with defaults
    meta.initByClient(LoginManager.getPrimary().getPrimarySession().getUserID());

    MetadataTableModel mod = (MetadataTableModel) getCurrentTable().getModel();
    mod.addRow(meta);
  }

  /**
   * Add a new completely blank row/Metadata to the currently selected tab. All columns
   * will be editable.
   *
   * Default, non-user editable fields like Creator and Created will be filled.
   *
   */
  public void createNewBlankMetadataOnCurrentTab(){
    Metadata meta = createNewMetadata();

    MetadataTableModel mod = (MetadataTableModel) getCurrentTable().getModel();
    mod.addRow(meta);
  }

  /**
   * Get the currently selected/viewed tab's table.
   * @return
   */
  public JTable getCurrentTable(){
      JViewport vp =  ((JScrollPane) getSelectedComponent()).getViewport();
      return (JTable) vp.getView();
  }

  /**
   * Get the piece of metadata currently selected in the current table
   * @return the selected metadata
   */
  public Metadata getCurrentlySelectedMetadata() {
    JTable tab = getCurrentTable();
    int curRow = tab.getSelectedRow();
    MetadataTableModel mod = (MetadataTableModel) tab.getModel();
    return mod.getMetadataFromRow(curRow);
  }

  /**
   * Returns the list of Metadata from the tab at index idx.
   * @param idx
   */
    public ArrayList<Metadata> getMetadataFromTab(int idx) throws Exception {
        // some fun downcasting to get to the table
        JScrollPane sp = (JScrollPane) getComponent(idx);
        JViewport vp =  (JViewport) sp.getViewport();
        JTable tab = (JTable) vp.getView();
        
        if(tab == null){
          throw new Exception("no tab found at index " + idx + ", tab count is "
                            + getComponentCount());
        }
        MetadataTableModel mod = (MetadataTableModel) tab.getModel();
        return mod.getMetadata();
    }

  public void removeCurrentlySelectedRow() {
    JTable tab = getCurrentTable();
    int curRow = tab.getSelectedRow();
    MetadataTableModel mod = (MetadataTableModel) tab.getModel();
    mod.removeRow(curRow);
  }

  /**
   * debugging
   */
  private void printMetatypeMap(){
      for(Entry<Class, JTable> e : metatypeMap.entrySet()){
            logger.info("Key, Val: " + e.getKey() + ", " + e.getValue());
      }
  }



 /**
  * Each Metadata type gets its own tab and table, backed by an instance of
  * this class. MetadataTableModel may also check which fields should be
  * displayed or editable for this Metadata type.
  *
  *
  */
  class MetadataTableModel extends AbstractTableModel{
    private HashMap<Integer, String> columnNames = new HashMap<Integer, String>();
    private ArrayList<Metadata> metadata = new ArrayList<Metadata>();
    private HashMap<Point, Boolean> editable = new HashMap<Point, Boolean>(); // point to represent row/col
    /**
     * can be used to get new instances of metadata for this model
     */
    private Class metadataClass;

    public MetadataTableModel(Metadata type) {
//       logger.fine("MetadataTableModel type: " + type.simpleName());

       int colCount = 0;
       for(Entry<String, MetadataValue> e : type.getAttributes()){
//            logger.fine("NEW ENTRY: " + e.getKey());
            if(e.getValue().displayInProperties){
                columnNames.put(colCount, e.getKey());
                colCount+=1;
            }

       }


       metadataClass = type.getClass();

    }

    public Class getMetadataClass(){
      return metadataClass;
    }

    public void addRow(Metadata m) {
        logger.info("add row in model");
//        logger.info("before adding, model size:" + metadata.size());
        metadata.add(m);
        int row = metadata.size() - 1;
        // note which cells are editable
        int colCount = 0;
        for(Entry<String, MetadataValue> e : m.getAttributes()){
            logger.info("NEW ENTRY:");
            if(e.getValue().displayInProperties){
                if(e.getValue().editable){
                    editable.put(new Point(row, colCount), true);
                    logger.info("entry is editable at r,c" + row + " " + colCount);
                }
                else{
                    editable.put(new Point(row, colCount), false);
                    logger.info("entry is NOT editable at r,c" + row + " " + colCount);
                }
                colCount+=1;
            }
        }
        this.fireTableRowsInserted(metadata.size() - 1,
                                   metadata.size() - 1);
        logger.info("after adding, model size:" + metadata.size());
    }

    public void removeAllRows(){
        int tmp = metadata.size();
//        logger.info("before removing all rows, model size:" + tmp);
        metadata.clear();
        this.fireTableRowsDeleted(0, tmp);
    }

    public void removeRow(int index) {
        metadata.remove(index);
        this.fireTableRowsDeleted(index, index);
    }

    public int getRowCount() {
        return metadata.size();
    }

    
    public int getColumnCount() {
        return columnNames.size();
    }

    public Metadata getMetadataFromRow(int rowIndex) {
        return metadata.get(rowIndex);
    }

    @Override // TODO is this 'override' needed and correct?
    public Object getValueAt(int rowIndex, int columnIndex) {
        return metadata.get(rowIndex).get(getColumnName(columnIndex)).getVal();
    }

    @Override
    public void setValueAt(Object aValue, int row, int col){
      logger.info("[MTT] set value at " + row + " " + col);

      switch(enforceEditable){
        case NEVER:
          throw new IllegalStateException("table set to disallow all edits");
        case ALWAYS:
          // allow to pass out of switch
          break;
        case CHECK:
          if(!editable.get(new Point(row, col))){
            throw new IllegalStateException("row, column" + row + ", " +
                               col + " not editable.");
          }
          break;
      }

      // was editable if no exception thrown prior to this
      // set the value
      String attr = columnNames.get(col);
      // we just verified this value should be editable
      // and it must be visible to have been editted
      MetadataValue mv = metadata.get(row).get(attr);
      try {
        mv.setVal((String) aValue);
      } catch (Exception ex) {
        throw new IllegalStateException("row, column" + row + ", " +
                           col + " not editable.");
      }
      fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if(enforceEditable == AllowEdits.ALWAYS){
          logger.info("allow all edits");
          return true;
        }
        else if(enforceEditable == AllowEdits.NEVER){
          logger.info("disallow all edits");
          return false;
        }
        if(editable == null){
          logger.info("editable is null.. " + editable);
        }
        logger.info("check if editable is allowed");
        logger.info("editable contains r,c" + row + ", " + col +
                " " + editable.containsKey(new Point(row, col)));
        boolean val = editable.get(new Point(row, col));
        logger.info("val is: " + val);
        return editable.get(new Point(row, col));
    }

    @Override
    public Class<?> getColumnClass(int column) {
        // TODO for now, these are all strings
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        if(!columnNames.containsKey(column)){
            throw new IllegalStateException("Unknown column " + column);
        }
        return columnNames.get(column);
    }

    /**
     * Returns the list of Metadata represented by this model.
     * @param state
     */
    public ArrayList<Metadata> getMetadata() {
        return metadata;
    }



  }

  /**
   * Adds the listener to all tables in this MetadataTypesTable. This listener
   * will be re-added to the tables if the tabs are ever re-built. Opposite
   * of unregsiterListSelectionListener.
   *
   * @param listener
   */
  public void registerListSelectionListener(ListSelectionListener listener){
    tableSelectionListeners.add(listener);
    for(Component c : getComponents() ){
          JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
          JTable tab = (JTable) vp.getView();
          tab.getSelectionModel().addListSelectionListener(
                listener);
    }
  }

  /**
   * Removes the listener from all tables in this MetadataTypesTable. This
   * listener will no longer be re-added to the tables if the tabs are
   * ever re-built. Opposite of regsiterListSelectionListener.
   *
   * @param listener
   */
  public void unregisterListSelectionListener(ListSelectionListener listener){
    tableSelectionListeners.remove(listener);
    for(Component c : getComponents() ){
          JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
          JTable tab = (JTable) vp.getView();
          tab.getSelectionModel().removeListSelectionListener(
                listener);
    }
  }

    /**
   * Adds the listener to all tables in this MetadataTypesTable. This listener
   * will be re-added to the tables if the tabs are ever re-built. Opposite
   * of unregsiterTableModelListener.
   *
   * @param listener
   */
  public void registerTableModelListener(TableModelListener listener){
    tableModelListeners.add(listener);
    for(Component c : getComponents() ){
          JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
          JTable tab = (JTable) vp.getView();
          tab.getModel().addTableModelListener(listener);
    }
  }

  /**
   * Removes the listener from all tables in this MetadataTypesTable. This
   * listener will no longer be re-added to the tables if the tabs are
   * ever re-built. Opposite of unregsiterTableModelListener.
   *
   * @param listener
   */
  public void unregisterTableModelListener(TableModelListener listener){
    tableModelListeners.remove(listener);
    for(Component c : getComponents() ){
          JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
          JTable tab = (JTable) vp.getView();
          tab.getModel().removeTableModelListener(listener);
    }
  }

  /**
   * a custom renderer to highlight rows and alternate color
   */
  private class CustomTableCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public Component getTableCellRendererComponent
       (JTable table, Object value, boolean isSelected,
       boolean hasFocus, int row, int column)
    {
        Component cell = super.getTableCellRendererComponent
           (table, value, isSelected, hasFocus, row, column);
        MetadataTableModel mod = (MetadataTableModel) table.getModel();
        mod.getMetadataFromRow(row);
        int selected = table.getSelectedRow();
        // selected rows have their own color
        if(selected == row){
          // neutral blue, white text
          cell.setBackground(selectedColor);
          cell.setForeground(Color.white);
        }
        else if(highlights.contains(mod.getMetadataFromRow(row).getID())){
          // highlight this row
          // orange-red, black text
          cell.setBackground(highlightColor);
          cell.setForeground(Color.BLACK);
          // You can also customize the Font and Foreground this way
          // cell.setForeground();
          // cell.setFont();

        }
        else if(row%2 == 1){
          // alternate gray and white for normal rows
          // black text
          cell.setBackground(Color.WHITE);
          cell.setForeground(Color.BLACK);
        }
        else{
          // alternate gray and white for normal rows
          // black text
          cell.setBackground(Color.LIGHT_GRAY);
          cell.setForeground(Color.BLACK);
        }
        return cell;
    }
  }

}
