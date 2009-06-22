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
package org.jdesktop.wonderland.modules.metadata.server.service;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;



import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;



import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.AbstractService.Version;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.jdesktop.wonderland.server.auth.WonderlandServerIdentity;



/**
 *
 * @author mabonner
 */
public class MetadataService extends AbstractService {
    /** The name of this class. */
    private static final String NAME = MetadataService.class.getName();

    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.modules.metadata.server.service";

    /** The logger for this class. */
	private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(PKG_NAME));

    /** The name of the version key. */
    private static final String VERSION_KEY = PKG_NAME + ".service.version";

    /** The major version. */
    private static final int MAJOR_VERSION = 0;

    /** The minor version. */
    private static final int MINOR_VERSION = 5;

    /** the component registry */
    private ComponentRegistry registry;

    /** manages the context of the current transaction */
    private TransactionContextFactory<MetadataContext> ctxFactory;

    /** executor where scheduled tasks are processed */
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /** The directory service */
    private DirectoryService dirService;
    

    public MetadataService(Properties props,
                           ComponentRegistry registry,
                           TransactionProxy proxy)
    {
        super(props, registry, proxy, logger);

        this.registry = registry;

        logger.log(Level.CONFIG, "Creating MetadataService properties: {0}",
                   props);
        PropertiesWrapper wrappedProps = new PropertiesWrapper(props);

        // create the transaction context factory
        ctxFactory = new TransactionContextFactoryImpl(proxy);
        try {
          init();
            /*
	         * Check service version.
 	         */
            transactionScheduler.runTask(new KernelRunnable() {
                public String getBaseTaskType() {
                    return NAME + ".VersionCheckRunner";
                }

                public void run() {
                    checkServiceVersion(
                            VERSION_KEY, MAJOR_VERSION, MINOR_VERSION);
                }
            }, taskOwner);
        } catch (Exception ex) {
            logger.logThrow(Level.SEVERE, ex, "Error reloading cells");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
        logger.log(Level.CONFIG, "Metadata service ready");
    }

    @Override
    protected void doShutdown() {
        executor.shutdown();
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
                                                Version currentVersion) {
        throw new IllegalStateException(
 	            "unable to convert version:" + oldVersion +
	            " to current version:" + currentVersion);
    }

    /**
     * Transaction state
     */
    private class MetadataContext extends TransactionContext {
        private List<ScheduledRequest> requests = 
                new LinkedList<ScheduledRequest>();

        public MetadataContext(Transaction txn) {
            super (txn);
        }

        public void add(ScheduledRequest request) {
            requests.add(request);
        }

        @Override
        public void abort(boolean retryable) {
            requests.clear();
        }

        @Override
        public void commit() {
            isCommitted = true;

            for (ScheduledRequest request : requests) {
                executor.submit(request);
            }

            requests.clear();
        }
    }

    /** Private implementation of {@code TransactionContextFactory}. */
    private class TransactionContextFactoryImpl
            extends TransactionContextFactory<MetadataContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, NAME);
        }

        /** {@inheritDoc} */
        protected MetadataContext createContext(Transaction txn) {
            return new MetadataContext(txn);
        }
    }

    class ScheduledRequest implements Runnable {
        private WonderlandServerIdentity identity;
        private BigInteger taskId;

        public ScheduledRequest(WonderlandServerIdentity identity,
                                BigInteger taskId)
        {
            this.identity = identity;
            this.taskId = taskId;
        }

        public void run() {
            try {
                // response now has the proper set of permissions, so create
                // a new transaction to call back into the secure task
                logger.log(Level.INFO, "run task");
                // transactionScheduler.runTask(new SecureTaskKernelRunner(taskId, grant), identity);
            } catch (Exception ex) {
                logger.logThrow(Level.WARNING, ex, "Unable to run secure task");
            }
        }
    }
    
    
    /**
     *
     *  APACHEDS CODE
     *
     */

    
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
        dirService.addPartition( partition );

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
    private void init() throws Exception
    {
        logger.log(Level.INFO, "[METADATA EADS] erase any old jbdm files");
        File jbdmFolder = new File("/Users/Matt/sun/metadata/JNDIdemo/server-work");
        deleteDir(jbdmFolder);
        // Initialize the LDAP service
        dirService = new DefaultDirectoryService();

        // Disable the ChangeLog system
        dirService.getChangeLog().setEnabled( false );
        dirService.setDenormalizeOpAttrsEnabled( true );

        // Create some new partitions named 'foo', 'bar' and 'apache'.
        Partition fooPartition = addPartition( "foo", "dc=foo,dc=com" );
        Partition barPartition = addPartition( "bar", "dc=bar,dc=com" );
        Partition apachePartition = addPartition( "apache", "dc=apache,dc=org");

        // Index some attributes on the apache partition
        addIndex( apachePartition, "objectClass", "ou", "uid" );

        // And start the service
        dirService.startup();


        // Inject the foo root entry if it does not already exist
        try
        {
            dirService.getAdminSession().lookup( fooPartition.getSuffixDn() );
        }
        catch ( LdapNameNotFoundException lnnfe )
        {
            logger.log(Level.INFO, "[METADATA EADS] had to make foo root");
            LdapDN dnFoo = new LdapDN( "dc=foo,dc=com" );
            ServerEntry entryFoo = dirService.newEntry( dnFoo );
            entryFoo.add( "objectClass", "top", "domain", "extensibleObject" );
            entryFoo.add( "dc", "foo" );
            dirService.getAdminSession().add( entryFoo );
        }

        // Inject the bar root entry
        try
        {
            dirService.getAdminSession().lookup( barPartition.getSuffixDn() );
        }
        catch ( LdapNameNotFoundException lnnfe )
        {
            LdapDN dnBar = new LdapDN( "dc=bar,dc=com" );
            ServerEntry entryBar = dirService.newEntry( dnBar );
            entryBar.add( "objectClass", "top", "domain", "extensibleObject" );
            entryBar.add( "dc", "bar" );
            dirService.getAdminSession().add( entryBar );
        }

        // Inject the apache root entry
        try
        {
            dirService.getAdminSession().lookup( apachePartition.getSuffixDn() );
        }
        catch ( LdapNameNotFoundException lnnfe )
        {
            LdapDN dnApache = new LdapDN( "dc=Apache,dc=Org" );
            ServerEntry entryApache = dirService.newEntry( dnApache );
            entryApache.add( "objectClass", "top", "domain", "extensibleObject" );
            entryApache.add( "dc", "Apache" );
            dirService.getAdminSession().add( entryApache );
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

    /**
     * We just do a lookup on the server to check that it's available.
     */
    public void test() //throws Exception
    {

        try
        {
            // Read an entry
            Entry result = this.dirService.getAdminSession().lookup( new LdapDN( "dc=apache,dc=org" ) );

            // And print it if available
            logger.log(Level.INFO, "[METADATA EADS] Found entry : " + result );

            Hashtable<Object, Object> env = new Hashtable<Object, Object>();
            env.put(DirectoryService.JNDI_KEY, this.dirService);
            env.put(Context.PROVIDER_URL, "");
            env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());
            env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
            env.put(Context.SECURITY_CREDENTIALS, "secret");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");


            DirContext ctx = new InitialDirContext(env);
            logger.log(Level.INFO, "[METADATA EADS] root context");
            logger.log(Level.INFO, "[METADATA EADS] root dn: " + ctx.getNameInNamespace());
            Attributes rootAttrs = ctx.getAttributes("");
            logger.log(Level.INFO, "[METADATA EADS] root attributes");
            for (NamingEnumeration re = rootAttrs.getAll(); re.hasMore();) {
                Attribute attr = (Attribute)re.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration e = attr.getAll(); e.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + e.next());
                }
            }




            logger.log(Level.INFO, "[METADATA EADS] *******************");
            logger.log(Level.INFO, "[METADATA EADS] apache context");
            DirContext apacheCtx = (DirContext) ctx.lookup("dc=apache,dc=org");
            logger.log(Level.INFO, "[METADATA EADS] apche dn: " + apacheCtx.getNameInNamespace());
            logger.log(Level.INFO, "[METADATA EADS] apache bound names");
            NamingEnumeration list2 = apacheCtx.list("");
            while (list2.hasMore()) {
                NameClassPair nc = (NameClassPair)list2.next();
                System.out.println(nc);
            }
            Attributes apacheAttrs = ctx.getAttributes("");

            logger.log(Level.INFO, "[METADATA EADS] apache attributes");
            for (NamingEnumeration ae = apacheAttrs.getAll(); ae.hasMore();) {
                Attribute attr = (Attribute)ae.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration e = attr.getAll(); e.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + e.next());
                }
            }




            // Create attributes to be associated with the new context
            Attributes attrs = new BasicAttributes(true); // case-ignore
            Attribute objclass = new BasicAttribute("objectclass");
            objclass.add("extensibleObject");
            objclass.add("domain");
            objclass.add("top");
