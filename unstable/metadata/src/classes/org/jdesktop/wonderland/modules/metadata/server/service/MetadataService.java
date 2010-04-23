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

import org.jdesktop.wonderland.modules.metadata.common.MetadataModificationListener;
import com.sun.sgs.app.ManagedObject;
import org.jdesktop.wonderland.modules.metadata.server.service.eads.EmbeddedADS;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;

import javax.naming.directory.InitialDirContext;

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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.directory.server.core.DirectoryService;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataConnectionType;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionModMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.auth.WonderlandServerIdentity;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import org.jdesktop.wonderland.modules.metadata.server.MetadataConnectionHandler;



/**
 * The MetadataService scans and registers Metadata types at startup. Since the
 * only method of creating a new Metadata type is creating a new module,
 * the server must be restarted and thus the types rescanned for every new type.
 *
 * Also keeps backend synchronized with world, and provides search access to
 * that backend.
 *
 * @author mabonner
 */
public class MetadataService extends AbstractService{
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

    /** the root context of the ldap server (dc=wonderland) */
    private InitialDirContext rootCtx;

    /** backend search DB (e.g. LDAP implementation) */
    private MetadataBackendInterface db;

    /** flag for whether or not backend needs to be reconstructed */
    private boolean rebuildDB = true;

    private ArrayList<MetadataModificationListener> modificationListeners
            = new ArrayList<MetadataModificationListener>();

    /** maps listeners to owning clients (in case of duplicate listeners) */
    private HashMap<MetadataModificationListener, ArrayList<WonderlandClientID>> clientModificationListeners
        = new HashMap<MetadataModificationListener, ArrayList<WonderlandClientID>>();


    /**
     * Alerts all matching listeners of change to metadata
     * @param cid cell id of modified cell
     * @param metadata the final state of the added/removed/modified metadata
     */
    private void metadataModification(CellID cid, Metadata metadata) {
      logger.log(Level.INFO, "metadata modification!");
      for(MetadataModificationListener l : modificationListeners){
        if(l.match(cid, metadata)){
          logger.log(Level.INFO, "server-side alert");
          l.fireAlert(cid, metadata);
        }
      }

      // collect all hit listeners for each client, we'll send just one message
      // to each client containing all of that client's hits
      HashMap<WonderlandClientID, ArrayList<MetadataModificationListener>> clientListenerHits
              = new HashMap<WonderlandClientID, ArrayList<MetadataModificationListener>>();

      for(Entry<MetadataModificationListener, ArrayList<WonderlandClientID>> e : clientModificationListeners.entrySet()){
        if(e.getKey().match(cid, metadata)){
          // for every client ID that is connected to this listener..
          for(WonderlandClientID wcid : e.getValue()){
            // create list of hits if necessary
            if(!clientListenerHits.containsKey(wcid)){
              clientListenerHits.put(wcid, new ArrayList<MetadataModificationListener>());
            }
            // add this listener hit
            ArrayList<MetadataModificationListener> clientHits = clientListenerHits.get(wcid);
            clientHits.add(e.getKey());
            clientListenerHits.put(wcid, clientHits);
          }
        }
      }

      CommsManager cm = WonderlandContext.getCommsManager();
      MetadataConnectionHandler handler = (MetadataConnectionHandler) cm.getClientHandler(MetadataConnectionType.CONN_TYPE);

      // send a message for each client with a hit
      for(Entry<WonderlandClientID, ArrayList<MetadataModificationListener>> e : clientListenerHits.entrySet()){
        handler.sendClientMessage(e.getKey(), new MetadataConnectionModMessage(e.getValue()));
      }
      
    }

    /**
     * Add a listener. Listener will be notified when metadata is added, removed
     * or modified.
     * @param l listener to add
     */
    public void addMetadataModificationListener(MetadataModificationListener l){
      modificationListeners.add(l);
    }

    /**
     * Remove a listener.
     * @param l listener to remove
     */
    public void removeMetadataModificationListener(MetadataModificationListener l){
      modificationListeners.remove(modificationListeners.lastIndexOf(l));
    }

    /**
     * client side listener (will result in message being sent via metadata
     * connection, rather than direct call to fire)
     * @param modListener
     */
    void addClientMetadataModificationListener(MetadataModificationListener modListener, WonderlandClientID wlCid) {
      // if this type of listner hasn't been seen, create its list of connected clients
      if(!clientModificationListeners.containsKey(modListener)){
        clientModificationListeners.put(modListener, new ArrayList<WonderlandClientID>());
      }

      logger.log(Level.INFO, "adding new mod listener for client id " + wlCid);
      // add the clientID
      clientModificationListeners.get(modListener).add(wlCid);
    }

