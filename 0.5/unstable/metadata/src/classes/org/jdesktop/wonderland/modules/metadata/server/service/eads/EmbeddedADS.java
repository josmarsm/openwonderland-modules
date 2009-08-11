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

package org.jdesktop.wonderland.modules.metadata.server.service.eads;


import org.jdesktop.wonderland.modules.metadata.server.service.*;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.schema.SchemaService;
import org.apache.directory.server.schema.registries.AttributeTypeRegistry;
import org.apache.directory.server.schema.registries.ObjectClassRegistry;
import org.apache.directory.server.schema.registries.Registries;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.BaseMetadata;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;

/**
 * A default backend interface for the MetadataService (to provide searching).
 * This example uses an Embedded Apache Directory Service server (EADS) to
 * create an LDAP database.
 *
 * Users are free to create an alternative backend, backed by LDAP or otherwise.
 *
 * @author mabonner
 */
public class EmbeddedADS implements MetadataBackendInterface
{
  private static Logger logger = Logger.getLogger(EmbeddedADS.class.getName());

  /**
   * registry from embedded ApacheDS server, used to check whether an attribute
   * has already been registered
   */
  private AttributeTypeRegistry attributeTypes;

  /**
   * registry from embedded ApacheDS server, used to check whether an object class
   * has already been registered
   */
  private ObjectClassRegistry objectClasses;

//  private final static String dateSyntaxOID = "1.3.6.1.4.1.1466.115.121.1.24";

  /**
   * LDAP syntax, used to register new object classes
   * An instantiated object must have exactly one STRUCTURAL object class.
   */
  public enum ObjectClassType { STRUCTURAL, AUXILIARY }

  // contexts used as bases to add new metadata, attributes, etc
  /** the very top context, containing all others, including schemas and various
   *  book-keeping trees set up by ApacheDS
   */
  static DirContext rootCtx;
  /**
   * the DN to name the WL ctx (topCtx) in rootCtx
   */
  static final String wlCtxDN = "dc=wonderland";
  /** immediately below root, contains all cells and metadata */
  static DirContext wlCtx;

  

  /**
   * stores attribute names that have already been registered
   */
  private HashMap<String, Datatype> attrNames = new HashMap<String, Datatype>();



  /**
   * used to store a cellID attribute and the context it can be located in,
   * saving re-looking it up.
   */
  class pairCidAndCtx{

    public DirContext ctx;
    public Integer cid;
    public pairCidAndCtx(DirContext x, Integer cellId) {
      ctx = x;
      cid = cellId;
    }
  }


  
  /**
   * Creates a new instance of EmbeddedADS. It initializes the directory service.
   * @param reset true if eads should rebuild itself, false if jbdm file already exists
   * @throws Exception If something went wrong
   */
  public EmbeddedADS(boolean reset) throws Exception
  {
    logger.log(Level.CONFIG, "[EADS] creating EADS");
    initLDAPServer(reset);
    logger.log(Level.CONFIG, "[EADS] created");
  }

  /**
   * Creates a new instance of EmbeddedADS. It initializes the directory service.
   *
   * Convenience method to initialize with some set types.
   *
   * @throws Exception If something went wrong
   */
  public EmbeddedADS(boolean reset, ArrayList<Metadata> metadata) throws Exception
  {
    logger.log(Level.CONFIG, "[EADS] creating EADS");
    initLDAPServer(reset);
    logger.log(Level.CONFIG, "[EADS] register metadata types..");
    for(Metadata m:metadata){
      registerMetadataType(m);
    }
    logger.log(Level.CONFIG, "[EADS] created");
  }




