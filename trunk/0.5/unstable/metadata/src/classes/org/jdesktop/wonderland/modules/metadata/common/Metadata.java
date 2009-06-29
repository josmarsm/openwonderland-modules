package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;

/**
 *
 * @author mabonner
 */
public class Metadata implements Serializable {
    private CellID parentCell;
    String type; // should this be a string? is it even necessary?
    public final int id; // unique id
    // TODO does this need to be synchronized in some way?
    // cellregistry didn't seem to sync on cellid's...
    private static int count;
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
     * by adding them to attributes in their constructor. To work with the
     * default LDAP implementation, attribute names should contain only
     * alpha-numeric and space characters, and should not begin with a numeric
     * character.
     *
     * when a user adds a metadata object in the cell properties panel,
     * a blank object of the appropriate type is created.
     *
     * @return
     */
    public Metadata(String creator, String creationDate){
        attributes = new HashMap<String, MetadataValue>();
        // defaults
        put("Creator", new MetadataValue(creator, false, true, Datatype.STRING));
        put("Created", new MetadataValue(creationDate, false, true, Datatype.DATE));
        put("Modifier", new MetadataValue("", false, true, Datatype.STRING));
        put("Modified", new MetadataValue("", false, true, Datatype.DATE));
        id = count++  ;
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
     * This is displayed on the cell properties, context menus, etc.
     * @return
     */
    public String simpleName(){
        return "Metadata";
    }

    public MetadataValue get(String attr){
        return attributes.get(attr);
    }

    /**
     * To work with the
     * default LDAP implementation, attribute names should contain only
     * alpha-numeric and space characters, and should not begin with a numeric
     * character.
     *
     * @param attr the name of the attribute
     * @param val type of value attribute represents
     */
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
