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
package org.jdesktop.wonderland.modules.ldaplogin.weblib;

import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.AuthSessionManagerImpl.TokenGenerator;
import org.jdesktop.wonderland.modules.securitysession.auth.weblib.UserPlugin;
import org.jdesktop.wonderland.modules.securitysession.weblib.UserRecord;

/**
 * A UserPlugin that uses LDAP to get a person's information.  This
 * plugin relies on the following properties being set in the
 * UserPlugin configuration file:
 * <ul>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.directory</code>
 *     The LDAP URL to connect to, for example ldaps://directory.java.net
 * </li>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.base-dn</code>
 *     The base DN to perform lookups under.  For example dc=org,dc=jdesktop
 * </li>
 * </ul>
 *
 * Additionally, the following optional parameters may be specified:
 * <ul>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.search-filter</code>
 *     The LDAP filter to search for user names, for example "employeenumber=%s",
 *     where %s will be subsitituted with the user name used for authentication.
 *     The default is "uid=%s"
 * </li>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.context-factory</code>
 *     The directory context factory.  Default is "com.sun.jndi.ldap.LdapCtxFactory"
 * </li>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.fullname-attr</code>
 *     The attribute in the directory that represents the full name.  Default is "cn"
 * </li>
 * <li><code>org.jdesktop.wonderland.modules.ldaplogin.email-attr</code>
 *     The attribute in the directory that represents the email address.  Default is "mail"
 * </li>
 * </ul>
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class LDAPUserPlugin implements UserPlugin {
    // logger
    private static final Logger logger =
            Logger.getLogger(LDAPUserPlugin.class.getName());

    // properties to use
    private static final String PROP_BASE =
            "org.jdesktop.wonderland.modules.ldaplogin.";
    private static final String PROP_LDAP_URL = PROP_BASE + "directory";
    private static final String PROP_BASE_DN = PROP_BASE + "base-dn";
    private static final String PROP_SEARCH_FILTER = PROP_BASE + "search-filter";
    private static final String PROP_CONTEXT_FACTORY = PROP_BASE + "context-factory";
    private static final String PROP_FULLNAME_ATTR = PROP_BASE + "fullname-attr";
    private static final String PROP_EMAIL_ATTR = PROP_BASE + "email-attr";

    // default values
    private static final String SEARCH_FILTER_DEFAULT = "uid=%s";
    private static final String CONTEXT_FACTORY_DEFAULT = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String FULLNAME_ATTR_DEFAULT = "cn";
    private static final String EMAIL_ATTR_DEFAULT = "mail";

    // properties not in environment
    private String searchFilter;
    private String baseDN;
    private String fullnameAttr;
    private String emailAttr;

    // directory context
    private DirContext dnContext;

    // ldap environment
    private Hashtable ldapEnv;

    public void configure(Properties props) {
        // required properties
        String ldapURL = props.getProperty(PROP_LDAP_URL);
        if (ldapURL == null) {
            throw new RuntimeException(PROP_LDAP_URL + " required");
        }
        baseDN = props.getProperty(PROP_BASE_DN);
        if (baseDN == null) {
            throw new RuntimeException(PROP_BASE_DN + " required.");
        }

        // optional properties
        searchFilter = props.getProperty(PROP_SEARCH_FILTER,
                                        SEARCH_FILTER_DEFAULT);
        fullnameAttr = props.getProperty(PROP_FULLNAME_ATTR,
                                        FULLNAME_ATTR_DEFAULT);
        emailAttr = props.getProperty(PROP_EMAIL_ATTR,
                                     EMAIL_ATTR_DEFAULT);

        String contextFactory = props.getProperty(PROP_CONTEXT_FACTORY,
                                        CONTEXT_FACTORY_DEFAULT);

        ldapEnv = new Hashtable();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        ldapEnv.put(Context.PROVIDER_URL, ldapURL);

        logger.info("Loading LDAP authenticator");
    }

    public PasswordResult credentialsMatch(String username, Object... credentials) {
        String dn = null;
        DirContext userContext;

        // map name to an identity
        try {
            try {
                userContext = findUser(username, false);
            } catch (CommunicationException ce) {
                // if there was a communications exception on the first try,
                // reset the LDAP context and try again
                userContext = findUser(username, true);
            }

            // read the distinguished name of this user
            if (userContext != null) {
                dn = userContext.getNameInNamespace();
            }
        } catch (NamingException ne) {
            logger.log(Level.WARNING, "Directory lookup error.", ne);
            throw new RuntimeException(ne);
        }

        // user not found
        if (dn == null) {
            return PasswordResult.UNKNOWN_USER;
        }

        // at this point, we have a record of the user, so we should return
        // a valid result.  First check if the request is valid
        if (credentials.length != 1 || !(credentials[0] instanceof char[])) {
            // invalid request
            return PasswordResult.NO_MATCH;
        }

        // now try to bind to that dn
        Hashtable secureEnv = new Hashtable(ldapEnv);
        secureEnv.put(Context.SECURITY_PRINCIPAL, dn);
        secureEnv.put(Context.SECURITY_CREDENTIALS, (char[]) credentials[0]);

        // try connecting.  If we succeed, the password was correct.
        try {
            DirContext secureContext = new InitialDirContext(secureEnv);
            secureContext.close();

            // if we didn't throw a naming exception, the password is valid
            return PasswordResult.MATCH;
        } catch (NamingException ne) {
            // an exception at this point means the login failed
            return PasswordResult.NO_MATCH;
        }
    }

    /**
     * Get a UserRecord from the user's LDAP entry.
     * @param userContext the user's directory information
     * @return a UserRecord describing the user
     */
    public UserRecord getUserRecord(String username, TokenGenerator generator) {
        DirContext userContext;

        try {
            try {
                userContext = findUser(username, false);
            } catch (CommunicationException ce) {
                // if there was a communications exception on the first try,
                // reset the LDAP context and try again
                userContext = findUser(username, true);
            }


            String fullname = getAttribute(fullnameAttr, userContext);
            String email = getAttribute(emailAttr, userContext);

            // create the userrecord
            UserRecord rec = new UserRecord(username,
                                            generator.generateToken(username));

            // set values in the record
            rec.getAttributes().put(new BasicAttribute("uid", username));
            rec.getAttributes().put(new BasicAttribute("cn", fullname));
            rec.getAttributes().put(new BasicAttribute("mail", email));
            
            return rec;
        } catch (NamingException ne) {
            logger.log(Level.WARNING, "Error communicating with LDAP", ne);
            return null;
        }
    }

    /**
     * Map a username to a user's directory entry.
     * @param username the name to lookup
     * @param newcontext if true, force a new InitialDirContext for the
     * LDAP lookup.  Otherwise, if there is an existing context it will
     * be reused.
     * @return the DirectoryContext corresponding to the given user name
     * @throws NamingException if there is an error finding the given
     * username
     */
    protected DirContext findUser(String username, boolean newcontext)
        throws NamingException
    {
        DirContext ret = null;

        // see if we need to instantiate the context
        if (dnContext == null || newcontext) {
            dnContext = new InitialDirContext(ldapEnv);
        }

        // setup the search controls
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(new String[] { "cn" });
        sc.setReturningObjFlag(true);
        sc.setCountLimit(1);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // do the search
        String filter = searchFilter.replaceAll("%s", username);
        NamingEnumeration ne = dnContext.search(baseDN, filter, sc);
        if (ne.hasMore()) {
            SearchResult res = (SearchResult) ne.next();
            ret = (DirContext) res.getObject();
        }

        // return the name
        return ret;
    }

    /**
     * Get an attribute's first value
     * @param attrName the attribute to get
     * @param context the context to get the attribute from
     * @return the attribute's first value, or null if the attribute
     * doesn't exist
     * @throws NamingException if there is an error
     */
    protected String getAttribute(String attrName, DirContext context)
        throws NamingException
    {
        Attributes attrs = context.getAttributes("", new String[] { attrName });
        Attribute attr = attrs.get(attrName);
        if (attr != null) {
            return (String) attr.get();
        } else {
            return null;
        }
    }

}


