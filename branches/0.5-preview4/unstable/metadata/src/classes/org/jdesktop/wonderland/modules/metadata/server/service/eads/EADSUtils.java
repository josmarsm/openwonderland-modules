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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.BaseMetadata;

/**
 * Constants and utility functions for EmbeddedADS
 *
 * Most utility functions involve converting Java objects and strings to an
 * LDAP friendly and/or unique form.
 *
 * @author mabonner
 */
public class EADSUtils {

  private static Logger logger = Logger.getLogger(EADSUtils.class.getName());

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
   * LDAP syntax constant
   */
  final static String integerSyntax = "EQUALITY integerMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.27 SINGLE-VALUE";
  /**
   * LDAP syntax constant
   */
  final static String stringSyntax = "EQUALITY caseIgnoreMatch SUBSTR caseIgnoreSubstringsMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 SINGLE-VALUE";
  /**
   * LDAP syntax constant
   */
  final static String dateSyntax = "EQUALITY generalizedTimeMatch ORDERING generalizedTimeOrderingMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 SINGLE-VALUE";

  /**
   * LDAP formatting for dates
   */
  final static DateFormat ldapDateFormat = new SimpleDateFormat("yyyyMMddhhmmssZ");

  /**
   * used to register the cellID attribute. The full name of this attribute is
   * stored in CELL_ID_ATTR
   */
  final static String CELL_ID_STR = "cellID";

  /**
   * used to register the metadataID attribute. The full name of this attribute is
   * stored in METADATA_ID_ATTR
   */
  final static String METADATA_ID_STR = "metadataID";


  // default Object Classes and Attributes

  /**
   * auxiliary object class name - added to each metadata object in addMetadata
   */
  final static String METADATA_AUX_OC = getUniqueName("metadata", EmbeddedADS.class);

  /**
   * default object class name - cells will have metadata 'underneath' them in ldap
   */
  static String CELL_OC = getUniqueName("cell", EmbeddedADS.class);
 
  /**
   * cellID attribute
   */
  final static String CELL_ID_ATTR = getUniqueName(CELL_ID_STR, EmbeddedADS.class);

  /**
   * metadataID attribute
   */
  final static String METADATA_ID_ATTR = getUniqueName(METADATA_ID_STR, EmbeddedADS.class);

  /**
   * convert a full classname like org.site.module.class to an ldap friendly
   * version by replacing all "." with "-", e.g. returns "org-site-module-class"
   * @param str the name to convert
   * @return
   */
  static String classToLDAPName(Class c) {
    return c.getName().replaceAll("\\.", "-");
  }

  /**
   * Creates a server-unique, ldap-friendly name for an object class or attribute
   * type. Does this by converting passed in class name to an ldap friendly form
   * and appending it with passed string.
   *
   * Passed string MUST be LDAP friendly. Called with the results of attrNameToLDAP,
   * (something like "date-created"), the results will be something like
   * "my-class-package-name-date-created"
   *
   * @param name must already be LDAP friendly
   * @param c class of object containing (attr type) or representing (cell type) this name
   * @return
   */
  static String getUniqueName(String name, Class c){
    return classToLDAPName(c) + "-" +  name;
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
  static void printContents(DirContext ctx, String scope) {
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
      logger.severe("[EADS Utils] error printing contents, ex:" + ex);
    }
  }

  /**
   * debugging method, prints out all LDAP contents int topCtx under ctx
   * @param topCtx top/root context in which names (like those bound in ctx) may be looked up
   * @param ctx context to begin listing bound names in
   * @param level used to put spaces in front of print statements based on recursion level
   * @throws javax.naming.NamingException
   */
  static void printContentsVerbose(DirContext topCtx, DirContext ctx, int level) {
    try {
      NamingEnumeration list = ctx.listBindings("");
      String spacer = "";
      for (int i = 0; i < level; i++) {
        spacer += " ";
      }
      while (list.hasMore()) {
        NameClassPair nc = (NameClassPair) list.next();
        logger.info(spacer + nc);
        logger.info(spacer + "name is :" + nc.getName());
        try {
          DirContext subCtx = (DirContext) topCtx.lookup(nc.getName());
          printContentsVerbose(topCtx, subCtx, level + 4);
        } catch (LdapNameNotFoundException lnnfe) {
          logger.info("end of line");
        }
      }
      Attributes attrs = ctx.getAttributes("");
    } catch (NamingException ex) {
      logger.severe("[EADS Utils] error printing verbose contents, ex:" + ex);
    }
  }

