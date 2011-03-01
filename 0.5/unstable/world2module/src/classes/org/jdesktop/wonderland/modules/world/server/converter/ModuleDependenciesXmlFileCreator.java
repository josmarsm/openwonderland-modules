package org.jdesktop.wonderland.modules.world.server.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jdesktop.wonderland.modules.world.common.xml.XmlUtil;

/**
 * This class is used to generate the module dependencies XML file which is part of a modules meta-data
 * describing the other modules on which it relies.
 *
 * @author Carl Jokl
 */
public class ModuleDependenciesXmlFileCreator implements ModuleXmlFileCreator {

    /**
     * The name of the module dependencies XML file.
     */
    public static final String XML_FILE_NAME = "requires.xml";

    /**
     * The XML tag name for the tag which contains the module dependency information.
     */
    public static final String MODULE_DEPENDENCIES_OUTER_TAG_NAME = "module-requires";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeAsEntry(ZipOutputStream destination, Charset encoding, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData) {
        boolean success = false;
        if (destination != null && encoding != null) {
            try {
                destination.putNextEntry(new ZipEntry(XML_FILE_NAME));
                success = writeFileContents(destination, encoding, moduleMetaData, serverMetaData, false);
                destination.closeEntry();
                success = true;
            }
            catch (IOException ioe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to create Zip entry for the module dependencies xml file!", ioe);
            }
        }
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeFileContents(OutputStream destination, Charset encoding, ModuleMetaData moduleMetaData, ServerMetaData serverMetaData, boolean closeDestination) {
        if (destination != null && encoding != null && moduleMetaData != null) {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
            try {
                XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(new OutputStreamWriter(destination, encoding));
                XmlUtil.writeXMLDeclaration(writer, XmlUtil.DEFAULT_XML_VERSION, encoding.displayName(), true);
                //ToDo: Do we need to deal with dependencies when creating a module from a world?
                writer.writeEmptyElement(MODULE_DEPENDENCIES_OUTER_TAG_NAME);
                writer.writeEndDocument();
                writer.flush();
                if (closeDestination) {
                    writer.close();
                }
                return true;
            }
            catch (XMLStreamException xse) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error creating the module dependencies xml file content!", xse);
            }
        }
        return false;
    }
}
