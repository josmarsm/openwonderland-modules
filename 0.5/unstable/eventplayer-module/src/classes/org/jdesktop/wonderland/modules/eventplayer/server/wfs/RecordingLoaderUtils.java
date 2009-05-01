/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.wfs;

import com.sun.sgs.app.ManagedReference;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.wfs.CellList;
import org.jdesktop.wonderland.common.wfs.CellList.Cell;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.wfs.importer.CellImporter;
import org.jdesktop.wonderland.server.wfs.importer.CellImporterUtils;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 *
 * @author bh37721
 */
public class RecordingLoaderUtils {
    /* The logger for the wfs loader */
    private static final Logger logger = Logger.getLogger(RecordingLoaderUtils.class.getName());

    /**
     * Loads a WFS root into the world, based in the given WFSCellMO with a
     * unique root name.
     * The CellMap is ordered.
     *
     * @param rootName The unique root name of the WFS
     * @param cellID the parent to which the root should be added. May be null.
     */
    public static CellMap<CellImportEntry> loadCellMap(String recorderName) {
        CellMap<CellImportEntry> cellMOMap = new CellMap();
        //logger.info("rootName: " + recorderName);
        /* A queue (last-in, first-out) containing a list of cell to search down */
        LinkedList<CellList> children = new LinkedList<CellList>();

        /* Find the children in the top-level directory and go! */
        CellList dir = CellImporterUtils.getWFSRootChildren(recorderName);
        if (dir == null) {
            /* Log an error and return, though this should never happen */
            logger.warning("WFSLoader: did not find root directory for wfs " + recorderName);
            return null;
        }
        children.addFirst(dir);

        /*
         * Loop until the 'children' Queue is entirely empty, which means that
         * we have loaded all of the cells and have searched all possible sub-
         * directories. The loadCells() method will add entries to children as
         * needed.
         */
        while (children.isEmpty() == false) {
            /* Fetch and remove the first on the list and load */
            CellList childdir = children.removeFirst();
            if (childdir == null) {
                /* Log an error and continue, though this should never happen */
                logger.warning("WFSLoader: could not fetch child dir in WFS " + recorderName);
                continue;
            }
            //logger.info("WFSLoader: processing children in " + childdir.getRelativePath());

            /* Recursively load the cells for this child */
            CellMap map = loadCellMap(recorderName, childdir, children);
            cellMOMap.putAll(map);
        }
         return cellMOMap;
    }

    /**
     * Recurisvely loads cells from a given child directory (dir) in the WFS
     * given by root. If this child has any children directories, then add
     * to the children parameter.
     *
     * @param root The root directory of the WFS being loaded
     * @param dir The current directory of children to load
     * @param children A list of child directories remaining to be loaded
     */
    public static CellMap<CellImportEntry> loadCellMap(String root, CellList dir, LinkedList<CellList> children) {
        /* Conatins a map of canonical cell names in WFS to cell objects */
        CellMap<CellImportEntry> cellMOMap = new CellMap();
        /*
         * Fetch an array of the names of the child cells. Check this is not
         * null, although this getChildren() should return an empty array
         * instead.
         */
        Cell childs[] = dir.getChildren();
        //logger.info("childs length: " + childs.length);
        if (childs == null) {
            logger.warning("WSLoader: could not read children in WFS " + root);
            return null;
        }

        /*
         * Loop throuch each of the child names and attempt to create a cell
         * based upon it. Then update the cell map to indicate that the object
         * exists and the last time it was modified on disk.
         */
        for (Cell child : childs) {
            //logger.info("WFSLoader: processing child " + child.name);
            CellImportEntry importEntry = new CellImportEntry(child.name);
            /*
             * Fetch the relative path of the parent. Check if null, although
             * this should never be the case. Then fetch the parent cell object.
             */
            importEntry.relativePath = dir.getRelativePath();
            if (importEntry.relativePath == null) {
                logger.warning("null relative path for cell " + importEntry.name);
                continue;
            }

            /*
             * Download and parse the cell configuration information. Create a
             * new cell based upon the information.
             */
            importEntry.serverState = CellImporterUtils.getWFSCell(root, importEntry.relativePath, importEntry.name);
            if (importEntry.serverState == null) {
                logger.warning("unable to read cell serverState info " + importEntry.relativePath + "/" + importEntry.name);
                continue;
            }
            //logger.info(setup.toString());

            /*
             * If the cell is at the root, then the relative path will be "/"
             * and we do not want to prepend it to the cell path.
             */
            String cellPath = importEntry.relativePath + "/" + importEntry.name;
            if (importEntry.relativePath.compareTo("") == 0) {
                cellPath = child.name;
            }


            cellMOMap.put(cellPath, importEntry);
            //logger.info("WFSLoader: putting " + cellPath + " into map with " + importEntry);


            /*
             * See if the cell has any children and add to the linked list.
             */
            CellList newChildren = CellImporterUtils.getWFSChildren(root, cellPath);
            if (newChildren != null) {
                children.addLast(newChildren);
            }
        }
        return cellMOMap;
    }

    /** An entry holding details about a cell to export */
    public static class CellImportEntry {
        private String relativePath;
        private String name;
        private CellServerState serverState;

        private CellImportEntry(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public CellServerState getServerState() {
            return serverState;
        }
    }
    

}
