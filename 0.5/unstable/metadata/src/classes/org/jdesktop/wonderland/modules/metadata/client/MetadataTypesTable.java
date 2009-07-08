package org.jdesktop.wonderland.modules.metadata.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;

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

@CellComponentProperties
public class MetadataTypesTable extends JTabbedPane {
  // used to map pieces of metadata to their appropriate table
  private HashMap<Class, JTable> metatypeMap = new HashMap<Class, JTable>();
  private static Logger logger = Logger.getLogger(MetadataComponent.class.getName());
  private ArrayList<ListSelectionListener> tableSelectionListeners = new ArrayList<ListSelectionListener>();
  private ArrayList<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();
//  private HashMap<JTable, Class> defaultMetadata = new HashMap<JTable, Class>();

  /**
   * whether or not to enforce table cell's editability based on Metadata types
   * turned off, for example, in the search panel to allow user to edit any attribute
   * in their search query.
   */
  private boolean enforceEditable;

  /**
   *
   *
   */
  public MetadataTypesTable() {
    // this panel is dynamically populated
    enforceEditable = true;
    updateTypeTabs();
  }

  void setEnforceEditable(boolean b) {
    enforceEditable = b;
  }

  

  /**
   * Clear all tabs from table, request the current list of Metadata types from
   * the client plugin, and rebuild each tab.
   */
  private void updateTypeTabs() {
//    Iterator<Class> iterator = MetadataPlugin.getMetadataTypes().iterator();
    Iterator<MetadataSPI> iterator = MetadataClientUtils.getTypesIterator();
    removeAll();
    metatypeMap.clear();
    while(iterator.hasNext()){
      MetadataSPI type = iterator.next();
//      Class clazz = type.getClass();
//        MetadataSPI type = null;
//        try {
//          type = (MetadataSPI) iterator.next().newInstance();
//        } catch (Exception ex) {
//          logger.log(Level.SEVERE, "[MTT] unexpected type in metadata plugin:" + ex);
//        }
        // tell type to do its client setup
//        type.initByClient(LoginManager.getPrimary().getPrimarySession().getUserID());
        // create a new table for this type
        JTable typeTable = new JTable(new MetadataTableModel(type));
//        defaultMetadata.put(typeTable, clazz);

        // listeners
        for(ListSelectionListener l: tableSelectionListeners){
          typeTable.getSelectionModel().addListSelectionListener(l);
        }
        for(TableModelListener l: tableModelListeners){
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
   * Erase all entries on each tab.
   */
  public void clearTabs(){
      for(Component c : getComponents() ){
            JViewport vp =  (JViewport) ((JScrollPane) c).getViewport();
            JTable tab = (JTable) vp.getView();
            MetadataTableModel mod = (MetadataTableModel) getCurrentTable().getModel();
            mod.removeAllRows();
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
   * @param newMetadata The list of Metadata to add.
   */
  public void addMetadata(List<MetadataSPI> newMetadata){
    for(MetadataSPI m : newMetadata)
    {
        // match to appropriate model
        JTable table = metatypeMap.get(m.getClass());
        if(table == null){
            logger.log(Level.SEVERE, "Unrecognized Metadata type \"" +
                                m.getClass().getName() + "\" " + m.getClass());
            continue;
        }
//        logger.log(Level.INFO, "metadata: " + m + " class: " + m.getClass().getName() + " type: " + m.simpleName() + "table: " + t);
        MetadataTableModel mod = (MetadataTableModel) table.getModel();
        logger.log(Level.INFO, "Model is: " + m);
        
        mod.addRow(m);
    }
      
  }

  /**
   * helper function, creates blank new metadata object of the same type as the
   * current tab.
   * @return
   */
  private MetadataSPI createNewMetadata(){
    JTable tab = getCurrentTable();
    MetadataTableModel mod = (MetadataTableModel) tab.getModel();
    MetadataSPI res = null;
    try {
      res = (MetadataSPI) mod.getMetadataClass().newInstance();
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
    MetadataSPI meta = createNewMetadata();
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
    MetadataSPI meta = createNewMetadata();

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
   * Returns the list of Metadata from the tab at index idx.
   * @param idx
   */
    public ArrayList<MetadataSPI> getMetadataFromTab(int idx) throws Exception {
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

  void removeCurrentlySelectedRow() {
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
            logger.log(Level.INFO, "Key, Val: " + e.getKey() + ", " + e.getValue());
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
    private ArrayList<MetadataSPI> metadata = new ArrayList<MetadataSPI>();
    private HashMap<Point, Boolean> editable = new HashMap<Point, Boolean>(); // point to represent row/col
    /**
     * can be used to get new instances of metadata for this model
     */
    private Class metadataClass;

    public MetadataTableModel(MetadataSPI type) {
       // TODO this needs to talk to the type registration system
       // to get example blanks of each type
       logger.log(Level.INFO, "MetadataTableModel type: " + type.simpleName());
//       Metadata example;
//       if(type.simpleName().equals("Metadata")){
//           example = new Metadata(null, null);
//       }
//       else{
//           example = new SimpleMetadata(null, null);
//       }

       int colCount = 0;
       for(Entry<String, MetadataValue> e : type.getAttributes()){
            logger.log(Level.INFO, "NEW ENTRY: " + e.getKey());
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

    public void addRow(MetadataSPI m) {
        logger.log(Level.INFO, "add row in model");
        metadata.add(m);
        int row = metadata.size() - 1;
        // note which cells are editable
        int colCount = 0;
        for(Entry<String, MetadataValue> e : m.getAttributes()){
            logger.log(Level.INFO, "NEW ENTRY:");
            if(e.getValue().displayInProperties){
                if(e.getValue().editable){
                    editable.put(new Point(row, colCount), true);
                    logger.log(Level.INFO, "entry is editable at r,c" + row + " " + colCount);
                }
                else{
                    editable.put(new Point(row, colCount), true);
                    logger.log(Level.INFO, "entry is NOT editable at r,c" + row + " " + colCount);
                }
                colCount+=1;
            }
        }
        this.fireTableRowsInserted(metadata.size() - 1,
                                   metadata.size() - 1);
    }

    public void removeAllRows(){
        int tmp = metadata.size();
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

    @Override // TODO is this 'override' needed and correct?
    public Object getValueAt(int rowIndex, int columnIndex) {
        return metadata.get(rowIndex).get(getColumnName(columnIndex)).getVal();
    }

    // TODO will this be called by things like add row? how does inplace editing work?
    @Override
    public void setValueAt(Object aValue, int row, int col){
        if(enforceEditable && editable.get(new Point(row, col))){
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
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if(!enforceEditable){
          logger.log(Level.INFO, "do not enforce editable");
          return true;
        }
        if(editable == null){
          logger.log(Level.INFO, "editable is null.. " + editable);
        }
        logger.log(Level.INFO, "editable contains r,c" + row + ", " + col +
                " " + editable.containsKey(new Point(row, col)));
        boolean val = editable.get(new Point(row, col));
        logger.log(Level.INFO, "val is: " + val);
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
    public ArrayList<MetadataSPI> getMetadata() {
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



}
