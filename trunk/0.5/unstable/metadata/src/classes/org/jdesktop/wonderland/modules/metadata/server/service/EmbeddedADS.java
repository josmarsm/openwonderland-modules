/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.server.service;


import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
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
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;

/**
 *
 * @author mabonner
 */
public class EmbeddedADS implements MetadataBackendInterface
{
  /** The package name. */
  private static final String PKG_NAME = "org.jdesktop.wonderland.modules.metadata.server.service";
  /** the logger for this class */
//  private static final LoggerWrapper logger =
//        new LoggerWrapper(Logger.getLogger(PKG_NAME));
  private static Logger logger = Logger.getLogger(EmbeddedADS.class.getName());

  /** OID to register new object classes (metadata subtypes) and attributes*/
  final static String SunOID = "1.3.6.1.4.1.42";
  // note: all remaining OID's are invented
  // pick a longish random number for sunlabs
  final static String SunLabsOID = SunOID + ".12341234";
  final static String WonderlandOID = SunLabsOID + ".0";
  final static String WonderlandModOID = WonderlandOID + ".1";
  final static String MetaModOID = WonderlandModOID + ".0";
  final static String MetaObjClassOID = MetaModOID + ".0";
  final static String MetaAttrOID = MetaModOID + ".1";

  /**
   * LDAP syntax constants
   */
  private final static String integerSyntax = "EQUALITY integerMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.27 SINGLE-VALUE";
  private final static String stringSyntax = "EQUALITY caseIgnoreMatch SUBSTR caseIgnoreSubstringsMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE";
  private final static String dateSyntax = "EQUALITY generalizedTimeMatch ORDERING generalizedTimeOrderingMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 SINGLE-VALUE";

  /**
   * LDAP syntax, used to register new object classes
   * An instantiated object must have exactly one STRUCTURAL object class.
   */
  public enum ObjectClassType { STRUCTURAL, AUXILIARY }

  /** used to complete OIDS for new object classes (metadata subtypes). incremented to keep unique. */
  static int ocCount = 0;
  /** used to complete OIDS for new attributes. incremented to keep unique. */
  static int attrCount = 0;
  /** used to complete OIDS for each metadata instance. incremented to keep unique. */
  // this is handled in the Metadata class now
  //  static int metaCount = 0;

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

  static final DateFormat ldapDateFormat = new SimpleDateFormat("yyyyMMddhhmmssZ");

  /**
   * stores attribute names that have already been registered
   */
  private HashMap<String, Datatype> attrNames = new HashMap<String, Datatype>();

  /**
   * auxiliary object class name - added to each metadata object in addMetadata
   */
  private static String METADATA_AUX_OC = "metadata";

  /**
   * default object class name - cells will have metadata 'underneath' them in ldap
   */
  private static String CELL_OC = "cell";



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
  public void addMetadata(CellID cid, MetadataSPI metadata) {
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
    String nameLdap = classnameToLDAP(metadata.getClass().getName());
    metaOC.add(nameLdap);
    metaOC.add(METADATA_AUX_OC);
    attrs.put(metaOC);
    logger.info("[EADS] adding metadata of type " + nameLdap +
            " to cell " + cid);

    // this class's attributes
    for(Entry<String, MetadataValue> e : metadata.getAttributes()){
      String attrName = attrnameToLDAP(e.getKey());
      MetadataValue mv = e.getValue();
      String val = mv.getVal();
      logger.info("[EADS] attribute " + attrName + " with raw value '" +
              val + "', expected type of " + mv.type);
      BasicAttribute attr = new BasicAttribute(attrName);

      // the value
      switch (mv.type){
        case DATE:
          // can't be a null value
          if(nullAttrCheck(mv)){
            Date d = new Date(0);
            val = Metadata.dateFormat.format(d);
            break;
          }
          // else, convert to ldap date
          try {
            Date d = Metadata.dateFormat.parse(val);
            val = ldapDateFormat.format(d);
          } catch (ParseException ex) {
            logger.severe("[EADS] invalid date syntax adding metadata" +
                    " to cell id: " + cid + ", string:" + val);
          }
          break;
        case INTEGER:
          // can't be a null value
          if(nullAttrCheck(mv)){
            val = "0";
          }
          break;
        case STRING:
          // can't be a null value
          if(nullAttrCheck(mv)){
            val = " ";
          }
      }

      // add to collection
      attr.add(val);
      attrs.put(attr);
    }
    try {
      // finally, add the new entry
      cellCtx.createSubcontext("mID=" + metadata.getID(), attrs);
    } catch (NamingException ex) {
      logger.info("[EADS] error adding metadata of type " + nameLdap +
            " to cell " + cid + ", ex:\n" + ex);
    }

    logger.info("[EADS] AFTER ADDING METADATA, CONTENTS:");
    logger.info("[EADS] ================ldap contents of cell ================:");
    printContents(rootCtx, wlCtxDN);
    logger.info("[EADS] ================verbose ldap contents of cell ================:");
    printContentsVerbose(rootCtx, cellCtx, 0);
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
    objclass.add("cell");
    attrs.put(objclass);
    try {
      // Create the context
      parentCtx.createSubcontext("cID=" + cid, attrs);
    } catch (NamingException ex) {
      logger.severe("[EADS] error adding cell id: " + cid + ", ex:" + ex);
    }
  }

