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

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.CellDimension;

/**
 *
 * @author jkaplan
 */
public class CellPersistence {
    private static final Logger logger =
            Logger.getLogger(CellPersistence.class.getName());
    
    /** singleton */
    private static CellPersistence persistence;
   
    /** the root cell id */
    private static final String ROOT_CELL_ID = "root";
    
    /** cells we known about, mapped by id */
    private Map<String, Cell> cells;
     
    /** map from cellID to path */
    private Map<String, String> paths;
    
    /** map from path to cell info */
    private Map<String, CellInfo> cellInfo;
    
    /** single thread to handle reading and writing */
    private ScheduledExecutorService processor;
    
    /** the task that periodically updates the directory */
    private ScheduledFuture directoryUpdate;
    
    /** a lock on the data */
    private ReadWriteLock lock;
    
    public synchronized static CellPersistence get() {
        if (persistence == null) {
            persistence = new CellPersistence();
        }
        
        return persistence;
    }
    
    /**
     * Singelton: Use getInstance() instead of constructor.
     */
    private CellPersistence() {
        cells = Collections.synchronizedMap(new HashMap());
    
        // these maps are only ever access from inside the executor, so
        // they do not need to be syncrhonized
        paths = new HashMap();
        cellInfo = new HashMap();
        
        // get the directory to synchronized with
        String cellDir = System.getProperty("cell.dir");
        if (cellDir == null) {
            cellDir = System.getProperty("user.home") + 
                      File.separator + ".worldBuilder";
        }
        File dir = new File(cellDir);
        File rootFile = new File(dir, "root-cell.xml");
        
        // create the root cell
        Cell root = new Cell(ROOT_CELL_ID);
        root.setSize(new CellDimension(1024, 1024));

        try {
            // create the file if it doesn't exist
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            if (!rootFile.exists()) {
                write(root, rootFile);
            }
            
            CellInfo info = add(null, root, rootFile);
            
            // make sure root gets re-read during the first scan
            info.setUpdateTime(0);   
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading root file: " + ioe, ioe);
        }    
        
        // initialize the lock
        lock = new ReentrantReadWriteLock();
        
        // create the processor service
        processor = Executors.newSingleThreadScheduledExecutor();
        
        // create the directory update task
        Runnable dirTask = new ReadDirectoryTask(dir, rootFile);
        
        // run the directory update the first time and wait for it to
        // complete
        try {
            Future future = processor.submit(dirTask);
            future.get();
        } catch (Exception ex) {
            throw new RuntimeException("Error starting persitence for " + dir, ex);
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
        return get(ROOT_CELL_ID);
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
    public void update(final Cell cell) {
        processor.submit(new CellUpdateTask(cell));
    }
    
    /**
     * Update a tree of cells
     * @param cell the cell to update
     */
    public void updateTree(final Cell cell) {
        processor.submit(new CellTreeUpdateTask(cell));
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
     * Add a record of a cell
     * @param parent the parent cell to add to
     * @param cell the cell to add
     * @param file the file that represents this cell
     * @return the CellInfo object that represents this cell
     */
    private CellInfo add(Cell parent, Cell cell, File file)
        throws IOException
    {
        logger.info("New cell: " + cell.getCellID());

        if (cells.containsKey(cell.getCellID())) {
            throw new IOException("Duplicate cell ID: " + cell.getCellID() + 
                    " in " + file.getCanonicalPath() + " and " + 
                    paths.get(cell.getCellID()));
        }

        // add the cell to its parent
        if (parent != null) {
            parent.addChild(cell);
        }

        // update maps
        cells.put(cell.getCellID(), cell);
        CellInfo info = new CellInfo(cell, file.lastModified());
        cellInfo.put(file.getCanonicalPath(), info);
        paths.put(cell.getCellID(), file.getCanonicalPath());
    
        return info;
    }
    
    /**
     * Update a cell, replacing it with the new version
     * @param parent the parent cell
     * @param cell the cell to update
     * @param file the file representing this cell
     * @return the updated CellInfo object
     */
    private CellInfo update(Cell parent, Cell cell, File file)
        throws IOException
    {
        logger.info("Updated cell: " + cell.getCellID());

        CellInfo info = cellInfo.get(file.getCanonicalPath());
        if (info == null) {
            throw new IOException("No cell for " + file.getCanonicalPath());
        }
        
        if (!cell.getCellID().equals(info.getCell().getCellID())) {
            throw new IOException("Attempt to change cell ID: " + 
                    file.getCanonicalPath() + " should be: " + 
                    info.getCell().getCellID());
        }

        // update cell in its parent
        if (parent != null) {
            parent.addChild(cell);
        }
        
        // set the update time
        info.setUpdateTime(file.lastModified());

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
        String path = paths.remove(removed.getCellID());
        if (path != null) {
            cellInfo.remove(path);
        }

        // cleanup children
        for (Cell child : removed.getChildren()) {
            cleanupTree(child);
        }
    }
    
    /**
     * Write a cell to disk
     */
    private void write(Cell cell, File file) 
        throws IOException
    {
        // write the updated data
        XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file));
        encoder.setPersistenceDelegate(URI.class, new DefaultPersistenceDelegate() {
            @Override
            protected Expression instantiate(
                    final Object oldInstance, final Encoder out) {

                return new Expression(oldInstance, oldInstance.getClass(), "new",
                        new Object[]{oldInstance.toString()});
            }
        });
        encoder.writeObject(cell);
        encoder.close();
    }
    
    /** 
     * Strip off the tailing "-cell.xml" from a filename
     * @param name the name with "-cell.xml"
     * @return the name with "-cell.xml stripped off
     */
    private final String basename(String name) {
        // get the filename minus the -cell.xml
        int len = name.length() - "-cell.xml".length();
        return name.substring(0, len);
    }
    
    /** 
     * Get the directory name associated with a cell. Changes the 
     * "-cell.xml" to "-dir".
     * @param name the name with "-cell.xml"
     * @return the name with "-cell.xml stripped off and "-dir" added
     */
    private final String dirname(String name) {
        // get the filename minus the -cell.xml
        return basename(name) + "-dir";
    }
    
    /**
     * Read cells from disk
     */
    class ReadDirectoryTask extends TreeChangeTask 
            implements ExceptionListener 
    {
        private File rootDir;
        private File rootFile;
         
        public ReadDirectoryTask(File rootDir, File rootFile) {
            this.rootDir = rootDir;
            this.rootFile = rootFile;
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
            processFile(rootDir, null, rootFile, addList, updateList, removeList);
        }

        private void processDirectory(File directory, Cell cell,
                                      List<CellUpdateRecord> addList,
                                      List<CellUpdateRecord> updateList,
                                      List<CellUpdateRecord> removeList) 
                throws IOException 
        {
            // make a list of expected children
            Collection<Cell> expectedChildren = cell.getChildren();
            
            // find all files matching "basename-cell.xml"
            File[] files = directory.listFiles(new FilenameFilter() {
                public boolean accept(File file, String fileName) {
                    return fileName.endsWith("-cell.xml");
                }
            });
           
            // process each file
            for (File file : files) {
                processFile(directory, cell, file, addList, 
                            updateList, removeList);
           
                // remove the corresponding cell from the set of expected
                // children
                CellInfo info = cellInfo.get(file.getCanonicalPath());
                if (info != null) {
                    expectedChildren.remove(info.getCell());
                }
            }
            
            // now remove references to any cells that weren't removed from
            // the expected children list
            for (Cell removed : expectedChildren) {
                removeList.add(new CellUpdateRecord(cell, removed, null));
            }
        }
        
        private void processFile(File directory, Cell parent, File file,
                                 List<CellUpdateRecord> addList,
                                 List<CellUpdateRecord> updateList,
                                 List<CellUpdateRecord> removeList) 
                throws IOException 
        {            
            final String basename = basename(file.getName());
            Cell cell;
            
            // see if we have any information about this file
            CellInfo info = cellInfo.get(file.getCanonicalPath());
            if (info == null || file.lastModified() > info.getUpdateTime()) {
                logger.info("Update file: " + file.getCanonicalPath());
                
                // it's a new or updated record, so read the cell
                XMLDecoder decoder = null;
                
                try {
                    decoder = new XMLDecoder(new FileInputStream(file), null, this);
                    cell = (Cell) decoder.readObject();     
                } finally {
                    if (decoder != null) {
                        decoder.close();
                    }
                }
                 
                // now update the info record
                if (info == null) {
                    addList.add(new CellUpdateRecord(parent, cell, file));
                } else {
                    updateList.add(new CellUpdateRecord(parent, cell, file));
                }
            } else {
                cell = info.getCell();
            }
            
            // find any subdirectories matching basename-dir
            File[] dirs = directory.listFiles(new FilenameFilter() {
                public boolean accept(File file, String fileName) {
                    return file.isDirectory() && 
                            fileName.equalsIgnoreCase(basename + "-dir");
                }
            });
            
            // process subdirectories
            if (dirs.length > 0) {
                processDirectory(dirs[0], cell, addList, updateList, removeList);
            }
        }
         
        public void exceptionThrown(Exception ex) {
            logger.log(Level.WARNING, "Error reading file", ex);
        }
    }

    class CellUpdateTask implements Runnable {
        private Cell cell;
        
        public CellUpdateTask(Cell cell) {
            this.cell = cell;
        }
        
        public void run() {
            try {
                // acquire a write lock
                getLock().writeLock().lock();
                
                // find the cell in the file system
                String path = paths.get(cell.getCellID());
                if (path == null) {
                    throw new IllegalStateException("No such cell: " + cell.getCellID());
                }

                // get the cell's file
                File file = new File(path);
                
                // write to disk
                write(cell, file);
                
                // update the cell in memory
                Cell orig = cells.get(cell.getCellID());
                update(orig.getParent(), cell, file);
                
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error updating cell", ex);
            } finally {
                getLock().writeLock().unlock();
            }
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
            
            // get the file associated with this cell
            File file = new File(paths.get(cell.getCellID()));
            
            // get the parent cell.  The parent is the new cell's parent,
            // unless the cell doesn't have a parent, in which case we
            // use the original cell's parent
            Cell parent = cell.getParent();
            if (parent == null) {
                parent = orig.getParent();
            }
            updateList.add(new CellUpdateRecord(parent, cell, file));
            
            // removed children are in the original list but not the
            // new list
            List<Cell> removedChildren = orig.getChildren();
            removedChildren.removeAll(cell.getChildren());
            for (Cell child : removedChildren) {
                File childFile = new File(file.getParent(), filename(file, child));
                removeList.add(new CellUpdateRecord(cell, child, childFile));
            }
            
            // added children are in the new list but not the original
            // list
            List<Cell> addedChildren = cell.getChildren();
            addedChildren.removeAll(orig.getChildren());
            for (Cell child : addedChildren) {
                addChildTree(cell, file, child, addList);
            }
            
            // all other children are just updated
            List<Cell> updatedChildren = cell.getChildren();
            updatedChildren.removeAll(addedChildren);
            for (Cell child : updatedChildren) {
                processTree(child, addList, updateList, removeList);
            }
        }
        
        private void addChildTree(Cell parent, File parentFile,
                                  Cell child, List addList)
        {
            String fileName = filename(parentFile, child);
            
            // create the child File.  Note this is just an abstract path
            // name, the file won't actually be created until we write to
            // it in postProcess().
            File childFile = new File(parentFile.getParentFile(), fileName);
            
            // add the record to the list
            addList.add(new CellUpdateRecord(parent, child, childFile));
     
            // add children
            for (Cell grandChild : child.getChildren()) {
                addChildTree(child, childFile, grandChild, addList);
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
            // update the file system
            
            // remove and files that need removing
            for (CellUpdateRecord remove : removeList) {
                File removeFile = remove.getFile();
                File removeDir = new File(removeFile.getParent(), 
                                          dirname(removeFile.getName()));
               
                // remove the directory
                if (removeDir.exists() && removeDir.isDirectory()) {
                    removeTree(removeDir);
                    removeDir.delete();
                }
                
                // remove the file
                removeFile.delete();
            }
            
            // add new files
            for (CellUpdateRecord add : addList) {
                File dir = add.getFile().getParentFile();
                if (!dir.exists()) {
                    dir.mkdir();
                }
                
                write(add.getCell(), add.getFile());
            }
            
            // update files
            for (CellUpdateRecord update : updateList) {
                write(update.getCell(), update.getFile());
            }
        }
        
        private void removeTree(File dir) {
            logger.info("Remove " + dir);
            
            File[] files = dir.listFiles();
            
            for (File file : files) {
                // clear out the directory
                if (file.isDirectory()) {
                    removeTree(file);
                }
                
                file.delete();
            }
        }
        
        private String filename(File parentFile, Cell child) {
            // calculate the child file name relative to the parent.
            // If the parent is 1-cell.xml, the child will be
            // 1-dir/2-cell.xml.
            String dirName = dirname(parentFile.getName());
            return dirName + File.separator + child.getCellID() + "-cell.xml";
        }
    }
    
    abstract class TreeChangeTask implements Runnable {
        public void run() {
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
                        add(added.getParent(), added.getCell(), added.getFile());
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
                        update(updated.getParent(), updated.getCell(), updated.getFile());
                    } catch (IOException ioe) {
                        logger.log(Level.WARNING, "Error updating " + 
                                   updated.getCell().getCellID(), ioe);
                        i.remove();
                    }
                }
                
                // post process with updated lists
                postProcess(addList, updateList, removeList);
                
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error during directory scan", ex);
            } finally {
                // release the lock
                getLock().writeLock().unlock();
            }
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
        private File file;
        
        public CellUpdateRecord(Cell parent, Cell cell, File file) {
            this.parent = parent;
            this.cell = cell;
            this.file = file;
        }
        
        public Cell getParent() {
            return parent;
        }
        
        public Cell getCell() {
            return cell;
        }
        
        public File getFile() {
            return file;
        }
    }
}
