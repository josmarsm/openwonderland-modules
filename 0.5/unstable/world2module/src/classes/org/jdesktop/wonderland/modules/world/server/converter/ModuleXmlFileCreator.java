package org.jdesktop.wonderland.modules.world.server.converter;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipOutputStream;

/**
 * This is an interface for standard functionality for any
 * class which generates extra XML files to be part of 
 * a module being created.
 *
 * @author Carl Jokl
 */
public interface ModuleXmlFileCreator {

    /**
     * Write the XML file as an entry within the specified ZipOutputStream.
     *
     * @param destination The destination ZipOutputStream into which to create an entry for the XML file and write the contents.
     * @param encoding The character set encoding to be used for the XML.
     * @param moduleMetaData Meta-data about the module which can be used a part of the contents of the XML file.
     * @param serverMetaData Meta-data about the current running Wonderland server which may or may not be needed as part of
     *                       the generation of the XML.
     * @return True if the XML file entry was able to be created successfully. False otherwise.
     */
    public boolean writeAsEntry(ZipOutputStream destination, Charset encoding, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData);

    /**
     * Write the XML contents of the file to be created into the specified destination stream.
     *
     * @param destination The destination output stream into which the XML will be written.
     * @param encoding The character set encoding to use for the XML.
     * @param moduleMetaData Meta-data about the module which can be used a part of the contents of the XML file.
     * @param serverMetaData Meta-data about the current running Wonderland server which may or may not be needed as part of
     *                       the generation of the XML.
     * @param closeDestination Whether the destination stream should be closed after the file contents are written. True if the
     *                         end of the contents is the end of the stream or false if other data is to be written to
     *                         the stream an so it should not be closed.
     * @return True if the XML file contents were able to be created successfully. False otherwise.
     */
    public boolean writeFileContents(OutputStream destination, Charset encoding, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData, boolean closeDestination);
}
