package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author mabonner
 */
public class Metadata implements Serializable {
    private CellID parentCell;
    String type; // should this be a string? is it even necessary?
    int uniqueID; // do this like CellID
    /**
     * this DateFormat to be used in all default date fields
     */
    public static final DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy h:mm:ss a");
    /**
     * access strings for default values
     */
//    public enum Defaults{CREATOR};

    private HashMap<String, MetadataValue> attributes;
//    private String author, modAuthor, creationDate, modDate;

    /**
     * Subclasses should include all attributes and any default values
     * by adding them to attributes in their constructor.
     *
     * when a user adds a metadata object in the cell properties panel,
     * a blank object of the appropriate type is created.
     *
     * @return
     */
    public Metadata(String creator, String creationDate){
        attributes = new HashMap<String, MetadataValue>();
        // defaults
        put("Creator", new MetadataValue(creator, false, true));
        put("Created", new MetadataValue(creationDate, false, true));
        put("Modifier", new MetadataValue("", false, true));
        put("Modified", new MetadataValue("", false, true));
        return;
    }

//    private void initDefaults(String inAuthor, String inCreationDate) {
////        author = inAuthor;
////        creationDate = inCreationDate;
////        modAuthor = "";
////        modDate = "";
//    }

    /**
     * Subclasses should overwrite this and provide their own human
     * readable type name.
     * This will affect where an object is displayed on the cell properties.
     * @return
     */
    public String simpleName(){
        return "Metadata";
    }

    public MetadataValue get(String attr){
        return attributes.get(attr);
    }

    public void put(String attr, MetadataValue val){
        attributes.put(attr, val);
    }

    public void remove(String attr){
        attributes.remove(attr);
    }

    public Set<Map.Entry<String, MetadataValue>> getAttributes(){
        return attributes.entrySet();
    }

    /**
     * Pack attributes/values for the Properties pane. Each pair should have a
     * JLabel "attr title" and a JLabel "attr value" for non-user editable vals,
     * or a JTextField for editable vals.
     *
     * Subclasses of Metadata should override this class, and begin by calling
     * super.toJPanel, and add to that Panel.
     *
     * See SimpleMetadata for an example.
     *
     * @return a JPanel containing the attr/value pairs
     */
//    public JPanel toJPanel(){
//        JPanel panel = new JPanel();
//        return panel;
//    }

//    public String getTitle(){
//        return attributes.get("author");
//    }
//    public String getText(){
//        return attributes.get("author");
//    }

}
