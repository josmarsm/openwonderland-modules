/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.worldbuilder.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.servlet.WonderlandServletUtil;
import org.jdesktop.lg3d.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.lg3d.wonderland.wfs.InvalidWFSException;
import org.jdesktop.lg3d.wonderland.wfs.WFS;
import org.jdesktop.lg3d.wonderland.wfs.WFSCell;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.lg3d.wonderland.wfs.WFSFactory;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.CellDimension;

/**
 *
 * @author jkaplan
 */
public class CellPersistence {
    private static final Logger logger =
            Logger.getLogger(CellPersistence.class.getName());
    
    /** property specifying the root of the WFS */
    public static final String WFS_ROOT_PROP = "wonderland.wfs.root";
    
    /** property specifying a WFS subdirectory to open */
    public static final String WFS_DIR_PROP = "wonderland.wfs.worldbuilder.dir";
    
    /** the default WFS root */
    private static final String WFS_ROOT_DEFAULT = File.separator + ".wonderland" + 
                                                   File.separator + "worldbuilder-wfs";
    
    /** the default WFS directory */
    private static final String WFS_DIR_DEFAULT = "root";
    
    /** singleton */
    private static CellPersistence persistence;
   
    /** set by the context PersistenceContextListener */
    private static ServletContext servletContext;
    
    /** the ID of the root cell */
    private String rootCellId;
    
    /** cells we known about, mapped by id */
    private Map<String, Cell> cells;
     
    /** map from cellID to WFS cell */
    private Map<String, WFSCell> wfsCells;
    
    /** map from path to cell info */
    private Map<String, CellInfo> cellInfo;
    
    /** single thread to handle reading and writing */
    private ScheduledExecutorService processor;
    
    /** the task that periodically updates the directory */
    private ScheduledFuture directoryUpdate;
    
    /** a lock on the data */
    private ReadWriteLock lock;
    
    /** convert to and from WFS */
    private WFSConverter converter;
    
    /**
     * Get the singelton instance of CellPersistence.  Create the persistence
     * if it doesn't exist.  <code>setServletContext()</code> must be called
     * before your can get the CellPersistence object.
     */
    public synchronized static CellPersistence get() {
        return get(true);
    }
    
    public synchronized static CellPersistence get(boolean create) {
        if (persistence == null && create) {
            persistence = new CellPersistence();
        }
        
        return persistence;
    }
    
    public static void setServletContext(ServletContext servletContext) {
        CellPersistence.servletContext = servletContext;
    }
    
