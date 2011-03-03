package org.jdesktop.wonderland.modules.world.server.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jdesktop.wonderland.modules.world.common.xml.XmlUtil;
import org.jdesktop.wonderland.modules.world.server.converter.xml.ModuleXmlConverter;
import org.jdesktop.wonderland.modules.world.common.xml.tags.TagHandlerLoader;
import org.jdesktop.wonderland.modules.world.server.converter.xml.tags.ColladaTagHandler;
import org.jdesktop.wonderland.modules.world.common.xml.tags.ContentHandlingConstants;
import org.jdesktop.wonderland.modules.world.common.xml.tags.DependencyListenable;
import org.jdesktop.wonderland.modules.world.common.xml.tags.DependentResourceNotificationListener;
import org.jdesktop.wonderland.modules.world.common.xml.tags.ModuleConversionInitializable;
import org.jdesktop.wonderland.modules.world.server.converter.xml.tags.ImageTagHandler;
import org.jdesktop.wonderland.modules.world.common.xml.tags.TagContentHandler;

/**
 * This is the main class to use for converting a world into a module.
 *
 * @author Carl Jokl
 */
public class WorldToModuleConverter {

    /**
     * The size of the byte buffer to use when performing a plain copy of a file into
     * the Module archive.
     */
    protected static final int PLAIN_COPY_BUFFER_SIZE = 1024;

    /**
     * Get the Wonderland File System directory for the specified snapshot or world.
     * 
     * @param name The name of the snapshot or world.
     * @param serverMetaData The serverMetaData to use to find the location of key directories from
     *                       which to find the WFS directory of the snapshot or world.
     * @param snapshot True if the WFS directory to be found is for a snapshot. False if the WFS directory
     *                 to be found is for a world.
     * @return A file containing the expected location of the WFS directory or null if one of the parameters
     *         was null or empty.
     */
    protected static File toWorldWfsDir(String name, ServerMetaData serverMetaData, boolean snapshot) {
        if (name != null && !name.isEmpty() && serverMetaData != null) {
           return new File(new File(snapshot ? serverMetaData.getSnapshotsDir() : serverMetaData.getWorldsDir(), name), serverMetaData.getWorldWFSDirectoryName());
        }
        return null;
    }

    /**
     * create a Jar manifest based on the specified module meta-data;
     *
     * @param moduleMetaData The module meta-data from which to populate the Jar attributes.
     * @param
     * @return The Manifiest which has been populated from the specified ModuleMetaData.
     */
    protected static Manifest toManifest(ModuleMetaData moduleMetaData, ServerMetaData serverMetaData) {
        Manifest moduleManifest = new Manifest();
        Map<String, Attributes> manifestEntries = moduleManifest.getEntries();
        Attributes currentAttributes = new Attributes();
        currentAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        if (serverMetaData != null) {
            currentAttributes.putValue("Created-By", String.format("%d.%d (Wonderland)", serverMetaData.getMajorVersion(), serverMetaData.getMinorVersion()));
        }
        if (moduleMetaData != null) {

        }
        manifestEntries.put(moduleMetaData.getModuleName(), currentAttributes);
        
        return moduleManifest;
    }