  /**
   * searches ldap tree for cell with given ID.
   * @param cid cell id to find
   * @return the cell's DN
   */
  private String getCellDN(CellID cid){
    String filter = "(&(objectclass=cell)(cid="+ cid +"))";
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



  public void eraseMetadata(int mid){
    // find the metadata cell
    String filter = "(&(mid="+ mid + "))";
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
    logger.severe("[EADS] clear cell metadata, cell id: " + cid);
    logger.info("***************************");
    printContents(rootCtx, wlCtxDN);
    logger.info("***************************");
    // get cell context
    DirContext cellCtx = getCellCtx(cid);
    // get all its metadata
    String filter = "(&(mid=*))";
    SearchControls ctls = new SearchControls();
    ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    NamingEnumeration results = null;
    try {
      results = cellCtx.search(getCellDN(cid), filter, ctls);
      logger.info("[EADS] clear cell metadata, search " + getCellDN(cid)
              + " for metadata");
      while(results.hasMore()){
        SearchResult si = (SearchResult)results.next();
        cellCtx.destroySubcontext(si.getName());
      }
    } catch (NamingException ex) {
      logger.severe("[EADS] error clearing metadata of cell with id:" + cid);
    }
    if(results == null){
      logger.info("[EADS] no metadata on this cell!");
    }
    logger.info("----------------------------");
    printContents(rootCtx, wlCtxDN);
    logger.info("-----------------------------");
  }

  /**
   * Take any action necessary to register this metadatatype as an option.
   * Name collision on class name or attribute name is up to the implementation.
   *
   * This implementation uses the full package name to describe a Metadata obj
   * and its attributes, avoiding collisions.
   *
   * TODO will scan class loader take care of duplication checking anyway?
   * @param m example of the type to register
   */
  public void registerMetadataType(MetadataSPI m) throws Exception {
    // build required elements for LDAP
    logger.info("[EADS] registering new metadata type");
    // NAME our unique class name
    // convert to ldap friendly name
    String nameLdap = classnameToLDAP(m.getClass().getName());
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
    mustLdap.add("metadataID");
    String attrName = "";
    for(Entry<String, MetadataValue> e : m.getAttributes()){
      try{
      // make LDAP friendly
        attrName = attrnameToLDAP(e.getKey());
  //      logger.info("Key, Val: " + e.getKey() + ", " + e.getValue());
        MetadataValue mv = e.getValue();
        registerAttribute("('" + attrName + "')" , "registered attribute #" + attrCount, e.getValue().type);
        mustLdap.add(attrName);
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
    registerObjectClass(nameLdap, descLdap, supersLdap, ObjectClassType.STRUCTURAL, mustLdap, mayLdap);

  }

  /**
   * Takes in array of object classes or attribute types, builds an RFC 4512
   * compliant grouping of them, e.g. "( 'val' $ 'val2' $ 'val3' )"
   * @param classes
   * @return
   */
  private String buildObjList(ArrayList<String> ids) {
    // prepare classes
    String res = "( ";
    int count = 0;
    for(String s:ids){
      res += s;
      count += 1;
      if(count < ids.size()){
        res += " $ ";
      }
    }
    res += " )";
    return res;
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

  private boolean nullAttrCheck(MetadataValue mv) {
//    logger.fine("[EADS] null attr check");
    if(mv == null){
      logger.severe("[EADS] null attr check.. mv itself is null! bad!");
    }
    if(mv.getVal() == null || mv.getVal().equals("")){
      return true;
    }
    return false;
  }

  /** register a new attribute with the embedded LDAP server
   * builds an RFC 4512 syntax string out of parameters
   *
   * @param name Name of the new attribute. Multiple values must follow RFC 4512
   * syntax, e.g. pass in "('name1' 'name2')" to add two values
   * @param desc DESC of the new attribute
   * @param type determines the syntax and comparison rules that will be used
   */
  private void registerAttribute(String name, String desc, Datatype type) throws NamingException {
    // check if this name is already in use..
    if(attrNames.containsKey(name)){
      // if it is already in use for the same type of data, don't recreate it
      if(attrNames.get(name) == type){
        logger.info("[EADS] re-using attribute " + name + " with type "
                + type);
        return;
      }
      else{
        // this is an unrecoverable collision
        // TODO
        // in the future, could uniqueify names the same way metadata types are
        // given unique names
        logger.severe("[EADS]Could not register attribute name "+
                name + " of type " + type + ", already in use for type " +
                attrNames.get(name));
        throw(new NamingException("attribute name collision"));
      }
    }
    desc = "'" + desc + "'";
    // if there was only one name, add 's around it
    // otherwise, assume it is already RFC 4512 compliant
    if(name.indexOf("(") == -1){
      name = "'" + name + "'";
    }
    String attr = "( " + MetaAttrOID + "." + attrCount + " NAME " + name + " DESC " + desc + " ";
    String syntax = "";
    switch (type){
      case INTEGER:
        syntax = integerSyntax;
        break;
      case STRING:
        syntax = stringSyntax;
        break;
      case DATE:
        syntax = dateSyntax;
        break;

    }

    attr += syntax + ")";
    Attributes newAttribute = new BasicAttributes(true);
    newAttribute.put("attributeTypes", attr);
    logger.info("[EADS] registering new attribute: " + name + " with type " + type);
    logger.fine("[EADS] full info: " + newAttribute);
    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, newAttribute);
    attrCount += 1;
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
   * @param name Name of the new attribute. Multiple values must follow RFC 4512
   * syntax, e.g. pass in "('name1' 'name2')" to add two values
   * @param desc DESC of the new attribute
   * @param classes superclasses to add to objectClass
   * @param mustAttrs attributes the class must have
   * @param mayAttrs attributes the class may have
   */
  private void registerObjectClass(String name, String desc, ArrayList<String> objClasses, ObjectClassType ocType, ArrayList<String> mustAttrs, ArrayList<String> mayAttrs) throws NamingException {

    // if there was only one name, add 's around it
    // otherwise, assume it is already RFC 4512 compliant
    if(name.indexOf("(") == -1){
      name = "'" + name + "'";
    }

    String must = buildObjList(mustAttrs);

    Attributes ocAttrs = new BasicAttributes(true);
    String oc = "( " + MetaObjClassOID + "." + (ocCount++) + " NAME " + name;
    if(desc != null){
      desc = "'" + desc + "'";
      oc += " DESC " + desc;
    }
    if(!objClasses.isEmpty()){
      oc += " SUP " + buildObjList(objClasses);
    }
    oc += " " + ocType;
    if(!mustAttrs.isEmpty()){
      oc += " MUST " + must;
    }
    if(!mayAttrs.isEmpty()){
      oc += " MAY " + buildObjList(mayAttrs);
    }
    oc += " )";
    ocAttrs.put("objectClasses", oc);
    logger.info("[EADS] registering object class:\n" + oc);
    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, ocAttrs);
  }

  /**
   * convert a full classname like org.site.module.class to an ldap friendly
   * version by replacing all "." with "-", e.g. returns "org-site-module-class"
   * @param str the name to convert
   * @return
   */
  private String classnameToLDAP(String str) {
    return str.replaceAll("\\.", "-");
  }

  /**
   * convert an attribute name like "date created" to an ldap friendly
   * version by replacing all " " with "-", e.g. returns "date-created"
   * @param str the attribute to convert
   * @return
   */
  private String attrnameToLDAP(String str) {
    return str.replaceAll(" ", "-");
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



  private static void printResults(HashMap<Integer, Set<Integer>> results) {
    for(Entry<Integer, Set<Integer> > e : results.entrySet()){
            logger.info("Key, Val: " + e.getKey() + ", " + e.getValue());
    }
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
    String f1 = "(&(objectclass=cell)(cid=*))";
    // will store their cID's
    String[] attrIDs = {"cid"};
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
   * (objectclass=cell), and search these cells for metadata matching each
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
  private HashMap<CellID, Set<Integer>> search(LinkedList<pairCidAndCtx> cids, ArrayList<String> filters, HashMap<CellID, Set<Integer>> results) throws NamingException {
    pairCidAndCtx pair = cids.poll();
    if(pair == null){
      // no other cells to search
      return results;
    }
    logger.info("current scope: at cid:" + pair.cid);
    String cellScope = "cid="+pair.cid;
    //    DirContext cellCtx = (DirContext) pair.ctx.lookup("cid="+pair.cid);
    // get any sub-cells, add to list for later recursions
    getAllCells(pair.ctx, cellScope, cids);

    // prepare to search this cell for metadata that matches filters
    SearchControls ctls = new SearchControls();
    // will store the metadata ids
    String[] returnIds = {"mid"};
    ctls.setReturningAttributes(returnIds);
    Set<Integer> matches = new HashSet<Integer>();

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
          Integer mid = Integer.parseInt((String)attr.get());
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
   * @return map, mapping cell id's (as Integers) whose metadata that matched the
   * search, to a set of metadata id's that matched the search for that cell.
   */
  public HashMap<CellID, Set<Integer> > searchMetadata(MetadataSearchFilters filters){
    LinkedList<pairCidAndCtx> cids = new LinkedList<pairCidAndCtx>();
    HashMap<CellID, Set<Integer> > results = null;

    // the only difference between the two search Metadata methods is here
    // this 'all' version gets all cells for searching

    getAllCells(rootCtx, wlCtxDN, cids);
    ArrayList<String> filterList = convertFiltersToLDAP(filters);
    logger.info("search Metadata for " + filterList.size() + " converted filters");
    logger.fine("[EADS] ================verbose ldap contents ================:");
    printContentsVerbose(rootCtx, wlCtx, 0);
    try {
      results = search(cids, filterList, new HashMap<CellID, Set<Integer>>());
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
   * @return map, mapping cell id's (as Integers) whose metadata that matched the
   * search, to a set of metadata id's that matched the search for that cell.
   */
  public HashMap<CellID, Set<Integer> > searchMetadata(MetadataSearchFilters filters, CellID cid){
    LinkedList<pairCidAndCtx> cids = new LinkedList<pairCidAndCtx>();
    HashMap<CellID, Set<Integer> > results = null;

    // the only difference between the two search Metadata methods is here
    // this 'scoped' version gets only cells below a certain parent for searching
    String dn = getCellDN(cid);
    ArrayList<String> filterList = convertFiltersToLDAP(filters);
    try {
      getAllCells(rootCtx, dn, cids);    
      logger.info("search Metadata for " + filterList.size() + " filters");

      results = search(cids, filterList, new HashMap<CellID, Set<Integer>>());
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

  /**
   * Convert a MetadataSearchFilters object into an LDAP search filters string
   *
   * @return the list of filters, in RFC 2254 format
   */
  private ArrayList<String> convertFiltersToLDAP(MetadataSearchFilters filters){
    ArrayList<String> result = new ArrayList<String>();
    // example filter
    // (&(objectclass=org-jdesktop-wonderland-modules-metadata-common-Metadata)(someAttr=hello hello));
    for(MetadataSPI m:filters.getFilters()){
      String newFilter = "(&(objectclass=";
      // add the object class
      String nameLdap = classnameToLDAP(m.getClass().getName());
      newFilter += nameLdap + ")";
      logger.fine("[EADS] oc filter in LDAP is: " + nameLdap);
      // add attributes
      newFilter += attributesToFilterString(m.getAttributes());
      newFilter += ")";
      logger.fine("[EADS] final filter: " + newFilter);
      result.add(newFilter);
    }
    return result;
  }

  private String attributesToFilterString(Set<Map.Entry<String, MetadataValue>> attrs){
    String res = "";
    for(Entry<String, MetadataValue> e: attrs){
      String attrName = attrnameToLDAP(e.getKey());
      logger.info("[EADS] making attr from Key, Val: " + e.getKey() + ", " + e.getValue());
      MetadataValue mv = e.getValue();
      String val = mv.getVal();
      switch (mv.type){
        case DATE:
          // can't be a null value
          if(nullAttrCheck(mv)){
            logger.fine("[EADS] wildcard date");
            val = "*";
            break;
          }
          // convert to ldap date
          try {
            Date d = Metadata.dateFormat.parse(val);
            val = ldapDateFormat.format(d);
          } catch (ParseException ex) {
            logger.severe("[EADS] invalid date syntax creating filter!");
          }
          break;
        case INTEGER:
          // can't be a null value
          if(nullAttrCheck(mv)){
            logger.fine("[EADS] wildcard int");
            val = "*";
          }
          break;
        case STRING:
          // can't be a null value
          if(nullAttrCheck(mv)){
            logger.fine("[EADS] wildcard string");
            val = "*";
          }
      }
      res += "(" + attrName + "=" + val + ")";
    }
    logger.fine("[EADS] resulting attrs filter:" + res);
    return res;
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
  private void addIndex( Partition partition, String... attrs )
  {
      // Index some attributes on the apache partition
      HashSet<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();

      for ( String attribute:attrs )
      {
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
  private void initLDAPServer() throws Exception
  {
    // Initialize the LDAP service
    // bash any old jdbm files
    // TODO
    // this will be broken if darkstar and the web server are ever on different
    // servers. This needs to be dealt with using RESTful services
    // left to its own devices, the directory service would choose something
    // like :.wonderland-server/0.5-dev/run/darkstar_server/run/core/data/sgs/server-work
    File jdbmFolder = new File("../../../../../metadata-module-db");
    deleteDir(jdbmFolder);
    logger.info("create dir service");
    service = new DefaultDirectoryService();
//      logger.info("Val of working diretory:" + service.getWorkingDirectory());
//      logger.info("abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());
    logger.info("erase any old jbdm files");
    // set jdbm working directory
    logger.info("ORIGINAL abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());
    logger.info("ORIGINAL can path of working diretory:" + service.getWorkingDirectory().getCanonicalPath());
    service.setWorkingDirectory(jdbmFolder);
    logger.info("abs path of working diretory:" + service.getWorkingDirectory().getAbsolutePath());
    logger.info("can path of working diretory:" + service.getWorkingDirectory().getCanonicalPath());

    // Disable the ChangeLog system
    service.getChangeLog().setEnabled( false );
    service.setDenormalizeOpAttrsEnabled( true );

    // TODO make this name the name of the WL server
    Partition worldPartition = addPartition( "world", wlCtxDN);

    // Index some attributes on the apache partition
    addIndex( worldPartition, "objectClass", "ou", "uid" );

    // And start the service
    service.startup();


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
    // register interior attributes
    registerAttribute("('cellID' 'cID')", "cellID from darkstar", Datatype.INTEGER);
    registerAttribute("('metadataID' 'mID')" , "ID assigned to metadata obj", Datatype.INTEGER);

    // register cell object class
    ArrayList<String> classes = new ArrayList<String>();
    classes.add("top");
    ArrayList<String> mustAttrs = new ArrayList<String>();
    mustAttrs.add("cID");
    ArrayList<String> mayAttrs = new ArrayList<String>();

    registerObjectClass(CELL_OC, "Represents a cell, will have metadata below it", classes, ObjectClassType.STRUCTURAL, mustAttrs, mayAttrs);

    // register metadata object class (added to all metadata objects in addMetadata,
    // forces them to have an mID and acts as a flag that this object is metadata
    mustAttrs.clear();
    mustAttrs.add("mID");
    // classes = top, may attrs is empty
    registerObjectClass(METADATA_AUX_OC, "auxiliary metadata type adds MID", classes, ObjectClassType.AUXILIARY, mustAttrs, mayAttrs);
  }


  /**
   * Creates a new instance of EmbeddedADS. It initializes the directory service.
   *
   * @throws Exception If something went wrong
   */
  public EmbeddedADS() throws Exception
  {
    logger.log(Level.CONFIG, "[EADS] creating EADS");
    initLDAPServer();
    logger.log(Level.CONFIG, "[EADS] created");
  }

  /**
   * Creates a new instance of EmbeddedADS. It initializes the directory service.
   *
   * Convenience method to initialize with some set types.
   *
   * @throws Exception If something went wrong
   */
  public EmbeddedADS(ArrayList<MetadataSPI> metadata) throws Exception
  {
    logger.log(Level.CONFIG, "[EADS] creating EADS");
    initLDAPServer();
    logger.log(Level.CONFIG, "[EADS] register metadata types..");
    for(MetadataSPI m:metadata){
      registerMetadataType(m);
    }
    logger.log(Level.CONFIG, "[EADS] created");
  }



  // Deletes all files and subdirectories under dir.
  // Returns true if all deletions were successful.
  // If a deletion fails, the method stops attempting to delete and returns false.
  private static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
    String[] children = dir.list();
    for (int i=0; i<children.length; i++) {
     boolean success = deleteDir(new File(dir, children[i]));
      if (!success) {
        return false;
      }
    }
   }

    // The directory is now empty so delete it
    return dir.delete();
  }

  /**
   *
   * prints out contents of ctx under scope, prints out only the DN's, no other
   * attributes are printed. To print attributes, use printContentsVerbose
   * 
   * @param ctx context to begin search in
   * @param scope bound name in ctx to search in
   * @throws javax.naming.NamingException
   */
  private void printContents(DirContext ctx, String scope) {
    try {
      // get all cells
      String filter = "(&(objectclass=cell)(cid=*))";
      SearchControls ctls = new SearchControls();
      ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      NamingEnumeration results = ctx.search(scope, filter, ctls);
      if (results == null) {
        logger.fine("no cells!");
      }
      // get metadata of each cell, print it too
      String filter2 = "(&(objectclass=metadata)(mid=*))";
      ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      while (results.hasMore()) {
        SearchResult si = (SearchResult) results.next();
        String dn = si.getName();
        logger.fine("cell dn is:" + dn);
        DirContext cellCtx = (DirContext) ctx.lookup(dn);
        NamingEnumeration metaResults = ctx.search(dn, filter2, ctls);
        logger.fine("print that cell's metadata");
        while (metaResults.hasMore()) {
          SearchResult meta = (SearchResult) metaResults.next();
          logger.fine("  " + "metadata name is :" + meta.getName());
        }
        logger.fine("done");
      }
    } catch (NamingException ex) {
      logger.severe("[EADS] error printing contents, ex:" + ex);
    }
  }

  /**
   * debugging method, prints out all LDAP contents int topCtx under ctx
   * @param topCtx top/root context in which names (like those bound in ctx) may be looked up
   * @param ctx context to begin listing bound names in
   * @param level used to put spaces in front of print statements based on recursion level
   * @throws javax.naming.NamingException
   */
  private static void printContentsVerbose(DirContext topCtx, DirContext ctx, int level) {
    try {
      NamingEnumeration list = ctx.listBindings("");
      String spacer = "";
      for (int i = 0; i < level; i++) {
        spacer += " ";
      }
      while (list.hasMore()) {
        NameClassPair nc = (NameClassPair) list.next();
        logger.fine(spacer + nc);
        logger.fine(spacer + "name is :" + nc.getName());
        try {
          DirContext subCtx = (DirContext) topCtx.lookup(nc.getName());
          printContentsVerbose(topCtx, subCtx, level + 4);
        } catch (LdapNameNotFoundException lnnfe) {
          logger.fine("end of line");
        }
      }
      Attributes attrs = ctx.getAttributes("");
    } catch (NamingException ex) {
      logger.severe("[EADS] error printing verbose contents, ex:" + ex);
    }
  }
}