    /**
     * Singelton: Use getInstance() instead of constructor.
     */
    private CellPersistence() {
        // make sure someone set the servlet context
        if (servletContext == null) {
            throw new IllegalStateException("ServletContext not set");
        }
        
        cells = Collections.synchronizedMap(new HashMap());
    
        // these maps are only ever access from inside the executor, so
        // they do not need to be syncrhonized
        wfsCells = new HashMap();
        cellInfo = new HashMap();
        
        // create the WFS converter
        converter = new WFSConverter();
       
        // create the root cell
        WFSCell rootWFS;
        try {
            rootWFS = initWFS();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        
        // initialize the lock
        lock = new ReentrantReadWriteLock();
        
        // create the processor service
        processor = Executors.newSingleThreadScheduledExecutor();
        
        // create the directory update task
        Runnable dirTask = new ReadWFSTask(rootWFS);
        
        // run the directory update the first time and wait for it to
        // complete
        try {
            Future future = processor.submit(dirTask);
            future.get();
        } catch (Exception ex) {
            throw new RuntimeException("Error starting persitence for " +
                                 rootWFS.getParentCellDirectory().getPathName(), 
                                       ex);
        }
        
        // now schdule the directory check every second
        directoryUpdate = processor.scheduleAtFixedRate(dirTask, 1, 1, 
                                                        TimeUnit.SECONDS);
    }
    
    /**
     * Get the root cell
     * @return the root cell
     */
    public Cell getRoot() {
        return get(rootCellId);
    }
    
    /**
     * Get a cell by id
     * @param cellID the id of the cell to get
     * @return the cell, or null if no cell exists with the given id
     */
    public Cell get(String cellID) {
        return cells.get(cellID);
    }
    
    /**
     * Get all cells
     * @return all cells
     */
    public Set<Cell> getCells() {
        synchronized (cells) {
            return new HashSet(cells.values());
        }
    }
    
    /**
     * Update properties of a single cell
     * @param cell the cell to update
     */
    public void update(final Cell cell) 
            throws ExecutionException, InterruptedException
    {
        // submit the update job
        Future f = processor.submit(new CellUpdateTask(cell));
        
        // wait for it to complete
        f.get();
    }
    
    /**
     * Update a tree of cells
     * @param cell the cell to update
     */
    public void updateTree(final Cell cell) 
        throws ExecutionException, InterruptedException
    {
        // submit the update job
        Future f = processor.submit(new CellTreeUpdateTask(cell));
        
        // wait for it to complete
        f.get();
    }
    
    /**
     * Shutdown the persistence
     */
    public void shutdown() {
        if (directoryUpdate != null) {
            directoryUpdate.cancel(true);
        }
        
        if (processor != null) {
            processor.shutdown();
        }
    }
    
    /**
     * Get the lock for reading and writing
     * @return the lock
     */
    public ReadWriteLock getLock() {
        return lock;
    }
    
    /**
     * Translate a cell to wfs
     * @param cell the cell to translate
     * @return the corresponding WFS cell
     */
    protected BasicCellGLOSetup toWFS(Cell cell) {
        return converter.toWFS(cell);
    }
    
    /**
     * Translate a wfs cell into a cell
     * @param wfsCell the wfs cell to translate
     * @return the corresponding cell
     */
    protected Cell fromWFS(WFSCell wfsCell) {
        return converter.fromWFS(wfsCell);
    }
    
    /**
     * Intialize WFS
     */
    private WFSCell initWFS() throws IOException {
        String wfsRootDefault = "file:" + System.getProperty("user.home") +
                WFS_ROOT_DEFAULT;
        String wfsRoot = WonderlandServletUtil.getProperty(WFS_ROOT_PROP,
                servletContext, wfsRootDefault);
        URL wfsRootURL = new URL(wfsRoot);

        // get the subdirectory, if any
        rootCellId = WonderlandServletUtil.getProperty(WFS_DIR_PROP,
                servletContext, WFS_DIR_DEFAULT);

        // create the root cell
        Cell root = new Cell(rootCellId);

        // open or create the directory
        WFSCellDirectory wfsRootDir =
            WonderlandServletUtil.openWFS(wfsRootURL, rootCellId,
                new WonderlandServletUtil.DefaultWFSCellCreator() {

                    @Override
                    public BasicCellGLOSetup getSetup(WFSCell cell)
                            throws IOException
                    {
                        // don't change the setup if it already exists
                        try {
                            if (cell.getCellSetup() != null) {
                                return cell.getCellSetup();
                            }
                        } catch (InvalidWFSCellException iwce) {
                            // ignore -- in this case, we should just go 
                            // ahead and create the cell
                        } catch (IOException ioe) {
                            // ignore -- in this case, we should just go 
                            // ahead and create the cell
                        }

                        BasicCellGLOSetup setup = super.getSetup(cell);
                        setup.setBoundsRadius(1024.0);
                        setup.setBoundsType("BOX");
                        setup.setOrigin(new double[] { 35.0, 0.0, 35.0 });
                        return setup;
                    }
                });

        // get the root cell
        WFSCell rootWFS = wfsRootDir.getAssociatedCell();
        if (rootWFS == null) {
            throw new IOException("Unable to find root cell " + rootCellId + 
                    " in " + wfsRootURL + ". Ensure the the " +
                    "wonderland.wfs.worldbuilder.dir property is set.");
        }

        // add a record of the root cell
        CellInfo info = add(null, root, rootWFS);

        // make sure root gets re-read during the first scan
        info.setUpdateTime(0);
        
        return rootWFS;
    }
    
    /**
     * Add a record of a cell
     * @param parent the parent cell to add to
     * @param cell the cell to add
     * @param wfsCell the cell in WFS that represents this cell
     * @return the CellInfo object that represents this cell
     */
    private CellInfo add(Cell parent, Cell cell, WFSCell wfsCell)
        throws IOException
    {
        logger.info("New cell: " + cell.getCellID());

        if (cells.containsKey(cell.getCellID())) {
            throw new IOException("Duplicate cell ID: " + cell.getCellID() + 
                    " in " + wfsCell.getCanonicalName() + " and " + 
                    wfsCells.get(cell.getCellID()));
        }

        // add the cell to its parent
        if (parent != null) {
            parent.addChild(cell);
        }

        // update maps
        cells.put(cell.getCellID(), cell);
        CellInfo info = new CellInfo(cell, wfsCell.getLastModified());
        cellInfo.put(wfsCell.getCanonicalName(), info);
        wfsCells.put(cell.getCellID(), wfsCell);
    
        return info;
    }
    
    /**
     * Update a cell, replacing it with the new version
     * @param parent the parent cell
     * @param cell the cell to update
     * @param wfsCell the cell in WFS that represents this cell
     * @return the updated CellInfo object
     */
    private CellInfo update(Cell parent, Cell cell, WFSCell wfsCell)
        throws IOException
    {
        logger.info("Updated cell: " + cell.getCellID());

        CellInfo info = cellInfo.get(wfsCell.getCanonicalName());
        if (info == null) {
            throw new IOException("No cell for " + wfsCell.getCanonicalName());
        }
        
        if (!cell.getCellID().equals(info.getCell().getCellID())) {
            throw new IOException("Attempt to change cell ID: " + 
                    wfsCell.getCanonicalName() + " should be: " + 
                    info.getCell().getCellID());
        }

        // update cell in its parent
        if (parent != null) {
            parent.addChild(cell);
        }
        
        // set the update time
        info.setUpdateTime(wfsCell.getLastModified());

        // update any children
        for (Cell child : info.getCell().getChildren()) {
            cell.addChild(child);
        }

        // now replace the cell in the map & info
        info.setCell(cell);
        cells.put(cell.getCellID(), cell);
        
        return info;
    }
    
    /**
     * Remove our record of a cell.  Must be called from
     * processor thread.
     * @param removed the cell that was removed
     */
    private void remove(Cell removed) {
        logger.info("Removed cell: " + removed.getCellID());
                
        if (removed.getParent() != null) {
            removed.getParent().removeChild(removed);
        }
                
        cleanupTree(removed);
    }
    
    /**
     * Clean up a removed cell and all its children
     * @param removed the removed cell
     */
    private void cleanupTree(Cell removed) {
        logger.info("Cleanup cell: " + removed.getCellID());

        cells.remove(removed.getCellID());
        WFSCell wfsCell = wfsCells.remove(removed.getCellID());
        if (wfsCell != null) {
            cellInfo.remove(wfsCell.getCanonicalName());
        }

        // cleanup children
        for (Cell child : removed.getChildren()) {
            cleanupTree(child);
        }
    }
    
    /**
     * Find a cell in wfs
     * @param directory the directory to search
     * @param name the name of the cell
     * @return the cell with the given name, or null if no cell
     * exists in the directory with the given name
     */
    private WFSCell find(WFSCellDirectory dir, String cellName) {
         WFSCell[] children = dir.getCells();
         WFSCell out = null;
         for (WFSCell child : children) {
            if (child.getCellName().equals(cellName)) {
                out = child;
                break;
            }
         }
         
         return out;
    }
    
    /**
     * Read cells from disk
     */
    class ReadWFSTask extends TreeChangeTask implements Runnable {
        private WFSCell wfsRoot;
         
        public ReadWFSTask(WFSCell wfsRoot) {
            this.wfsRoot = wfsRoot;
        }
        
        public void run() {
            try {
                call();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error during directory scan", ex);
            }
        }
        
        /**
         * Process the tree and update the appropriate lists.  The addList
         * should include all cells that were added in this update.  The
         * updateList should include all cells that were updated, and the
         * remove list should include all cells that were removed.
         */
        protected void process(List<CellUpdateRecord> addList,
                               List<CellUpdateRecord> updateList,
                               List<CellUpdateRecord> removeList)
            throws Exception
        {
            processCell(null, wfsRoot, addList, updateList, removeList);
        }

        private void processDirectory(WFSCellDirectory dir, Cell parent,
                                      List<CellUpdateRecord> addList,
                                      List<CellUpdateRecord> updateList,
                                      List<CellUpdateRecord> removeList) 
                throws IOException 
        {
            // make a list of expected children
            Collection<Cell> expectedChildren = parent.getChildren();
            
            // process each file
            for (WFSCell child : dir.getCells()) {
                processCell(parent, child, addList, updateList, removeList);
           
                // remove the corresponding cell from the set of expected
                // children
                CellInfo info = cellInfo.get(child.getCanonicalName());
                if (info != null) {
                    expectedChildren.remove(info.getCell());
                }
            }
            
            // now remove references to any cells that weren't removed from
            // the expected children list
            for (Cell removed : expectedChildren) {
                removeList.add(new CellUpdateRecord(parent, removed, null));
            }
        }
        
        private void processCell(Cell parent, 
                                 WFSCell wfsCell,
                                 List<CellUpdateRecord> addList,
                                 List<CellUpdateRecord> updateList,
                                 List<CellUpdateRecord> removeList) 
                throws IOException 
        {     
            Cell cell = null;
            
            // see if we have any information about this file
            CellInfo info = cellInfo.get(wfsCell.getCanonicalName());
            if (info == null || wfsCell.getLastModified() > info.getUpdateTime()) {
                logger.info("Update cell: " + wfsCell.getCanonicalName());
                
                // read the cell from WFS
                cell = fromWFS(wfsCell);
                
                // now update the info record
                if (info == null) {
                    addList.add(new CellUpdateRecord(parent, cell, wfsCell));
                } else {
                    updateList.add(new CellUpdateRecord(parent, cell, wfsCell));
                }
            } else {
                cell = info.getCell();
            }
            
            if (wfsCell.getCellDirectory() != null) {
                processDirectory(wfsCell.getCellDirectory(), cell,
                                 addList, updateList, removeList);
            }
        }
    }

    class CellUpdateTask implements Callable {
        private Cell cell;
        
        public CellUpdateTask(Cell cell) {
            this.cell = cell;
        }
        
        public Object call() throws Exception {
            try {
                // acquire a write lock
                getLock().writeLock().lock();
                
                // find the cell in the file system
                WFSCell wfsCell = wfsCells.get(cell.getCellID());
                if (wfsCell == null) {
                    throw new IllegalStateException("No such cell: " + cell.getCellID());
                }

                // update the cell's file
                wfsCell.setCellSetup(toWFS(cell));
                
                // update the cell in memory
                Cell orig = cells.get(cell.getCellID());
                update(orig.getParent(), cell, wfsCell);
                
                wfsCell.write();
            } finally {
                getLock().writeLock().unlock();
            }
            
            return null;
        }
    }
    
    class CellTreeUpdateTask extends TreeChangeTask {
        private Cell cell;
        
        public CellTreeUpdateTask(Cell cell) {
            this.cell = cell;
        }
        
        protected void process(List<CellUpdateRecord> addList,
                               List<CellUpdateRecord> updateList,
                               List<CellUpdateRecord> removeList)
            throws Exception
        {
            processTree(cell, addList, updateList, removeList);
        }
        
        protected void processTree(Cell cell,
                                   List<CellUpdateRecord> addList,
                                   List<CellUpdateRecord> updateList,
                                   List<CellUpdateRecord> removeList)
        {
            // get the current version of this cell
            Cell orig = cells.get(cell.getCellID());
            if (orig == null) {
                throw new IllegalArgumentException("No such cell " +
                                                   cell.getCellID());
            }
            
            // make sure there isn't a version problem
            if (orig.getVersion() != cell.getVersion()) {
                throw new IllegalStateException("Cell version mismatch." + 
                        "Cell " + cell.getCellID() + " expected " +
                        orig.getVersion() + " got " + cell.getVersion());
            }
            
            // get the wfs cell associated with this cell
            WFSCell wfsCell = wfsCells.get(cell.getCellID());
            
            // get the parent cell.  The parent is the new cell's parent,
            // unless the cell doesn't have a parent, in which case we
            // use the original cell's parent
            Cell parent = cell.getParent();
            if (parent == null) {
                parent = orig.getParent();
            }
            updateList.add(new CellUpdateRecord(parent, cell, wfsCell));
            
            // removed children are in the original list but not the
            // new list
            List<Cell> removedChildren = orig.getChildren();
            removedChildren.removeAll(cell.getChildren());
            for (Cell child : removedChildren) {
                WFSCell childWFS = wfsCells.get(child.getCellID());
                removeList.add(new CellUpdateRecord(cell, child, childWFS));
            }
            
            // added children are in the new list but not the original
            // list
            List<Cell> addedChildren = cell.getChildren();
            addedChildren.removeAll(orig.getChildren());
            for (Cell child : addedChildren) {
                addChildTree(cell, wfsCell, child, addList);
            }
            
            // all other children are just updated
            List<Cell> updatedChildren = cell.getChildren();
            updatedChildren.removeAll(addedChildren);
            for (Cell child : updatedChildren) {
                processTree(child, addList, updateList, removeList);
            }
        }
        
        private void addChildTree(Cell parent, WFSCell parentWFS,
                                  Cell child, List addList)
        {
            // create the new cell in WFS.  The file won't actually be created
            // until we write to it in the post-process method
            WFSCellDirectory parentDir = parentWFS.getCellDirectory();
            if (parentDir == null) {
                parentDir = parentWFS.createCellDirectory();
            }
            
            WFSCell childWFS = parentDir.addCell(child.getCellID());
                
            // add the record to the list
            addList.add(new CellUpdateRecord(parent, child, childWFS));
     
            // add children
            for (Cell grandChild : child.getChildren()) {
                addChildTree(child, childWFS, grandChild, addList);
            }
        }
        
        /**
         * Post-process the tree and apply changes listed in the appropriate
         * lists.  The addList will include all cells that were successfully
         * added in this update.  The updateList will include all cells that 
         * were updated, and the remove list will include all cells that were 
         * removed.
         */
        @Override
        protected void postProcess(List<CellUpdateRecord> addList,
                                   List<CellUpdateRecord> updateList,
                                   List<CellUpdateRecord> removeList)
            throws Exception
        {
            // update the WFS to reflect the changes
            
            // remove and files that need removing
            for (CellUpdateRecord remove : removeList) {
                Cell parent = remove.getParent();
                WFSCell parentWFS = wfsCells.get(parent.getCellID());
                parentWFS.getCellDirectory().removeCell(remove.getWFSCell());
            }
            
            // add new files
            for (CellUpdateRecord add : addList) {
                WFSCell addWFS = add.getWFSCell();
                addWFS.setCellSetup(toWFS(add.getCell()));
            }
            
            // update files
            for (CellUpdateRecord update : updateList) {
                WFSCell updateWFS = update.getWFSCell();
                updateWFS.setCellSetup(toWFS(update.getCell()));
            }
            
            // get the wfs cell associated with this cell
            WFSCell wfsCell = wfsCells.get(cell.getCellID());
            wfsCell.write();
        }
    }
    
    abstract class TreeChangeTask implements Callable {
        public Object call() throws Exception {
            try {
                // get the write lock
                getLock().writeLock().lock();
                
                // collect changes
                List<CellUpdateRecord> addList    = new ArrayList();
                List<CellUpdateRecord> updateList = new ArrayList();
                List<CellUpdateRecord> removeList = new ArrayList();
                
                // collect records that need updating
                process(addList, updateList, removeList);
                
                // apply the updates.  Make sure to remove before adding,
                // to prevent incorrect duplication errors
                for (CellUpdateRecord removed : removeList) {
                    remove(removed.getCell());
                }
                
                for (Iterator<CellUpdateRecord> i = addList.iterator();
                     i.hasNext();)
                {
                    CellUpdateRecord added = i.next();
                    try {
                        add(added.getParent(), added.getCell(), added.getWFSCell());
                    } catch (IOException ioe) {
                        logger.log(Level.WARNING, "Error adding " + 
                                   added.getCell().getCellID(), ioe);
                        i.remove();
                    }
                }
                
                for (Iterator<CellUpdateRecord> i = updateList.iterator();
                     i.hasNext();)
                {
                    CellUpdateRecord updated = i.next();
                    try {
                        update(updated.getParent(), updated.getCell(), updated.getWFSCell());
                    } catch (IOException ioe) {
                        logger.log(Level.WARNING, "Error updating " + 
                                   updated.getCell().getCellID(), ioe);
                        i.remove();
                    }
                }
                
                // post process with updated lists
                postProcess(addList, updateList, removeList);
            } finally {
                // release the lock
                getLock().writeLock().unlock();
            }
            
            return null;
        }
        
        /**
         * Process the tree and update the appropriate lists.  The addList
         * should include all cells that were added in this update.  The
         * updateList should include all cells that were updated, and the
         * remove list should include all cells that were removed.
         */
        protected abstract void process(List<CellUpdateRecord> addList,
                                        List<CellUpdateRecord> updateList,
                                        List<CellUpdateRecord> removeList)
            throws Exception;
        
        /**
         * Post-process the tree and apply changes listed in the appropriate
         * lists.  The addList will include all cells that were successfully
         * added in this update.  The updateList will include all cells that 
         * were updated, and the remove list will include all cells that were 
         * removed.
         */
        protected void postProcess(List<CellUpdateRecord> addList,
                                   List<CellUpdateRecord> updateList,
                                   List<CellUpdateRecord> removeList)
            throws Exception
        {
        }
    }
    
    class CellInfo {

        private Cell cell;
        private long updateTime;

        public CellInfo(Cell cell, long updateTime) {
            this.cell = cell;
            this.updateTime = updateTime;
        }

        public Cell getCell() {
            return cell;
        }

        public void setCell(Cell cell) {
            this.cell = cell;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }
    
    class CellUpdateRecord {
        private Cell parent;
        private Cell cell;
        private WFSCell wfsCell;
        
        public CellUpdateRecord(Cell parent, Cell cell, WFSCell wfsCell) {
            this.parent = parent;
            this.cell = cell;
            this.wfsCell = wfsCell;
        }
        
        public Cell getParent() {
            return parent;
        }
        
        public Cell getCell() {
            return cell;
        }
        
        public WFSCell getWFSCell() {
            return wfsCell;
        }
    }
}