  /** Deletes all files and subdirectories under dir.
   * Returns true if all deletions were successful.
   * If a deletion fails, the method stops attempting to delete and returns false.
   */
  static boolean deleteDir(File dir) {
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
   * Convert an attribute name like "date created" to an ldap friendly
   * version by replacing all " " with "-", e.g. returns "date-created"
   *
   * Note that this does NOT create a full, server-unique name, call getUniqueName
   * with the results of this function and the attributes parent class for that.
   *
   * @param str the attribute to convert
   * @return
   */
  static String attrnameToLDAP(String str) {
    return str.replaceAll(" ", "-");
  }

  /**
   * returns false if mv's value is null or "", or if mv itself == null
   * @param mv the value to check
   * @return
   */
  static boolean nullAttrCheck(MetadataValue mv) {
//    logger.fine("[EADS Utils] null attr check");
    if(mv == null){
      logger.severe("[EADS Utils] null attr check.. mv itself is null!");
      return true;
    }
    if(mv.getVal() == null || mv.getVal().equals("")){
      return true;
    }
    return false;
  }

  /**
   * Convert a MetadataSearchFilters object into an LDAP search filters string
   *
   * @return the list of filters, in RFC 2254 format
   */
  static ArrayList<String> convertFiltersToLDAP(MetadataSearchFilters filters){
    ArrayList<String> result = new ArrayList<String>();
    // example filter
    // (&(objectclass=org-jdesktop-wonderland-modules-metadata-common-Metadata)(someAttr=hello hello));
    for(Metadata m:filters.getFilters()){
      String newFilter = "(&(objectclass=";
      // add the object class
      String nameLdap = EADSUtils.classToLDAPName(m.getClass());
      newFilter += nameLdap + ")";
      logger.fine("[EADS Utils] oc filter in LDAP is: " + nameLdap);
      // add attributes
      newFilter += attributesToFilterString(m.getAttributes(), m.getClass());
      newFilter += ")";
      logger.fine("[EADS Utils] final filter: " + newFilter);
      result.add(newFilter);
    }
    return result;
  }

  /**
   * covert a set of attr names/values to RFC 2254 format. Also convert names to
   * their unique, LDAP friendly forms (class name + attr name)
   * @param attrs set of attribute names (raw, not yet passed through attrnameToLDAP) mapped to their MetadataValues
   * @param metadataClass class of metadata type holding these attributes.
   * @return
   */
  static String attributesToFilterString(Set<Map.Entry<String, MetadataValue>> attrs, Class metadataClass){
    String res = "";
    for(Entry<String, MetadataValue> e: attrs){
      String attrName = EADSUtils.attrnameToLDAP(e.getKey());
      logger.info("[EADS Utils] making attr from Key, Val: " + e.getKey() + ", " + e.getValue());
      MetadataValue mv = e.getValue();
      String val = mv.getVal();
      switch (mv.type){
        case DATE:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            logger.fine("[EADS Utils] wildcard date");
            val = "*";
            break;
          }
          // convert to ldap date
          try {
            Date d = BaseMetadata.dateFormat.parse(val);
            val = EADSUtils.ldapDateFormat.format(d);
          } catch (ParseException ex) {
            logger.severe("[EADS Utils] invalid date syntax creating filter!");
          }
          break;
        case INTEGER:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            logger.fine("[EADS Utils] wildcard int");
            val = "*";
          }
          break;
        case STRING:
          // can't be a null value
          if(EADSUtils.nullAttrCheck(mv)){
            logger.fine("[EADS Utils] wildcard string");
            val = "*";
          }
      }
      res += "(" + EADSUtils.getUniqueName(attrName, metadataClass) + "=" + val + ")";
    }
    logger.fine("[EADS Utils] resulting attrs filter:" + res);
    return res;
  }

  /**
   * Takes in array of object classes or attribute types, builds an RFC 4512
   * compliant grouping of them, e.g. "( 'val' $ 'val2' $ 'val3' )"
   * @param classes
   * @return
   */
  static String buildObjList(ArrayList<String> ids) {
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


//  static void printResults(HashMap<Integer, Set<Integer>> results) {
//    for(Entry<Integer, Set<Integer> > e : results.entrySet()){
//            logger.info("Key, Val: " + e.getKey() + ", " + e.getValue());
//    }
//  }
}