  /**
   * adds the passed metadata object to the cell named id.
   * throws exceptions and logs errors if the cell does not exist or the
   * metadata type has not been registered.
   *
   * note that LDAP doesn't allow null/empty attributes.
   * Empty attributes are converted into
   *   Datatype.STRING : a single space " "
   *   Datatype.INTEGER : 0
   *   Datatype.DATE : 0 seconds since 'the epoch', e.g. January 1, 1970, 00:00:00 GMT
   *
   * @param cid id of cell to add metadata to
   * @param metadata metadata object to add
   */
  public void addMetadata(CellID cid, Metadata metadata) {
    // check if the cell exists, prepare a context if it does
    DirContext cellCtx = getCellCtx(cid);
    if(cellCtx == null){
      logger.severe("[EADS]could not add metadata to cell - could not " +
              "find cell id:" + cid);
    }

    // prepare attributes
    BasicAttributes attrs = new BasicAttributes(true);

    // object class
    BasicAttribute metaOC = new BasicAttribute("objectclass");
    String nameLdap = EADSUtils.classToLDAPName(metadata.getClass());
    metaOC.add(nameLdap);
    metaOC.add(EADSUtils.METADATA_AUX_OC);
    attrs.put(metaOC);
    logger.info("[EADS] adding metadata of type " + nameLdap +
            " to cell " + cid);

    // this class's attributes
    for(Entry<String, MetadataValue> e : metadata.getAttributes()){
      // make name ldap friendly
      String attrName = EADSUtils.attrnameToLDAP(e.getKey());
      // make name server-unique
      attrName = EADSUtils.getUniqueName(attrName, metadata.getClass());
      MetadataValue mv = e.getValue();
      String val = mv.getVal();
      logger.info("[EADS] attribute " + attrName + " with raw value '" +
              val + "', expected type of " + mv.type);
      BasicAttribute attr = new BasicAttribute(attrName);

      // the value
      switch (mv.type){
        case DATE:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            Date d = new Date(0);
            val = BaseMetadata.dateFormat.format(d);
            break;
          }
          // else, convert to ldap date
          try {
            Date d = BaseMetadata.dateFormat.parse(val);
            val = EADSUtils.ldapDateFormat.format(d);
          } catch (ParseException ex) {
            logger.severe("[EADS] invalid date syntax adding metadata" +
                    " to cell id: " + cid + ", string:" + val);
          }
          break;
        case INTEGER:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            val = "0";
          }
          break;
        case STRING:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            val = " ";
          }
      }

      // add to collection
      attr.add(val);
      attrs.put(attr);
    }
    try {
      // finally, add the new entry
      cellCtx.createSubcontext(EADSUtils.METADATA_ID_ATTR + "=" + metadata.getID(), attrs);
    } catch (NamingException ex) {
      logger.info("[EADS] error adding metadata of type " + nameLdap +
            " to cell " + cid + ", ex:\n" + ex);
    }

    logger.info("[EADS] AFTER ADDING METADATA, CONTENTS:");
    logger.info("[EADS] ================ldap contents of cell ================:");
    EADSUtils.printContents(rootCtx, wlCtxDN);
    logger.info("[EADS] ================verbose ldap contents of cell ================:");
    EADSUtils.printContentsVerbose(rootCtx, cellCtx, 0);
  }


  public void eraseCell(CellID cid){
    logger.severe("[EADS] erasing cell with id: " + cid);
    try {
      DirContext cellCtx = (DirContext) rootCtx.lookup(getCellDN(cid));
      eraseContext(cellCtx);
    } catch (NamingException ex) {
      logger.severe("[EADS] error erasing cell with id: " + cid);
    }

  }

  /**
   * Adds a cell under the appropriate context. Called by the two addCell methods
   * implemented for the Backend interface.
   *
   * @param cid cell ID to add
   * @param parentCtx context to add new cell under
   */
  private void addCellHelper(CellID cid, DirContext parentCtx) {
    logger.info("[EADS] adding cell");
    BasicAttributes attrs = new BasicAttributes(true); // case-ignore
    BasicAttribute objclass = new BasicAttribute("objectclass");
    objclass.add(EADSUtils.CELL_OC);
    attrs.put(objclass);
    try {
      // Create the context
      logger.info("[EADS] create cell subcontext");
      parentCtx.createSubcontext(EADSUtils.CELL_ID_ATTR + "=" + cid, attrs);
      logger.info("[EADS] created cell subcontext");
    } catch (NamingException ex) {
      logger.severe("[EADS] error adding cell id: " + cid + ", ex:" + ex);
      logger.severe("[EADS] looked like:" + EADSUtils.CELL_ID_ATTR + "=" + cid);
    }
  }

  /**
   * searches ldap tree for cell with given ID.
   * @param cid cell id to find
   * @return the cell's DN
   */
  private String getCellDN(CellID cid){
    logger.info("get DN for cid " + cid);
    String filter = "(&(objectclass=" + EADSUtils.CELL_OC + ")(" + EADSUtils.CELL_ID_ATTR + "="+ cid +"))";
    String cellScope = wlCtxDN;
    SearchControls ctls = new SearchControls();
    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration results;
    SearchResult si = null;
    try {
      results = rootCtx.search(cellScope, filter, ctls);
      si = (SearchResult)results.next();
    } catch (NamingException ex) {
      logger.severe("[EADS] could not find cell with id: " + cid);
      logger.info("[EADS] ================verbose ldap contents ================:");
      EADSUtils.printContentsVerbose(rootCtx, wlCtx, 0);
    }
    return si.getName();
  }


  private void eraseContext(DirContext delCtx) throws NamingException {
      NamingEnumeration list = delCtx.listBindings("");

      // go through all bindings
      while (list.hasMore()) {
          NameClassPair nc = (NameClassPair)list.next();
          try{
            DirContext subCtx = (DirContext) rootCtx.lookup(nc.getName());
            // recurse
            eraseContext(subCtx);
          }
          catch ( LdapNameNotFoundException lnnfe ){
            logger.info("severe error...");
          }

      }

      // by the time we return here, context should be empty
      rootCtx.destroySubcontext(delCtx.getNameInNamespace());
  }



  public void removeMetadata(MetadataID mid){
    // find the metadata cell
    String filter = "(&(" + EADSUtils.METADATA_ID_ATTR + "="+ mid + "))";
    SearchControls ctls = new SearchControls();
    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration results = null;
    try {
      results = rootCtx.search(wlCtxDN, filter, ctls);
      SearchResult si = (SearchResult)results.next();
      rootCtx.destroySubcontext(si.getName());
    } catch (NamingException ex) {
      logger.severe("[EADS] error erasing metadata, could not find mid:" + mid);
    }
  }

  /**
   * Remove all metadata from a cell
   *
   * @param cid id of cell to remove metadata from
   */
  public void clearCellMetadata(CellID cid) {
    logger.info("[EADS] clear cell metadata, cell id: " + cid);
    logger.info("***************************");
    EADSUtils.printContents(rootCtx, wlCtxDN);
    logger.info("***************************");
    // get cell context
    // TODO will this be necessary when cell reparenting is added?
//    DirContext cellCtx = getCellCtx(cid);
    // get all its metadata
    String filter = "(&(" + EADSUtils.METADATA_ID_ATTR + "=*))";
    SearchControls ctls = new SearchControls();
    ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    NamingEnumeration results = null;
    NamingEnumeration results2 = null;
    String cellDN = getCellDN(cid);
    logger.info("Cell DN to clear is: " + cellDN);
//    try {
//      logger.info("Cell ctx is: " + cellCtx.getNameInNamespace());
//    } catch (NamingException ex) {
//      logger.severe("error viewing cell ctx name" + ex);
//    }
    try {
      // TODO this search is failing?
    logger.info("[EADS] clear cell metadata, search " + getCellDN(cid)
              + " for metadata");
    results = rootCtx.search(cellDN, filter, ctls);
    logger.info("[EADS] got results");
//      results2 = cellCtx.search(cellDN, filter, ctls);
      
      while(results.hasMore()){
        logger.info("[EADS] attempting to delete an item");
        SearchResult si = (SearchResult)results.next();
        logger.info("[EADS] clearing result: " + si.getName());
        rootCtx.destroySubcontext(si.getName());
        logger.info("[EADS] result cleared!");
      }
    } catch (NamingException ex) {
      logger.severe("[EADS] error clearing metadata of cell with id:" + cid);
      logger.severe("[EADS] exception:" + ex);
    }
    if(results == null){
      logger.info("[EADS] no metadata on this cell!");
    }
    logger.info("----------------------------");
    EADSUtils.printContents(rootCtx, wlCtxDN);
    logger.info("-----------------------------");
  }

  /**
   * Take any action necessary to register this metadatatype as an option.
   * Name collision on class name or attribute name is up to the implementation.
   *
   * This implementation uses the full package name to describe a Metadata obj
   * and its attributes, avoiding collisions.
   *
   * @param m example of the type to register
   */
  public void registerMetadataType(Metadata m) throws Exception {
    // build required elements for LDAP
    logger.info("[EADS] registering new metadata type");
    // NAME our unique class name
    // convert to ldap friendly name
    String nameLdap = EADSUtils.classToLDAPName(m.getClass());
    logger.info("[EADS] type name: " + nameLdap);

    // SUP super classes
    // super-classes are not used. if superclasses were needed, fetch as follows
    ArrayList<String> supersLdap = new ArrayList<String>();
    //
    // fetch all super classes
    //
    // Class superClass = m.getClass().getSuperclass();
    // Class objClass = Object.class;
    // while(!superClass.equals(objClass)){
    //   String superName = toLdapFriendlyStr(superClass.getName());
    //   logger.info("superclass name: " + superName);
    //   supersLdap.add(superName);
    //   superClass = superClass.getSuperclass();
    // }

    // DESC
    String descLdap = "registered type";

    // MUST attributes
    // register all these attributes as we assemble our MUST list
    ArrayList<String> mustLdap = new ArrayList<String>();
    // all attributes must have a metadata id
    mustLdap.add(EADSUtils.getUniqueName(EADSUtils.METADATA_ID_STR, this.getClass()));
    String attrName = "";
    for(Entry<String, MetadataValue> e : m.getAttributes()){
      try{
      // make LDAP friendly
        attrName = EADSUtils.attrnameToLDAP(e.getKey());
  //      logger.info("Key, Val: " + e.getKey() + ", " + e.getValue());
        MetadataValue mv = e.getValue();
        ArrayList<String> attrNameArray = new ArrayList<String>();
        attrNameArray.add(attrName);
        registerAttribute(attrNameArray, attrName, e.getValue().type, m.getClass());
        mustLdap.add(EADSUtils.getUniqueName(attrName, m.getClass()));
      }
      catch(LdapInvalidAttributeValueException ex){
        logger.severe("[EADS] Attribute " + attrName + " for type "
                + m.getClass().getName() + " has invalid LDAP syntax\nMetadata type" +
                " will not be registered");
        logger.severe("[EADS] Attribute value:"+e.getValue());
        throw(new Exception("[EADS] Attribute " + attrName + " for type " +
                m.getClass().getName() + " has invalid LDAP syntax"));
      }
    }

    // MAY attributes
    // (no optional attributes allowed in current implementation)
    ArrayList<String> mayLdap = new ArrayList<String>();

    // finally, register the type
    try{
      registerObjectClass(nameLdap, descLdap, supersLdap, ObjectClassType.STRUCTURAL, mustLdap, mayLdap);
    }
    catch(NamingException e){
      logger.info("[EADS] exception registering metadata type:" + e);
    }

  }

  

  /**
   * If this cell id exists, return a context to it. If not, return null.
   * @param cid id of the cell to find
   * @return
   */
  private DirContext getCellCtx(CellID cid) {
    DirContext cellCtx;
    String dn = getCellDN(cid);
    try{
      cellCtx = (DirContext) rootCtx.lookup(dn);
    }
    catch(NamingException e){
      // if an exception was thrown, things weren't yet properly initialized
      // or the cell simply wasn't found
      logger.info("[EADS] could not get cell context for cellID: + cid");
      return null;
    }
    return cellCtx;
  }

  

  /** register a new attribute with the embedded LDAP server
   * builds an RFC 4512 syntax string out of parameters
   *
   * OIDs are created for attributes by hashing the parent class and attribute name.
   * Thus, every uniquely named attribute in a unique object class (metadata subtype) will
   * have a unique and constant OID.
   *
   * Attribute OID's will thus have the form...
   * metadataAttributesOID.classHash.nameHash
   *
   * @param name list of names for new attribute
   * @param desc DESC of the new attribute
   * @param type determines the syntax and comparison rules that will be used
   * @param parentClass full name of parent class this attribute is associated with, used to build OID hash for this attribute type
   */
  private void registerAttribute(ArrayList<String> names, String desc, Datatype type, Class parentClass) throws NamingException {
    // build OID - this attribute's unique identifier
    // create by hashing parent class' and this attribute's name
    int classHash = parentClass.getName().hashCode();
    if (classHash < 0){
      classHash = classHash * -1;
    }

    int nameHash = names.get(0).hashCode();
    if (nameHash < 0){
      nameHash = nameHash * -1;
    }
    String OID = EADSUtils.MetaAttrOID + "." + classHash + "." + nameHash;
    // check if this attribute has already been added
    logger.info("checking if contains attribute with oid:" + OID);
    if(attributeTypes.hasAttributeType(OID)){
      logger.info("[EADS] register attribute - already registered!!");
      return;
    }
            
    desc = "'" + desc + "'";
    // convert list of names to be RFC 4512 compliant
    // also convert them to be server-side unique by appending their full class name
    String name = "(";
    for(String s:names){
      name += "'" + EADSUtils.getUniqueName(EADSUtils.attrnameToLDAP(s), parentClass) + "' ";
    }
    name += ")";

    String syntax = "";
    switch (type){
      case INTEGER:
        syntax = EADSUtils.integerSyntax;
        break;
      case STRING:
        syntax = EADSUtils.stringSyntax;
        break;
      case DATE:
        syntax = EADSUtils.dateSyntax;
        break;
    }

    String attr = "( " + OID + " NAME " + name + " DESC " + desc + " " + syntax + ")";
    Attributes newAttribute = new BasicAttributes(true);
    newAttribute.put("attributeTypes", attr);

    logger.info("[EADS] registering new attribute: " + OID + " "+ name + " with type " + type);
    logger.fine("[EADS] full info: " + newAttribute);
    boolean tryAgain = true;
    int count = 0;
    Attributes as = rootCtx.getAttributes("cn=schema");
    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, newAttribute);