//            objclass.add("organizationalUnit");
            attrs.put(objclass);

            // Create the context
            DirContext newCtx = apacheCtx.createSubcontext("dc=New", attrs);



            logger.log(Level.INFO, "[METADATA EADS] *******************");
            logger.log(Level.INFO, "[METADATA EADS] new context was created");
            logger.log(Level.INFO, "[METADATA EADS] apache bound names");
            list2 = apacheCtx.list("");
            while (list2.hasMore()) {
                NameClassPair nc = (NameClassPair)list2.next();
                System.out.println(nc);
            }
            apacheAttrs = ctx.getAttributes("");

            logger.log(Level.INFO, "[METADATA EADS] apache attributes");
            for (NamingEnumeration ae = apacheAttrs.getAll(); ae.hasMore();) {
                Attribute attr = (Attribute)ae.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration e = attr.getAll(); e.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + e.next());
                }
            }






            logger.log(Level.INFO, "[METADATA EADS] *******************");
            logger.log(Level.INFO, "[METADATA EADS] new context");
            logger.log(Level.INFO, "[METADATA EADS] new context dn: " + newCtx.getNameInNamespace());
            logger.log(Level.INFO, "[METADATA EADS] newctx bound names");
            NamingEnumeration newList = newCtx.list("");
            while (newList.hasMore()) {
                NameClassPair nc = (NameClassPair)newList.next();
                System.out.println(nc);
            }
            Attributes newAttrs = newCtx.getAttributes("");

            logger.log(Level.INFO, "[METADATA EADS] newctx attributes");
            for (NamingEnumeration e = newAttrs.getAll(); e.hasMore();) {
                Attribute attr = (Attribute)e.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration ne = attr.getAll(); ne.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + ne.next());
                }
            }

            logger.log(Level.INFO, "[METADATA EADS] *******************");


            Attributes atAttrs = new BasicAttributes(true);
            atAttrs.put("attributeTypes", "( 1.3.6.1.4.1.18060.0.4.3.2.1 NAME 'numberOfGuns' DESC 'Number of guns of a ship' EQUALITY integerMatch SYNTAX 1.3.6.1.4.1.1466.115.121.1.27 SINGLE-VALUE )");
            ctx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, atAttrs);

            Attributes ocAttrs = new BasicAttributes(true);
            ocAttrs.put("objectClasses", "( 1.3.6.1.4.1.18060.0.4.3.3.1 NAME 'ship' DESC 'An entry which represents a ship' SUP top STRUCTURAL MUST cn MAY ( numberOfGuns $ description ) )");
            ctx.modifyAttributes("cn=schema", DirContext.ADD_ATTRIBUTE, ocAttrs);


             // Create attributes to be associated with the new context
            attrs = new BasicAttributes(true); // case-ignore
            objclass = new BasicAttribute("objectclass");
