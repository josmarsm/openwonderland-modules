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

package org.jdesktop.wonderland.modules.metadata.common;

import com.jme.math.Vector3f;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;


/**
 * To create a new metadata type, users are expected to extend the base Metadata
 * type and annotate their class with "@MetadataType". See Metadata and
 * SimpleMetadata for examples of this.
 *
 * Metadata subtypes should provide at least a constructor that takes no arguments,
 * in addition to any other constructors.
 * 
 * This interface is provided in case someone wants to create their own base class.
 * Examine the provided base class, BaseMetadata, before doing this.
 *
 * @author mabonner
 */
public interface Metadata {

    /**
     * Provide a human-readable type name.
     *
     * This is displayed on the cell properties, context menus, etc.
     * @return
     */
    public String simpleName();

    /**
     * Provides a human-readable string that displays the Metadata's content
     * Base metadata's implementation should be fine for most users.
     * @return
     */
    @Override
    public String toString();

    /**
     * Called when a new Metadata object is created by a client (generaly, from
     * the cell properties pane or context menu)
     * 
     * @return
     */
    public void initByClient(WonderlandIdentity id);

    /**
     * Return the MetadataValue bound to an attribute. Return null if the name
     * is not bound.
     *
     * @param attr attribute name
     * @return bound value, or null
     */
    public MetadataValue get(String attr);

    /**
     * Return the Metadata's id, a unique number among all metadata objects
     *
     * @return metadata's id
     */
    public MetadataID getID();

    /**
     * Bind a new MetadataValue an attribute name. Will replace old value
     * if one exists.
     *
     * Note that to work with the default LDAP implementation, attribute
     * names should contain only alpha-numeric and space characters, and
     * should not begin with a numeric character.
     *
     * @param attr the name of the attribute
     * @param val type of value attribute represents
     */
    public void put(String attr, MetadataValue val);

    /**
     * Convenience function to change the value of an already added attribute,
     * without adjusting other information (examples: editable, displayInProperties)
     * present in the MetadataValue.
     * 
     * Replaces the value member of the MetadataValue bound to attr. If the name
     * attr has not already been bound to a MetadataValue (using the other
     * version of put, this will throw an exception.
     *
     * Note that to work with the default LDAP implementation, attribute
     * names should contain only alpha-numeric and space characters, and
     * should not begin with a numeric character.
     *
     * @param attr the name of the attribute
     * @param val new value for the MetadataValue bound to this name
     */
    public void put(String attr, String val) throws MetadataException;

    /**
     * Remove an attribute binding, if it exists.
     * 
     * @param attr
     */
    public void remove(String attr);

    /**
     * Get all bound attribute names and their values
     * @return
     */
    public Set<Map.Entry<String, MetadataValue>> getAttributes();

    /**
     * Will be called if cell is annotated with the MetadataContextMenuItem
     * annotation. Examine the cell's class and determine if this metadatatype should be
     * addable via the cell's context menu.
     *
     * If the cell will not be annotated in this way, simply leave this operation
     * unsupported.
     *
     * @param c class of cell in question
     * @return true if this metadata type should appear in cell with class c's context menu
     */
    public boolean contextMenuCheck(Class c);

     /**
      * Metadata objects should override the equals method and compare attributes.
      * The Metadata base type's implementation should be fine for most users.
      * @param o
      * @return
      */
     @Override
     public boolean equals(Object o);

     /**
      * The in-world location associated with this metadata. The default implementation
      * returns null, which will cause the search's goto button to use the parent
      * cell's location.
      * @return location
      */
     public Vector3f getLocation();
}