//    logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%TRYING");
//    while(tryAgain && count < 10){
//      try{
//        logger.info("try again");
//        tryAgain = false;
//        attrCount += 1;
//        count++;
//        attr = "( " + MetaAttrOID + "." + attrCount + " NAME " + name + " DESC " + desc + " " + syntax + ")";
//        newAttribute = new BasicAttributes(true);
//        newAttribute.put("attributeTypes", attr);
//        rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, newAttribute);
//      }
//      catch(NamingException e){
//        logger.info("EXCEPTION IS: " + e);
//        logger.info("EXCEPTION class: " + e.getClass().getName());
//        String oid = MetaAttrOID + "." + (attrCount -1);
//        Attribute ar = as.get(oid);
//        if(ar == null){
//          logger.info("get oid was null");
//        }
//        else{
//          logger.info((String) ar.get());
//        }
//        tryAgain = true;
//      }
//    }
//
//    logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%DONE TRYING");
    // at this point, the type has been successfully added
    // note the name/type pair to avoid collisions
    attrNames.put(name, type);
  }


  /**
   * Register a new object class with the embedded LDAP server.
   * builds an RFC 4512 syntax string out of parameters.
   *
   * Make sure to add any attributes the class needs first, or this will throw
   * an exception.
   *
   * @param name name of new object class - assumes name has been converted to LDAP and joined with class name
   * @param desc DESC of the new attribute
   * @param classes superclasses to add to objectClass
   * @param mustAttrs attributes the class must have
   * @param mayAttrs attributes the class may have
   * @param className
   */
  private void registerObjectClass(String name, String desc, ArrayList<String> objClasses, ObjectClassType ocType, ArrayList<String> mustAttrs, ArrayList<String> mayAttrs) throws NamingException {
    // build OID - this attribute's unique identifier
    // create by hashing parent class' and this attribute's name
    int x = name.hashCode();
    if (x < 0){
      x = x * -1;
    }
    String OID = EADSUtils.MetaObjClassOID + "." + x;
    // check if this attribute has already been added
    logger.info("checking if contains attribute with oid:" + OID);
    if(objectClasses.hasObjectClass(OID)){
      logger.info("[EADS] register object class - already registered!!");
      return;
    }


    // if there was only one name, add 's around it
    // otherwise, assume it is already RFC 4512 compliant
    if(name.indexOf("(") == -1){
      name = "'" + name + "'";
    }

    String must = EADSUtils.buildObjList(mustAttrs);

    Attributes ocAttrs = new BasicAttributes(true);
    String oc = "( " + OID + " NAME " + name;
    if(desc != null){
      desc = "'" + desc + "'";
      oc += " DESC " + desc;
    }
    if(!objClasses.isEmpty()){
      oc += " SUP " + EADSUtils.buildObjList(objClasses);
    }
    oc += " " + ocType;
    if(!mustAttrs.isEmpty()){
      oc += " MUST " + must;
    }
    if(!mayAttrs.isEmpty()){
      oc += " MAY " + EADSUtils.buildObjList(mayAttrs);
    }
    oc += " )";
    ocAttrs.put("objectClasses", oc);
    logger.info("[EADS] registering object class:\n" + oc);
    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, ocAttrs);
  }

  public void addCell(CellID cid) {
    logger.info("[EADS] adding cell " + cid + " to world context");
    addCellHelper(cid, wlCtx);
  }

  public void addCell(CellID cid, CellID parent) {
    logger.info("[EADS] adding cell " + cid + " to parent context " + parent);
    addCellHelper(cid, getCellCtx(parent));
  }

  

  /**
   * Searches for all cells present at ctx, adds them to cids.
   * @param ctx Context in which to scope search for cells
   * @param scope name in ctx in which to search for cells
   * @param cids found cell cellID's are added to this list
   * @throws javax.naming.NamingException
   */
  private void getAllCells(DirContext ctx, String scope, LinkedList<pairCidAndCtx> cids) {
    // get all cells
    String f1 = "(&(objectclass=" + EADSUtils.CELL_OC + ")(" + EADSUtils.CELL_ID_ATTR + "=*))";
    // will store their cID's
    String[] attrIDs = {EADSUtils.CELL_ID_ATTR};
    SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(attrIDs);
    NamingEnumeration answer;
    try {
      answer = ctx.search(scope, f1, ctls);
      // all found we have found are in this context... we will pair this context
      // with the cID's we save
      DirContext cellCtx = (DirContext)ctx.lookup(scope);
      while (answer.hasMore()) {
        SearchResult sr = (SearchResult)answer.next();
  //      logger.info((">>> found new cell" + sr.getName());
        for (NamingEnumeration e = sr.getAttributes().getAll(); e.hasMore();){
          Attribute attr = (Attribute)e.next();
    //        logger.info((attr.get());
          Integer i = Integer.parseInt((String)attr.get());
          cids.add(new pairCidAndCtx(cellCtx,i));
        }
      }
    } catch (NamingException ex) {
      logger.severe("[EADS] error retrieving all cells " +
                  " from scope: " + scope + ", ctx:" + ctx);
    }
  }

  /**
   * Tail-recursive function will search entire tree from ctx down for cells
   * (objectclass='EADSUtils.CELL_OC'), and search these cells for metadata matching each
   * filter string in filters
   * @param cids list of cells and their contexts remaining to search, built up
   *        during recursion
   * @param filters the list of filters to match
   * @param ctx context in which to begin search. To search all cells, pass the
   *            wl parent context. To search a particular cell's children, pass
   *            in that cell's context. Doese NOT search the cell pointed to
   *            by context, even if context points at a cell.
   * @param results maps a full-matching cell id's to a set of their filter
   *                matching metadata's id's, built up over recursion
   * @return results, after every cell from cids and any new cells discovered
   *            during recursion are complete
   * @throws javax.naming.NamingException
   */
  private HashMap<CellID, Set<MetadataID>> search(LinkedList<pairCidAndCtx> cids, ArrayList<String> filters, HashMap<CellID, Set<MetadataID>> results) throws NamingException {
    pairCidAndCtx pair = cids.poll();
    if(pair == null){
      // no other cells to search
      return results;
    }
    logger.info("current scope: at cid:" + pair.cid);
    String cellScope = EADSUtils.CELL_ID_ATTR + "=" + pair.cid;
    //    DirContext cellCtx = (DirContext) pair.ctx.lookup(EADSUtils.CELL_ID_ATTR + "="+pair.cid);
    // get any sub-cells, add to list for later recursions
    getAllCells(pair.ctx, cellScope, cids);

    // prepare to search this cell for metadata that matches filters
    SearchControls ctls = new SearchControls();
    // will store the metadata ids
    String[] returnIds = {EADSUtils.METADATA_ID_ATTR};
    ctls.setReturningAttributes(returnIds);
    Set<MetadataID> matches = new HashSet<MetadataID>();

    // break the loop and set to false if cell fails a filter
    boolean cellHitAllFilters = true;
    // check all filters
    logger.info("got any children cells, prepare to search for " + filters.size() + " filters");
    for(String filter:filters){
      logger.info("checking filter " + filter);
      boolean hitFilter = false;
      NamingEnumeration hits = pair.ctx.search(cellScope, filter, ctls);
      while(hits.hasMore()){
        // cell has at least one hit for this filter
        hitFilter = true;
        SearchResult sr = (SearchResult)hits.next();
        logger.info("hit >>>" + sr.getName());
        // log the mid for all hits
        for (NamingEnumeration e = sr.getAttributes().getAll(); e.hasMore();){
          Attribute attr = (Attribute)e.next();
          MetadataID mid = new MetadataID(Integer.parseInt((String)attr.get()));
          matches.add(mid);
        }
      }
      if(!hitFilter){
        cellHitAllFilters = false;
        logger.info("failed to match " + filter);
        break;
      }
    }

    if(cellHitAllFilters){
      results.put(new CellID(pair.cid), matches);
    }


    return  search(cids, filters, results);
  }

  /**
   * Implementation for Backend interface.
   *
   * Search all cells in the world, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping CellID's whose metadata that matched the
   * search, to a set of MetadataID's that matched the search for that cell.
   */
  public HashMap<CellID, Set<MetadataID> > searchMetadata(MetadataSearchFilters filters){
    LinkedList<pairCidAndCtx> cids = new LinkedList<pairCidAndCtx>();
    HashMap<CellID, Set<MetadataID> > results = null;

    // the only difference between the two search Metadata methods is here
    // this 'all' version gets all cells for searching

    getAllCells(rootCtx, wlCtxDN, cids);
    ArrayList<String> filterList = EADSUtils.convertFiltersToLDAP(filters);
    logger.info("search Metadata for " + filterList.size() + " converted filters");
    logger.fine("[EADS] ================verbose ldap contents ================:");
    EADSUtils.printContentsVerbose(rootCtx, wlCtx, 0);
    try {
      results = search(cids, filterList, new HashMap<CellID, Set<MetadataID>>());
    } catch (NamingException ex) {
      logger.severe("[EADS] error searching for metadata " +
                  " with filters: ");
      for(String str:filterList){
        logger.severe(str);
      }
    }
    return results;
  }

  /**
   * Search all cells beneath cid, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping CellID's whose metadata that matched the
   * search, to a set of MetadataID's that matched the search for that cell.
   */
  public HashMap<CellID, Set<MetadataID> > searchMetadata(MetadataSearchFilters filters, CellID cid){
    LinkedList<pairCidAndCtx> cids = new LinkedList<pairCidAndCtx>();
    HashMap<CellID, Set<MetadataID> > results = null;

    // the only difference between the two search Metadata methods is here
    // this 'scoped' version gets only cells below a certain parent for searching
    String dn = getCellDN(cid);
    ArrayList<String> filterList = EADSUtils.convertFiltersToLDAP(filters);
    try {
      getAllCells(rootCtx, dn, cids);    
      logger.info("search Metadata for " + filterList.size() + " filters");

      results = search(cids, filterList, new HashMap<CellID, Set<MetadataID>>());
    } catch (Exception ex) {
      logger.severe("[EADS] error searching for metadata " +
                  " scoped under cellID: " + cid + " with filters: " + filters);
      logger.severe("Exception: " + ex);
      for(String str:filterList){
        logger.severe(str);
      }
    }
    return results;
  }

  
  



    /** The directory service */
    public DirectoryService service;

    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition( String partitionId, String partitionDn ) throws Exception
    {
        // Create a new partition named 'foo'.
        Partition partition = new JdbmPartition();
        partition.setId( partitionId );
        partition.setSuffix( partitionDn );
        service.addPartition( partition );

        return partition;
    }


  /**
   * Add a new set of index on the given attributes
   *
   * @param partition The partition on which we want to add index
   * @param attrs The list of attributes to index
   */
  private void addIndex( Partition partition, String... attrs ){
      // Index some attributes on the apache partition
      HashSet<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();

      for ( String attribute:attrs ){
          indexedAttributes.add( new JdbmIndex<String,ServerEntry>( attribute ) );
      }

      ((JdbmPartition)partition).setIndexedAttributes( indexedAttributes );
  }


  /**
   * Initialize the server. It creates the partition, add the index, and
   * inject the context entries for the created partitions.
   *
   * @throws Exception if there were some problems why initializing the system
   */
  private void initLDAPServer(boolean reset) throws Exception{
    // Initialize the LDAP service
    // bash any old jdbm files
    // TODO
    // this will be broken if darkstar and the web server are ever on different
    // servers. This needs to be dealt with using RESTful services
    // left to its own devices, the directory service would choose something
    // like :.wonderland-server/0.5-dev/run/darkstar_server/run/core/data/sgs/server-work
    File jdbmFolder = new File("../../../../../metadata-module-db");
    //    logger.info("erase any old jbdm files");
    if(reset){
      logger.info("rebuilding");
      if(!EADSUtils.deleteDir(jdbmFolder)){
        logger.info("failed to delete jbdm folder!");
      } else {
        logger.info("deleted jbdm");
      }
    }
    else{
      logger.info("rebuild self");
    }
    logger.info("create dir service");
    service = new DefaultDirectoryService();
//      logger.info("Val of working diretory:" + service.getWorkingDirectory());
//      logger.info("abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());

    // set jdbm working directory
    logger.info("ORIGINAL abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());
    logger.info("ORIGINAL can path of working diretory:" + service.getWorkingDirectory().getCanonicalPath());
    service.setWorkingDirectory(jdbmFolder);
    logger.info("abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());
    logger.info("can path of working diretory:" + service.getWorkingDirectory().getCanonicalPath());

    // Disable the ChangeLog system
    service.getChangeLog().setEnabled( false );
    service.setDenormalizeOpAttrsEnabled( true );

    logger.info("create default partitions");
    // TODO make this name the name of the WL server
    Partition worldPartition = addPartition( "world", wlCtxDN);

    // Index some attributes on the world partition
    addIndex( worldPartition, "objectClass", "ou", "uid" );

    // And start the service
    service.startup();

    // get registries for checking whether attributes/object classes have been registered
    SchemaService ss = service.getSchemaService();
    Registries r = ss.getRegistries();
    attributeTypes = r.getAttributeTypeRegistry();
    objectClasses = r.getObjectClassRegistry();

    // Inject the world root entry if it does not already exist
    try
    {
        service.getAdminSession().lookup( worldPartition.getSuffixDn() );
    }
    catch ( LdapNameNotFoundException lnnfe )
    {
        LdapDN rootDN = new LdapDN( wlCtxDN );
        ServerEntry rootEntry = service.newEntry( rootDN );
        rootEntry.add( "objectClass", "top", "domain", "dcObject" );
        rootEntry.add( "dc", "wonderland" );
        service.getAdminSession().add( rootEntry );
    }

    // set up top-level contexts
    // prepare environement variables
    Hashtable<Object, Object> env = new Hashtable<Object, Object>();
    env.put(DirectoryService.JNDI_KEY, service);
    env.put(Context.PROVIDER_URL, "");
    env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());
    env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    // create context
    rootCtx = new InitialDirContext(env);
    wlCtx = (DirContext) rootCtx.lookup(wlCtxDN);
    logger.info("[EADS] initial contexts prepared");

    if(reset == false){
      logger.info("[EADS] completed setup, do not need to rebuild db");
      return;
    }

    // register interior attributes
    logger.info("[EADS] register default attributes");
    logger.info("[EADS] dummy");
    // prepare attribute names
    ArrayList<String> cidNames = new ArrayList<String>();
    cidNames.add(EADSUtils.CELL_ID_STR);
    ArrayList<String> midNames = new ArrayList<String>();
    midNames.add(EADSUtils.METADATA_ID_STR);
    try{

      registerAttribute(cidNames, "cellID from darkstar", Datatype.INTEGER, this.getClass());
      registerAttribute(midNames, "ID assigned to metadata obj", Datatype.INTEGER, this.getClass());
    }
    catch(NamingException e){
      logger.info("[EADS] default attributes already registered, exception:" + e);
    }

    // register cell object class
    ArrayList<String> classes = new ArrayList<String>();
    classes.add("top");
    ArrayList<String> mustAttrs = new ArrayList<String>();
    mustAttrs.add(EADSUtils.CELL_ID_ATTR);
    ArrayList<String> mayAttrs = new ArrayList<String>();
    try{
      registerObjectClass(EADSUtils.CELL_OC, "Represents a cell, will have metadata below it", classes, ObjectClassType.STRUCTURAL, mustAttrs, mayAttrs);
      // register metadata object class (added to all metadata objects in addMetadata,
      // forces them to have an mID and acts as a flag that this object is metadata
      mustAttrs.clear();
      mustAttrs.add(EADSUtils.METADATA_ID_ATTR);
      // classes = top, may attrs is empty
      registerObjectClass(EADSUtils.METADATA_AUX_OC, "auxiliary metadata type adds MID", classes, ObjectClassType.AUXILIARY, mustAttrs, mayAttrs);
    }
    catch(NamingException e){
      logger.info("[EADS] exception:" + e);
    }  
  }
  
}
