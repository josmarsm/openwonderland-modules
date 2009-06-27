/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.server.service;

import com.sun.sgs.impl.sharedutil.LoggerWrapper;

import java.io.File;
import java.util.ArrayList;
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
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;

/**
 *
 * @author mabonner
 */
public class EmbeddedADS
{
  // TODO put this into the interface/abstract class
  /** The package name. */
  private static final String PKG_NAME = "org.jdesktop.wonderland.modules.metadata.server.service";
  /** the logger for this class */
  private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(PKG_NAME));

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

  // LDAP syntax constants
  private final static String integerSyntax = "EQUALITY integerMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.27 SINGLE-VALUE";
  private final static String stringSyntax = "EQUALITY caseIgnoreMatch SUBSTR caseIgnoreSubstringsMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE";
  private final static String dateSyntax = "EQUALITY generalizedTimeMatch ORDERING generalizedTimeOrderingMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 SINGLE-VALUE";

  /** used to complete OIDS for new object classes (metadata subtypes). incremented to keep unique. */
  static int ocCount = 0;
  /** used to complete OIDS for new attributes. incremented to keep unique. */
  static int attrCount = 0;
  /** used to complete OIDS for each metadata instance. incremented to keep unique. */
  static int metaCount = 0;

  // contexts used as bases to add new metadata, attributes, etc
  /** the very top context, containing all others, including schemas and various
   *  book-keeping trees set up by ApacheDS
   */
  static DirContext rootCtx;
  /** immediately below root, contains all cells and metadata */
  static DirContext topCtx;

  void addMetadata(CellID id, Metadata metadata) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public void eraseCell(CellID id) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public void eraseMetadataForCell(CellID id) {
    throw new UnsupportedOperationException("Not yet implemented");
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
  public void registerMetadataType(Metadata m) {
    // register all attributes
    for(Entry<String, MetadataValue> e : m.getAttributes()){
            logger.log(Level.INFO, "Key, Val: " + e.getKey() + ", " + e.getValue());

            
      }

    // for the moment, treat them all as strings
    // convert to ldap friendly name
    String ldapName = m.getClass().getName().replaceAll("\\.", "-");
    Class superClass = m.getClass().getSuperclass();
    String superName = superClass.getName();
    while(!superName.equals("java.lang.Object")){
      System.out.println(superName);
      superClass = superClass.getSuperclass();
      superName = superClass.getName();
    }
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
      res += "'"+s+"'";
      count += 1;
      if(count < ids.size()){
        res += " $ ";
      }
    }
    res += " )";
    return res;
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
    desc = "'" + desc + "'";
    // if there was only one name, add 's around it
    // otherwise, assume it is already RFC 4512 compliant
    if(name.indexOf("(") == -1){
      name = "'" + name + "'";
    }
    String attr = "( " + MetaAttrOID + "." + attrCount + " NAME " + name + " DESC " + desc;
    String syntax = "";
    switch (type){
      case INTEGER: syntax = integerSyntax;
      case STRING: syntax = stringSyntax;
      case DATE: syntax = dateSyntax;

    }

    attr += syntax + ")";



    Attributes newAttribute = new BasicAttributes(true);
    newAttribute.put("attributeTypes", attr);
    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, newAttribute);

    attrCount += 1;
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
   * @param classes items to add to objectClass
   * @param mustAttrs attributes the class must have
   * @param mayAttrs attributes the class may have
   */
  private void registerObjectClass(String name, String desc, ArrayList<String> objClasses, ArrayList<String> mustAttrs, ArrayList<String> mayAttrs) throws NamingException {
    desc = "'" + desc + "'";
    // if there was only one name, add 's around it
    // otherwise, assume it is already RFC 4512 compliant
    if(name.indexOf("(") == -1){
      name = "'" + name + "'";
    }

    String classes = buildObjList(objClasses);
    String must = buildObjList(mustAttrs);
    String may = buildObjList(mayAttrs);
    
    Attributes ocAttrs = new BasicAttributes(true);
    String oc = "( " + MetaObjClassOID + "." + ocCount + " NAME " + name + 
            " DESC " + desc + " SUP " + classes + "STRUCTURAL MUST " + must +
            " MAY " + may + " )";

    rootCtx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, ocAttrs);
    ocCount +=1;
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

  private static void printOutContents(DirContext topCtx, DirContext ctx, int level) throws NamingException {
      NamingEnumeration list = ctx.listBindings("");
      String spacer = "";
      for(int i=0; i< level;i++){
        spacer += " ";
      }
      while (list.hasMore()) {
          NameClassPair nc = (NameClassPair)list.next();
          logger.log(Level.INFO, spacer + nc);
          logger.log(Level.INFO, spacer + "name is :" + nc.getName());
          try{
            DirContext subCtx = (DirContext) topCtx.lookup(nc.getName());
            printOutContents(topCtx, subCtx, level + 4);
          }
          catch ( LdapNameNotFoundException lnnfe ){
            logger.log(Level.INFO, "end of line");
          }

      }
      Attributes attrs = ctx.getAttributes("");
  }

  private static void printResults(HashMap<Integer, Set<Integer>> results) {
    for(Entry<Integer, Set<Integer> > e : results.entrySet()){
            logger.log(Level.INFO, "Key, Val: " + e.getKey() + ", " + e.getValue());
    }
  }

  /**
   * Searches for all cells present at ctx, adds them to cids.
   * @param ctx Context in which to scope search for cells
   * @param scope name in ctx in which to search for cells
   * @param cids found cell cellID's are added to this list
   * @throws javax.naming.NamingException
   */
  private void getAllCells(DirContext ctx, String scope, LinkedList<pairCidAndCtx> cids) throws NamingException {
    // get all cells
    String f1 = "(&(objectclass=cell)(cid=*))";
    // will store their cID's
    String[] attrIDs = {"cid"};
    SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(attrIDs);
    NamingEnumeration answer = ctx.search(scope, f1, ctls);

  //    Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
  //    matchAttrs.put(new BasicAttribute("objectclass", "cell"));
  //    NamingEnumeration answer = rootCtx.search("dc=wonderland", matchAttrs);
    // all found we have found are in this context... we will pair this context
    // with the cID's we save
    DirContext cellCtx = (DirContext)ctx.lookup(scope);
    while (answer.hasMore()) {
      SearchResult sr = (SearchResult)answer.next();
//      logger.log(Level.INFO, (">>> found new cell" + sr.getName());
      for (NamingEnumeration e = sr.getAttributes().getAll(); e.hasMore();){
        Attribute attr = (Attribute)e.next();
  //        logger.log(Level.INFO, (attr.get());
        Integer i = Integer.parseInt((String)attr.get());
        cids.add(new pairCidAndCtx(cellCtx,i));
      }
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
  private HashMap<Integer, Set<Integer>> searchHelper(LinkedList<pairCidAndCtx> cids, ArrayList<String> filters, DirContext ctx, HashMap<Integer, Set<Integer>> results) throws NamingException {
    pairCidAndCtx pair = cids.poll();
    if(pair == null){
      // no other cells to search
      return results;
    }
    logger.log(Level.INFO, "looking at cid:" + pair.cid);
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
    logger.log(Level.INFO, "got cells, prepare to search for " + filters.size() + " filters");
    for(String filter:filters){
      logger.log(Level.INFO, "checking filter " + filter);
      boolean hitFilter = false;
      NamingEnumeration hits = pair.ctx.search(cellScope, filter, ctls);
      while(hits.hasMore()){
        // cell has at least one hit for this filter
        hitFilter = true;
        SearchResult sr = (SearchResult)hits.next();
        logger.log(Level.INFO, "hit >>>" + sr.getName());
        // log the mid for all hits
        for (NamingEnumeration e = sr.getAttributes().getAll(); e.hasMore();){
          Attribute attr = (Attribute)e.next();
          Integer mid = Integer.parseInt((String)attr.get());
          matches.add(mid);
        }
      }
      if(!hitFilter){
        cellHitAllFilters = false;
        logger.log(Level.INFO, "failed to match " + filter);
        break;
      }
    }

    if(cellHitAllFilters){
      results.put(pair.cid, matches);
    }


    return  searchHelper(cids, filters, ctx, results);
  }


  /**
   * Search all or a subset of cells based on a list of filters, describing
   * metadata attached to the cells.
   * @param filters the list of filters to match
   * @param rootCtx context in which to begin search. To search all cells, pass the
   *            wl parent context. To search a particular cell's children, pass
   *            in that cell's context. Doese NOT search the cell pointed to
   *            by context, even if context points at a cell.
   * @return
   * @throws javax.naming.NamingException
   */
  HashMap<Integer, Set<Integer> > searchMetadata(ArrayList<String> filters, DirContext rootCtx) throws NamingException {
    DirContext wlCtx = (DirContext) rootCtx.lookup("dc=wonderland");
    // start from root
    // get all cells

    LinkedList<pairCidAndCtx> cids = new LinkedList<pairCidAndCtx>();
    getAllCells(rootCtx, "dc=wonderland", cids);

    logger.log(Level.INFO, "searchMetadata for " + filters.size() + " filters");
    return searchHelper(cids, filters, wlCtx, new HashMap<Integer, Set<Integer> >());
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
        logger.log(Level.INFO, "erase any old jbdm files");
        File jbdmFolder = new File("/Users/Matt/sun/metadata/JNDIdemo/server-work");
        deleteDir(jbdmFolder);
        // Initialize the LDAP service
        service = new DefaultDirectoryService();

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled( false );
        service.setDenormalizeOpAttrsEnabled( true );

        // TODO make this name the name of the WL server
        Partition worldPartition = addPartition( "world", "dc=wonderland");

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
            logger.log(Level.INFO, "had to make world root");
            LdapDN rootDN = new LdapDN( "dc=wonderland" );
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
        topCtx = (DirContext) rootCtx.lookup("dc=wonderland");

        // register interior attributes
        registerAttribute("('cellID' 'cID')", "cellID from darkstar", Datatype.INTEGER);
        registerAttribute("('metadataID' 'mID')" , "ID assigned to metadata obj", Datatype.INTEGER);

        // register cell object class
        ArrayList<String> classes = new ArrayList<String>();
        classes.add("cell");
        classes.add("top");
        ArrayList<String> mustAttrs = new ArrayList<String>();
        mustAttrs.add("cID");
        ArrayList<String> mayAttrs = new ArrayList<String>();

        registerObjectClass("cell", "Represents a cell, will have metadata below it", classes, mustAttrs, mayAttrs);
        
    }


    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    public EmbeddedADS(ArrayList<Metadata> metadata) throws Exception
    {
        initLDAPServer();
        for(Metadata m:metadata){
          registerMetadataType(m);
        }


    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
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
}
