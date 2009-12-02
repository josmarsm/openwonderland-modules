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

package org.jdesktop.wonderland.modules.eventplayer.server.wfs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.wfs.CellList;
import org.jdesktop.wonderland.common.wfs.CellList.Cell;
import org.jdesktop.wonderland.modules.eventplayer.server.RecordingRoot;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;
import org.jdesktop.wonderland.server.wfs.importer.CellImporterUtils;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;
import org.xml.sax.InputSource;

/**
 * Helper utilities
 * @author Jordan Slott
 * @author Bernard Horan
 */
public class RecordingLoaderUtils {
    /* The prefix to add to URLs for the eventplayer web service */
    private static final String WEB_SERVICE_PREFIX = "eventplayer/eventplayer/resources/";
    
    /* The logger for this class */
    private static final Logger logger = Logger.getLogger(RecordingLoaderUtils.class.getName());



    /**
     * Retrieve a map of cells given by the name of the recording
     * The CellMap is ordered.
     *
     * @param tapeName The unique name of the recording
     * @return a map of cells that have been loaded. The map is ordered
     * @throws JAXBException
     * @throws IOException
     */
    public static CellMap<CellImportEntry> loadCellMap(String tapeName) throws JAXBException, IOException {
        logger.info("tapeName: " + tapeName);
        RecordingRoot recordingRoot = getRecordingRoot(tapeName);
        logger.info("recordingRoot: " + recordingRoot);
        String recordingName = recordingRoot.getRootPath();
        logger.info("recordingName: " + recordingName);
        CellMap<CellImportEntry> cellMOMap = new CellMap<CellImportEntry>();
        //logger.info("rootName: " + recorderName);
        /* A queue (last-in, first-out) containing a list of cell to search down */
        LinkedList<CellList> children = new LinkedList<CellList>();

        /* Find the children in the top-level directory and go! */
        CellList dir = CellImporterUtils.getWFSRootChildren(recordingName);
        if (dir == null) {
            /* Log an error and return, though this should never happen */
            logger.warning("RecordingLoader: did not find root directory for wfs " + recordingName);
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
                logger.warning("RecordingLoader: could not fetch child dir in WFS " + recordingName);
                continue;
            }
            //logger.info("WFSLoader: processing children in " + childdir.getRelativePath());

            /* Recursively load the cells for this child */
            CellMap<CellImportEntry> map = loadCellMap(recordingName, childdir, children);
            if (map != null) {
                cellMOMap.putAll(map);
            }
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
     * @return the map of cells, ordered
     */
    public static CellMap<CellImportEntry> loadCellMap(String root, CellList dir, LinkedList<CellList> children) {
        /* Conatins a map of canonical cell names in WFS to cell objects */
        CellMap<CellImportEntry> cellMOMap = new CellMap<CellImportEntry>();
        /*
         * Fetch an array of the names of the child cells. Check this is not
         * null, although this getChildren() should return an empty array
         * instead.
         */
        Cell childs[] = dir.getChildren();
        if (childs != null) {
            logger.info("childs length: " + childs.length);
        }
        if (childs == null) {
            logger.warning("RecordingLoader: no children in " + dir.getRelativePath());
            return null;
        }

        /*
         * Loop throuch each of the child names and attempt to create a cell
         * based upon it. Then update the cell map to indicate that the object
         * exists and the last time it was modified on disk.
         */
        for (Cell child : childs) {
            logger.info("RecordingLoader: processing child " + child.name);
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
            logger.info("server state: " + importEntry.serverState.toString());

            /*
             * If the cell is at the root, then the relative path will be "/"
             * and we do not want to prepend it to the cell path.
             */
            String cellPath = importEntry.relativePath + "/" + importEntry.name;
            if (importEntry.relativePath.compareTo("") == 0) {
                cellPath = child.name;
            }


            cellMOMap.put(cellPath, importEntry);
            logger.info("RecordingLoader: putting " + cellPath + " into map with " + importEntry);


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

    /**
     * Get the recordingRoot object for a recording
     * @param tapeName the name of the recording
     * @return a recording root object that identifies the path of the recording
     * @throws javax.xml.bind.JAXBException
     * @throws java.io.IOException
     */
    public static RecordingRoot getRecordingRoot(String tapeName) throws JAXBException, IOException {
        //logger.info("tapeName: " + tapeName);
        String encodedName = URLEncoder.encode(tapeName, "UTF-8");
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "getrecording/" + encodedName);
        //logger.info("url: " + url);
        return RecordingRoot.decode(new InputStreamReader(url.openStream()));
    }

    /**
     * Get an input source for the changes file of a recording
     * @param tapeName the name of the recording
     * @return an input source for use by an XML parser
     * @throws javax.xml.bind.JAXBException
     * @throws java.io.IOException
     */
    public static InputSource getRecordingInputSource(String tapeName) throws JAXBException, IOException {
        //WorldRoot recordingRoot = getRecordingRoot(tapeName);
        //logger.info("recordingRoot: " + recordingRoot);
        String encodedName = URLEncoder.encode(tapeName, "UTF-8");
        URL url = new URL(CellImporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "getrecording/" + encodedName + "/changes");
        //logger.info("url: " + url);

        return new InputSource(new InputStreamReader(url.openStream()));
    }

    /**
     * Get an input source for the changes file of a recording
     * @param tapeName the name of the recording
     * @return an input source for use by an XML parser
     * @throws javax.xml.bind.JAXBException
     * @throws java.io.IOException
     */
    public static Reader getRecordingInputReader(String tapeName) throws JAXBException, IOException {
        //WorldRoot recordingRoot = getRecordingRoot(tapeName);
        //logger.info("recordingRoot: " + recordingRoot);
        String encodedName = URLEncoder.encode(tapeName, "UTF-8");
        URL url = new URL(CellImporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "getrecording/" + encodedName + "/changes");
        //logger.info("url: " + url);

        return new InputStreamReader(url.openStream());
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