//            objclass.add("extensibleObject");
            objclass.add("ship");
            objclass.add("top");
            attrs.put(objclass);
            BasicAttribute guns = new BasicAttribute("numberOfGuns");
            guns.add("152");
            attrs.put(guns);

            // Create the context
            DirContext shipCtx = apacheCtx.createSubcontext("cn=HMS Victory", attrs);



            logger.log(Level.INFO, "[METADATA EADS] *******************");
            logger.log(Level.INFO, "[METADATA EADS] ship context was created");
            logger.log(Level.INFO, "[METADATA EADS] apache bound names");
            list2 = apacheCtx.list("");
            while (list2.hasMore()) {
                NameClassPair nc = (NameClassPair)list2.next();
                System.out.println(nc);
            }
            apacheAttrs = ctx.getAttributes("");

            logger.log(Level.INFO, "[METADATA EADS] apache attributes");
            for (NamingEnumeration ae = apacheAttrs.getAll(); ae.hasMore();) {
                Attribute attr = (Attribute)ae.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration e = attr.getAll(); e.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + e.next());
                }
            }

            logger.log(Level.INFO, "[METADATA EADS] *******************");
            logger.log(Level.INFO, "[METADATA EADS] ship context");
            logger.log(Level.INFO, "[METADATA EADS] new context dn: " + shipCtx.getNameInNamespace());
            logger.log(Level.INFO, "[METADATA EADS] shipCtx bound names");
            newList = shipCtx.list("");
            while (newList.hasMore()) {
                NameClassPair nc = (NameClassPair)newList.next();
                System.out.println(nc);
            }
            newAttrs = shipCtx.getAttributes("");

            logger.log(Level.INFO, "[METADATA EADS] shipCtx attributes");
            for (NamingEnumeration e = newAttrs.getAll(); e.hasMore();) {
                Attribute attr = (Attribute)e.next();
                logger.log(Level.INFO, "[METADATA EADS] attribute: " + attr.getID());
                /* Print each value */
                for (NamingEnumeration ne = attr.getAll(); ne.hasMore();){
                  logger.log(Level.INFO, "[METADATA EADS] value: " + ne.next());
                }
            }
        }
        catch ( Exception e )
        {
            // Ok, we have something wrong going on ...
            // e.printStackTrace();
        }
    }

}