    /**
     * Convert the specified Snapshot into a module.
     *
     * (this method may need to be modified to throw exceptions rather than just returning null on failure).
     *
     * @param worldName The name of the Snapshot or world to be converted into a module.
     * @param snapshot True if the world is a snapshot, false if the world is a regular world.
     * @param moduleMetaData The meta-data about the module to be created.
     * @param serverMetaData Meta-data about the current server from which the Snapshot will be exported.
     * @return A File which points to the Module archive containing the exported module (if successful).
     */
    public File convertSnapshotToModule(String worldName, boolean snapshot, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData) {
        File moduleFile = null;
        File worldWfsDir = toWorldWfsDir(worldName, serverMetaData, snapshot);
        //This check is also made in the other method but checking here saves processing / cleanup if the snapshot or world does not exist.
        if (worldWfsDir != null && worldWfsDir.exists() && worldWfsDir.isDirectory()) {
            File createdModulesDir = serverMetaData.getCreatedModulesDir();
            boolean createdModulesDirExists = false;
            if (createdModulesDir.exists()) {
                createdModulesDirExists = true;
            }
            else {
                File parent = createdModulesDir.getParentFile();
                if (parent != null && parent.exists() && createdModulesDir.mkdir()) {
                    createdModulesDirExists = true;
                }
                else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Destination directory into which the created module will be writted does not exist and could not be created!");
                }
            }
            if (createdModulesDirExists) {
                moduleFile = new File(createdModulesDir, String.format("%s.jar", moduleMetaData.getModuleName()));
                boolean moduleFileCreated = false;
                if (moduleFile.exists()) {
                    if (!moduleFile.delete()) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to delete existing archive file!");
                        return null;
                    }
                    else {
                        try {
                            moduleFileCreated = moduleFile.createNewFile();
                        }
                        catch (IOException ioe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while creating module file!", ioe);
                        }
                    }
                }
                else {
                    try {
                        moduleFileCreated = moduleFile.createNewFile();
                    }
                    catch (IOException ioe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while creating module file!", ioe);
                    }
                }
                if (moduleFileCreated) {
                    try {
                        if (!createModuleArchive(new FileOutputStream(moduleFile), worldName, snapshot, moduleMetaData, serverMetaData)) {
                            moduleFile = null;
                        }
                    }
                    catch (FileNotFoundException fnfe) {
                        /* Should not be possible due to other checks (unless someone deletes the file after it is created)
                           but exception must be handled. */
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error, module file not found when trying to open file for writing!", fnfe);
                        moduleFile = null;
                    }
                }
            }
        }
        return moduleFile;
    }

    /**
     * Having identified the module archive file location, proceed to create the module archive.
     *
     * @param moduleOutputStream The output stream for the module archive to be created.
     * @param worldName The name of the Snapshot or world to be converted into a module.
     * @param snapshot True if the world is a snapshot, false if the world is a regular world.
     * @param moduleMetaData The meta-data about the module to be created.
     * @param serverMetaData The meta-data about the server on which this snapshot / world is being converted to a module.
     * @return True if the archive was able to be created successfully.
     */
    public boolean createModuleArchive(OutputStream moduleOutputStream, String worldName, boolean snapshot, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData) {
        boolean success = true;
        File worldWfsDir = toWorldWfsDir(worldName, serverMetaData, snapshot);
        //Check all the required parameters exist. The world name as server meta data parameters will have been checked by toWorldWfsDir.
        if (worldWfsDir != null && worldWfsDir.exists() && moduleOutputStream != null && moduleMetaData != null) {
            try {
                /* Create a jar file. */
                JarOutputStream moduleJarOutputStream = new JarOutputStream(moduleOutputStream, toManifest(moduleMetaData, serverMetaData));
                File[] worldFiles = worldWfsDir.listFiles();
                /* Create the main directories in the Jar file. */
                String wfsDirName = String.format("%s/", serverMetaData.getWFSDirectoryName());
                moduleJarOutputStream.putNextEntry(new ZipEntry(wfsDirName));
                moduleJarOutputStream.closeEntry();
                String projectWfsDirName = String.format("%s%s-%s/", wfsDirName, moduleMetaData.getModuleName(), serverMetaData.getWFSDirectoryName());
                moduleJarOutputStream.putNextEntry(new ZipEntry(projectWfsDirName));
                moduleJarOutputStream.closeEntry();
                /* Create Module Info and Module Requirements XML files. */
                ModuleXmlFileCreator creator = new ModuleInfoXmlFileCreator();
                success = creator.writeAsEntry(moduleJarOutputStream, XmlUtil.UTF8, moduleMetaData, serverMetaData);
                creator = new ModuleDependenciesXmlFileCreator();
                success = success && creator.writeAsEntry(moduleJarOutputStream, XmlUtil.UTF8, moduleMetaData, serverMetaData);
                if (success) {
                    /* Process XML files */
                    Collection<TagContentHandler> allTagHandlers = getTagHandlerLoader().loadTagConentHandlers();
                    Map<String, String> dependencies = new HashMap<String, String>();
                    Map<String, String> optionalDependencies = new HashMap<String, String>();
                    DependencyListBuildingDependencyListener dependencyListener = new DependencyListBuildingDependencyListener(dependencies, optionalDependencies);
                    ModuleXmlConverter converter = new ModuleXmlConverter();
                    for (TagContentHandler handler : allTagHandlers) {
                        if (handler instanceof ModuleConversionInitializable) {
                            ((ModuleConversionInitializable) handler).init(moduleMetaData, serverMetaData);
                        }
                        if (handler instanceof DependencyListenable) {
                            ((DependencyListenable) handler).setDependencyListener(dependencyListener);
                        }
                    }
                    converter.setHandlers(allTagHandlers);
                    InputStream currentInputStream = null;
                    for (File worldFile : worldFiles) {
                        moduleJarOutputStream.putNextEntry(new ZipEntry(projectWfsDirName.concat(worldFile.getName())));
                        currentInputStream = new FileInputStream(worldFile);
                        if (ContentHandlingConstants.WONDERLAND_CONTENT_FILE_FILTER.accept(worldFile)) {
                            if (!converter.convertXml(currentInputStream, moduleJarOutputStream, true, false)) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, String.format("Failed to convert file %s for use in module archive!", worldFile.getName()));
                                success = false;
                            }
                        }
                        else {
                            if (!plainCopy(currentInputStream, moduleJarOutputStream)) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, String.format("Failed copy file %s to module archive!", worldFile.getName()));
                                success = false;
                            }
                        }
                        moduleJarOutputStream.closeEntry();
                    }
                    if (!handleDependencies(serverMetaData, dependencies, optionalDependencies, converter, moduleJarOutputStream, allTagHandlers)) {
                         Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to handle files / art upon which the world depends!");
                         success = false;
                    }
                    dependencyListener.dispose();
                }
                moduleJarOutputStream.finish();
                moduleJarOutputStream.close();
            }
            catch (FileNotFoundException fnfe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create module archive as file did not exist!", fnfe);
            }
            catch (IOException ioe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while trying to write data to module!", ioe);
            }
        }
        else {
            success = false;
        }
        return success;
    }

    /**
     * Handle the all the files for which a dependency exists within the module.
     *
     * @param serverMetaData The server meta-data relating to the current running server which is being used to create the module.
     * @param dependency A map of all the source URIs to destination URIs for resources for which there is a dependency.
     * @param optionalDependencies A set containing a list of source URIs to be checked to see if any contain resources. If the
     *                             resources exist then there is a dependency and the resources should be included in the module.
     * @param xmlConverter The converter used to convert XML documents from the snapshot / world to a module.
     * @param destinationArchive The destinationArchive Zip archive into which the dependencies need to be copied.
     * @param tagHandlers All the TagContentHandlers which can be used to perform the XML conversions as needed.
     * @return True if the dependencies were able to be resolved successfully.
     */
    protected boolean handleDependencies(ServerMetaData serverMetaData, Map<String, String> dependencies, Map<String, String> optionalDependencies, ModuleXmlConverter xmlConverter, ZipOutputStream destinationArchive, Collection<TagContentHandler> tagHandlers) {
        boolean success = true;
        String[] currentSourceURIs = null;
        Set<String> createdDestinationDirectories = new HashSet<String>();
        String projectArtDirName = WonderlandDirectoryConstants.DEFAULT_ART_DIRECTORY_NAME;
        while (!dependencies.isEmpty() || !optionalDependencies.isEmpty()) {
            /* First remove the current dependencies. The dependency list may repopulate with more sub dependencies as dependencies
               are being handled. */
            if (!dependencies.isEmpty()) {
                currentSourceURIs = getKeyArray(dependencies);
                success = handleDependencies(serverMetaData, projectArtDirName, currentSourceURIs, getValues(dependencies, currentSourceURIs, true), false, xmlConverter, destinationArchive, tagHandlers, createdDestinationDirectories) && success;
            }
            if (!optionalDependencies.isEmpty()) {
                currentSourceURIs = getKeyArray(optionalDependencies);
                success = handleDependencies(serverMetaData, projectArtDirName, currentSourceURIs, getValues(optionalDependencies, currentSourceURIs, true), true, xmlConverter, destinationArchive, tagHandlers, createdDestinationDirectories) && success;
            }
        }
        return success;
    }

    /**
     * Get an array containing all the keys in the specified map.
     *
     * @param map The map from which to get an array of all the keys.
     * @return An array of all the keys within the map.
     */
    private static String[] getKeyArray(Map<String, String> map) {
        return map.keySet().toArray(new String[map.size()]);
    }

    /**
     * Get an array containing all the values in the specified map
     * which correspond to the array of specified keys.
     *
     * @param map The map from which to get the values.
     * @param keyOrder The keys used to specify the order in which the values will be populated in the returned array.
     * @param clearMap Whether the map should be cleared once the values have been retrieved.
     * @return An array of the values in the specified map which correspond to the specified keys.
     */
    private static String[] getValues(Map<String, String> map, String[] keyOrder, boolean clearMap) {
        String[] values = new String[keyOrder.length];
        for (int keyIndex = 0; keyIndex < keyOrder.length; keyIndex++) {
            values[keyIndex] = map.get(keyOrder[keyIndex]);
        }
        if (clearMap) {
            map.clear();
        }
        return values;
    }

    /**
     * Internal method for handling dependencies which have been processed into URI arrays and this method can be used for required or optional
     * dependencies.
     *
     * @param serverMetaData The server meta-data relating to the current running server which is being used to create the module.
     * @param projectArtDirName The name of the directory in which the project art is stored.
     * @param sourceURIs An array containing all the sourceURIs to the resources to be copied.
     * @param destinationURIs An array (of equal size to the sourceURIs array) which contains the destination to which the resources should be moved.
     * @param optionalResources Whether these resources are optional. If true then if any of the source resources cannot be found it is not considered an error.
     * @param xmlConverter The converter used to convert XML documents from the snapshot / world to a module.
     * @param destinationArchive The destinationArchive Zip archive into which the dependencies need to be copied.
     * @param tagHandlers All the TagContentHandlers which can be used to perform the XML conversions as needed.
     * @param createdDestinationDirectories This is a set of all the directories which have been created in the archive to prevent duplication.
     * @return True if the dependencies were able to be resolved successfully.
     */
    private boolean handleDependencies(ServerMetaData serverMetaData, String projectArtDirName, String[] sourceURIs, String[] destinationURIs, boolean optionalResources, ModuleXmlConverter xmlConverter, ZipOutputStream destinationArchive, Collection<TagContentHandler> tagHandlers, Set<String> createdDestinationDirectories) {
        boolean success = true;
        File currentSourceFile = null;
        String currentDestination = null;
        for (int dependencyIndex = 0; dependencyIndex < sourceURIs.length; dependencyIndex++) {
            currentSourceFile = resolveSourceFile(serverMetaData, sourceURIs[dependencyIndex]);
            if (currentSourceFile != null && currentSourceFile.exists()) {
                currentDestination = handleDestinationLocation(projectArtDirName, destinationURIs[dependencyIndex], currentSourceFile.isDirectory(), destinationArchive, createdDestinationDirectories);
                if (currentDestination != null) {
                    if (!handleCurrentResource(currentSourceFile, currentDestination, destinationArchive, tagHandlers, xmlConverter)) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, String.format("Could failed to transfer %s to module archive!", currentSourceFile.getName()));
                        success = false;
                    }
                }
                else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to derive a destination file name upon which to create a dependency!");
                    success = false;
                }
            }
            else if (!optionalResources) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not find source file upon which a dependency exists!");
                success = false;
            }
        }
        return success;
    }

    /**
     * Handle the destination location whereby the individual directories down to the location in the archive where the resource is to
     * be stored. This method also makes sure any directories which do not exist are created in the archive.
     *
     * @param projectArtDirName The name of the art directory within the archive.
     * @param destinationURI The destination URI for the resource.
     * @param isDir Whether the destination location is a directory.
     * @param archiveDestination The destination Zip archive which is the destination of the resource and which may require
     *                           directories to be created within it.
     * @param createdDestinationDirectories A set of directories which already exist within the archive.
     * @return The Zip based path to the resource.
     */
    protected String handleDestinationLocation(String projectArtDirName, String destinationURI, boolean isDir, ZipOutputStream archiveDestination, Set<String> createdDestinationDirectories) {
        String[] destinationPathElements = resolveDestinationPath(projectArtDirName, destinationURI);
        StringBuilder pathBuilder = new StringBuilder();
        String currentPath = null;
        if (destinationPathElements != null && destinationPathElements.length > 0) {
            final int max = destinationPathElements.length;
            for (int pathElementIndex = 0; pathElementIndex < max; pathElementIndex++) {
                pathBuilder.append(destinationPathElements[pathElementIndex]);
                if (isDir || (pathElementIndex + 1) < max) {
                    pathBuilder.append('/');
                    currentPath = pathBuilder.toString();
                    if (!createdDestinationDirectories.contains(currentPath)) {
                        try {
                            archiveDestination.putNextEntry(new ZipEntry(currentPath));
                            archiveDestination.closeEntry();
                            createdDestinationDirectories.add(currentPath);
                            
                        }
                        catch (IOException ioe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, String.format("Failed to directory: '%s' in the destination archive!", currentPath), ioe);
                            return null;
                        }
                    }
                }
            }
        }
        return pathBuilder.toString();
    }

    /**
     * Handle copying the current source file or directory to the destination URI in the specified archive.
     *
     * @param sourceResource The source resource which is either a file or directory.
     * @param destinationURI The destination URI to which the resource will be copied.
     * @param archiveDestination The destination archive into which the resource is to be copied.
     * @param xmlConverter The converter class to perform any XML conversions which may be required.
     * @param allTagHandlers A collection of all the TagContentHandlers which may be used in converting any XML as needed which requires changing to work with the module.
     * @return True if the operation completed successfully.
     */
    protected boolean handleCurrentResource(File sourceResource, String destinationFile, ZipOutputStream archiveDestination, Collection<TagContentHandler> allTagHandlers, ModuleXmlConverter xmlConverter) {
        boolean success = true;
        try {
            if (sourceResource.isDirectory()) {
                String childPath = null;
                for (File child : sourceResource.listFiles()) {
                    if (child.isDirectory()) {
                        childPath =  String.format("%s%s/", destinationFile, child.getName());
                        archiveDestination.putNextEntry(new ZipEntry(childPath));
                        archiveDestination.closeEntry();
                    }
                    else {
                        childPath = destinationFile.concat(child.getName());
                    }
                    success = handleCurrentResource(child, childPath, archiveDestination, allTagHandlers, xmlConverter) && success;
                }
            }
            else {
                archiveDestination.putNextEntry(new ZipEntry(destinationFile));
                List<TagContentHandler> applicableHandlers = new LinkedList<TagContentHandler>();
                for (TagContentHandler handler : allTagHandlers) {
                    if (handler.getHandledFiles().accept(sourceResource)) {
                        applicableHandlers.add(handler);
                    }
                }
                InputStream source = new FileInputStream(sourceResource);
                if (applicableHandlers.isEmpty()) {
                    if (!plainCopy(source, archiveDestination)) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to copy a resource on which the destination module is dependent into the module archive!");
                        success = false;
                    }
                }
                else {
                    xmlConverter.setHandlers(applicableHandlers);
                    if (!xmlConverter.convertXml(source, archiveDestination, true, false)) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to convert an XML resource on which the destination module is dependent for the module archive!");
                        success = false;
                    }
                }
                archiveDestination.closeEntry();
            }

        }
        catch (IOException ioe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error creating dependency resource in the destination module archive!", ioe);
            success = false;
        }
        return success;
    }

    /**
     * Resolve the location of source resource file using the specified parameters.
     *
     * @param serverMetaData The server meta-data which can be used to find content directory locations.
     * @param sourceURI The source URI which is to be converted to a File.
     * @return The file pointing to the resource in its source location or null if it could not be found.
     */
    protected File resolveSourceFile(ServerMetaData serverMetaData, String sourceURI) {
        if (sourceURI != null && serverMetaData != null) {
            sourceURI = sourceURI.trim();
            if (sourceURI.startsWith(ContentHandlingConstants.WONDERLAND_CONTENT_PREFIX)) {
                File contentRoot = serverMetaData.getContentRoot();
                String[] pathElements = sourceURI.substring(ContentHandlingConstants.WONDERLAND_CONTENT_PREFIX.length()).split("/");
                File currentLocation = contentRoot;
                int atIndex = 0;
                for (String element : pathElements) {
                    currentLocation = new File(currentLocation, element);
                    if (!currentLocation.exists()) {
                        atIndex = element.indexOf('@');
                        if (atIndex > 0) {
                            element = element.substring(0, atIndex);
                            currentLocation = new File(currentLocation.getParentFile(), element);
                            if (!currentLocation.exists()) {
                                break;
                            }
                        }
                    }
                }
                if (currentLocation.exists()) {
                    return currentLocation;
                }
            }
        }
        return null;
    }

    /**
     * Resolve the destination path elements from the project art directory name and the destination URI.
     *
     * @param projectArtDirName The name of the directory which contains the art for this module.
     * @param destinationURI The destination URI to which the resource should be copied.
     * @return An array of all the elements of the path (i.e. directories with possibly a file at the end)
     *         for the specified destination resource. Null will be returned if the resolving the path
     *         fails.
     */
    protected String[] resolveDestinationPath(String projectArtDirName, String destinationURI) {
        if (projectArtDirName != null && destinationURI != null) {
            destinationURI = destinationURI.trim();
            if (destinationURI.startsWith(ContentHandlingConstants.WONDERLAND_ART_PREFIX)) {
                /* Skip over the project name which should be the first item after the art prefix. */
                int slashIndex = destinationURI.indexOf('/', ContentHandlingConstants.WONDERLAND_ART_PREFIX.length());
                if (slashIndex > 0) {
                    destinationURI = projectArtDirName.concat(destinationURI.substring(slashIndex));
                }
            }
            return destinationURI.split("/");
        }
        return null;
    }

    /**
     * Perform a plain copy of the specified source resource to the specified
     * destination stream.
     *
     * @param source The source stream from which the resource will be read.
     * @param destination The destination stream into which the resource will be written.
     * @return True if the resource was able to be copied successfully.
     */
    protected boolean plainCopy(InputStream source, OutputStream destination) {
        byte[] plainCopyBuffer = new byte[PLAIN_COPY_BUFFER_SIZE];
        int bytesCopied = 0;
        /* Perform a plain copy */
        try {
            bytesCopied = source.read(plainCopyBuffer);
            while (bytesCopied > 0) {
                destination.write(plainCopyBuffer);
                destination.flush();
                bytesCopied = source.read(plainCopyBuffer);
            }
            destination.flush();
            source.close();
            return true;
        }
        catch (IOException ioe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while trying to copy a resource required by the module!", ioe);
        }
        return false;
    }

    /**
     * Get a loader used to load the available tag handlers for use in exporting a Snapshot to a Module.
     *
     * @return The TagHandlerLoader used to load the TagContentHandlers for use in this conversion.
     */
    protected TagHandlerLoader getTagHandlerLoader() {
        return new DefaultTagHandlerLoader();
    }

    /**
     * Utility class used to populate a list of dependencies via acting as a dependency listener for the tag handlers.
     */
    private static class DependencyListBuildingDependencyListener implements DependentResourceNotificationListener {

        /**
         * Dependencies in the form of a map. The nature of the map prevents duplications of the same dependency.
         */
        private Map<String, String> dependencyDestination;

        /**
         * All sources is used as a secondary prevention of duplication. As the dependency destination may be cleared
         * between handling of dependency lists. This is an extra check to make sure resource is not already been
         * satisfied.
         */
        private Set<String> allDestinations;

        /**
         * Destination of any optional dependencies.
         */
        private Map<String, String> optionalDependencyDestination;

        /**
         * Create a new instance of DependencyListBuildingDependencyListener which
         * adds dependency objects to the specified list every time it is informed of
         * a dependency.
         * 
         * @param dependencyDestination The destination map into which the dependencies will be
         *                              put when the object is informed of dependencies. The key
         *                              is the source URI and the value is the destination URI.
         * @param optionalDependencyDestination The destination map into which any optional dependencies will be put when the object
         *                                      is informed of optional dependencies. The key
         *                                      is the source URI to check and the value is the destination URI.
         */
        public DependencyListBuildingDependencyListener(Map<String, String> dependencyDestination, Map<String, String> optionalDependencyDestination) {
            this.dependencyDestination = dependencyDestination;
            this.optionalDependencyDestination = optionalDependencyDestination;
            allDestinations = new HashSet<String>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dependencyFound(String originalURI, String destinationURI) {
            if (dependencyDestination != null && !allDestinations.contains(destinationURI)) {
                dependencyDestination.put(originalURI, destinationURI);
                allDestinations.add(destinationURI);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void optionalDependencyCheck(String checkURI, String destinationURI) {
            if (optionalDependencyDestination != null && !allDestinations.contains(destinationURI)) {
                optionalDependencyDestination.put(checkURI, destinationURI);
                allDestinations.add(destinationURI);
            }
        }

        /**
         * Dispose of the internal state in order to free up memory.
         */
        public void dispose() {
            if (dependencyDestination != null) {
                dependencyDestination.clear();
                dependencyDestination = null;
            }
            if (optionalDependencyDestination != null) {
                optionalDependencyDestination.clear();
                optionalDependencyDestination = null;
            }
            if (allDestinations != null) {
                allDestinations.clear();
                allDestinations = null;
            }
        }
    }

    /**
     * Default simple implementation of a TagHandlerLoader.
     */
    private static class DefaultTagHandlerLoader implements TagHandlerLoader {

        /**
         * Create a new instance of DefaultTagHandlerLoader with the specified meta-data.
         */
        public DefaultTagHandlerLoader() {
            
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<TagContentHandler> loadTagConentHandlers() {
            Collection<TagContentHandler> handlers = new ArrayList<TagContentHandler>(2);
            handlers.add(new ColladaTagHandler());
            handlers.add(new ImageTagHandler());
            return handlers;
        }
    }
}