    /**
     * client side listener (will result in message being sent via metadata
     * connection, rather than direct call to fire)
     * @param modListener
     */
    void removeClientMetadataModificationListener(MetadataModificationListener modListener, WonderlandClientID wlCid) {
      if(!clientModificationListeners.containsKey(modListener)){
        logger.log(Level.SEVERE, "trying to remove non-registered listener type for client id " + wlCid);
        return;
      }

      ArrayList<WonderlandClientID> attachedClients = clientModificationListeners.get(modListener);
      int idx = attachedClients.lastIndexOf(wlCid);
      if(idx < 0){
        // this listner type exists/is registered for some client, but not this one
        logger.log(Level.SEVERE, "trying to remove extant but unconnected listener type for client id " + wlCid);
        return;
      }

      // remove this client id
      attachedClients.remove(idx);
      clientModificationListeners.put(modListener, attachedClients);
    }
    

    public MetadataService(Properties props,
                           ComponentRegistry registry,
                           TransactionProxy proxy)
    {
      super(props, registry, proxy, logger);
      logger.log(Level.INFO, "creating metadata service");
      this.registry = registry;

      logger.log(Level.CONFIG, "Creating MetadataService properties: {0}",
                 props);
      PropertiesWrapper wrappedProps = new PropertiesWrapper(props);

      // create the transaction context factory
      ctxFactory = new TransactionContextFactoryImpl(proxy);
      try {
//        db = new EmbeddedADS();
//        scanAndRegisterTypes();
//        ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
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
      logger.log(Level.INFO, "metadata service completed, embedded db:" + db);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
      logger.log(Level.INFO, "Metadata service ready");
      CheckMetadataBackend check = new CheckMetadataBackend();
      try {
        transactionScheduler.runTask(check, taskOwner);
      } catch (Exception ex) {
        logger.logThrow(Level.SEVERE, ex, "Error checking metadata backend");
      }
      if(rebuildDB == false){
        try {
          logger.log(Level.INFO, "flag was found, do not need to rebuild");
          db = new EmbeddedADS(false);
          ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
        } catch (Exception ex) {
          Logger.getLogger(MetadataService.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      else{
        try {
          logger.log(Level.INFO, "flag was NOT found, need to rebuild");
          db = new EmbeddedADS(true);
          scanAndRegisterTypes();
          ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
          // set flag
          SetMetadataBackendFlag setFlag = new SetMetadataBackendFlag();
          try {
            transactionScheduler.runTask(setFlag, taskOwner);
          } catch (Exception ex) {
            logger.logThrow(Level.SEVERE, ex, "Error setting metadata backend flag");
          }
        } catch (Exception ex) {
          Logger.getLogger(MetadataService.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      logger.log(Level.INFO, "Metadata service doready complete");
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
   * Look for and register classes with the MetadataType annotation.
   *
   * If called more than once on the same db object, could result in re-registering
   * metadata types and throwing exceptions.
   *
   * @param cl the class loader to check for metadata types
   * @throws java.lang.Exception
   */
  private void scanAndRegisterTypes() throws Exception {
    ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
    // search annotations
    Iterator<Metadata> it = scl.getAll(MetadataType.class, Metadata.class); //CellFactorySPI.class);
    logger.log(Level.INFO, "[Metadata Service] about to search classloader");
    while (it.hasNext()) {
      Metadata metadata = it.next();
      logger.log(Level.INFO, "[Metadata Service] using system scl, scanned type:" + metadata.simpleName());
      registerMetadataType(metadata);
    }
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
    
    
    
    //
    // Metadata Actions
    //

    public void setCellMetadata(CellID id, Collection<Metadata> metadata){
      db.clearCellMetadata(id);
//      Iterator<LinkedHashMap<MetadataID, Metadata>> itr = metadata.iterator();
      for(Metadata m:metadata){
        db.addMetadata(id, m);
      }
    }

    /**
     * adds a new cell to the top level (e.g., has no parent besides the world)
     * @param cid id of cell to create
     */
    void addCell(CellID cid){
      if(db == null){
        logger.log(Level.SEVERE, "warning: backend not initialized in addCell!");
      }
      db.addCell(cid);
    }

    /**
     * adds a new cell beneath the passed in cell
     * @param cid id of cell to create
     * @param parent id of the parent cell to create under
     */
    void addCell(CellID cid, CellID parent){
      db.addCell(cid,parent);
    }


    /**
     * adds the passed metadata object to the cell with cellID cid.
     * logs errors if the cell does not exist or the
     * metadata type has not been registered. Calls
     * metadataModification to alert listeners of change.
     * @param cid id of cell to add metadata to
     * @param metadata metadata object to add
     */
    void addMetadata(CellID cid, Metadata metadata){
      db.addMetadata(cid, metadata);
      metadataModification(cid, metadata);
    }
    
    /**
     * changes metadata with metadataID mid to match passed metadata object.
     * logs errors if the cell does not exist or the
     * metadata type has not been registered. Calls
     * metadataModification to alert listeners of change.
     * @param mid id of metadata to be altered
     * @param cid id of cell to add metadata to
     * @param metadata new metadata object
     */
    void modifyMetadata(MetadataID mid, CellID cid, Metadata metadata){
      db.removeMetadata(mid);
      db.addMetadata(cid, metadata);
      metadataModification(cid, metadata);
    }


    /**
     * Remove cell and all metadata. This should be called when a cell is deleted.
     *
     * @param cid cellID of the cell to delete
     */
    public void eraseCell(CellID cid){
      db.eraseCell(cid);
    }

    /**
     * Delete the specified metadata object. Calls
     * metadataModification to alert listeners of change.
     * @param cid CellID of the cell from which metadata is being removed
     * @param mid MetadataID designating the metadata to remove
     * @param metadata the removed metadata
     */
    public void removeMetadata(CellID cid, MetadataID mid, Metadata metadata){
      db.removeMetadata(mid);
      metadataModification(cid, metadata);
    }

    /**
     * Remove all metadata from a cell
     *
     * @param cid id of cell to remove metadata from
     */
    public void clearCellMetadata(CellID cid){
      db.clearCellMetadata(cid);
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
    public void registerMetadataType(Metadata m) throws Exception{
      db.registerMetadataType(m);
    }


    /**
     * Search all cells in the world, finding cells with metadata satisfying the
     * passed in MetadataSearchFilters
     *
     * @param filters search criteria
     * @param cid id of parent cell to scope the search
     * @return map, mapping CellID's whose metadata that matched the
     * search, to a set of MetadataID's that matched the search for that cell.
     */
    public HashMap<CellID, Set<MetadataID> > searchMetadata(MetadataSearchFilters filters){
      // pass in a listener to notify, rather than sending directly to a connection
      logger.log(Level.INFO, "[META SERVICE] global search with " + filters.filterCount() + " filters");
      return db.searchMetadata(filters);
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
      return db.searchMetadata(filters, cid);
    }

    private class CheckMetadataBackend implements KernelRunnable {
        // check darkstar datastore for backend flag.. if flag is present,
        // assume backend is good. If flag missing, tell backend to rebuild.
        private String key = "WonderlandMetadataBackend";

        public String getBaseTaskType() {
            return NAME + ".CheckMetadataBackend";
        }

        public void run() throws Exception {
          // try to get key
          try{
            dataService.getServiceBinding(key);
            logger.log(Level.INFO, "[META SERVICE] got backend binding");
            // does not need to be rebuilt
            rebuildDB = false;
          }
          catch(Exception e){
            // binding was not present. rebuild DB, set binding.
            logger.log(Level.INFO, "[META SERVICE] did not get backend binding: " + e);

            rebuildDB = true;
          }
          logger.log(Level.INFO, "[META SERVICE]  completed backend check");
        }
    }

    private class SetMetadataBackendFlag implements KernelRunnable {
        // Set backend flag, indicating valid backend has been established
        private String key = "WonderlandMetadataBackend";

        public String getBaseTaskType() {
            return NAME + ".CheckMetadataBackend";
        }

        public void run() throws Exception {
            try{
            dataService.setServiceBinding(key, new MetadataBackendFlag());
            logger.log(Level.INFO, "[META SERVICE]  registered flag");
            }
            catch(Exception e2){
              logger.log(Level.INFO, "[META SERVICE] error setting flag:" + e2);
            }
        }
    }

     private static class MetadataBackendFlag implements Serializable, ManagedObject {
       // currently empty, no extra information needed other than flag's existence
     }

}
