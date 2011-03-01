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
 * This class is used to generate the module info XML file which is part of a modules meta-data
 * describing the other modules on which it relies.
 *
 * @author Carl Jokl
 */
public class ModuleInfoXmlFileCreator implements ModuleXmlFileCreator {

    /**
     * The name of the module info XML file.
     */
    public static final String XML_FILE_NAME = "module.xml";

    /**
     * The XML tag name for the tag which contains the module information / meta-data.
     */
    public static final String MODULE_INFO_OUTER_TAG_NAME = "wonderland-module";

    /**
     * The name of the module name XML tag.
     */
    public static final String MODULE_NAME_TAG_NAME = "name";

    /**
     * The name of the module version XML tag.
     */
    public static final String MODULE_VERSION_TAG_NAME = "version";

    /**
     * The name of the module major version XML tag.
     */
    public static final String MODULE_MAJOR_VERSION_TAG_NAME = "major";

    /**
     * The name of the module minor version XML tag.
     */
    public static final String MODULE_MINOR_VERSION_TAG_NAME = "minor";

    /**
     * The name of the module mini version XML tag.
     */
    public static final String MODULE_MINI_VERSION_TAG_NAME = "mini";

    /**
     * The name of the module description XML tag.
     */
    public static final String MODULE_DESCRIPTION_TAG_NAME = "description";

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
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to create Zip entry for the module information xml file!", ioe);
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
                writer.writeStartElement(MODULE_INFO_OUTER_TAG_NAME);
                writer.writeCharacters("\n\t");
                writer.writeStartElement(MODULE_NAME_TAG_NAME);
                writer.writeCharacters(moduleMetaData.getModuleName());
                /* End of module name tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n\t");
                writer.writeStartElement(MODULE_VERSION_TAG_NAME);
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement(MODULE_MAJOR_VERSION_TAG_NAME);
                writer.writeCharacters(Integer.toString(moduleMetaData.getMajorVersion()));
                /* End of the major version tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement(MODULE_MINOR_VERSION_TAG_NAME);
                writer.writeCharacters(Integer.toString(moduleMetaData.getMinorVersion()));
                /* End of the major version tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement(MODULE_MINI_VERSION_TAG_NAME);
                writer.writeCharacters(Integer.toString(moduleMetaData.getMiniVersion()));
                /* End of the major version tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n\t");
                /* End of the version tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n\t");
                writer.writeStartElement(MODULE_DESCRIPTION_TAG_NAME);
                writer.writeCharacters(moduleMetaData.getDescription());
                /* End of module name tag. */
                writer.writeEndElement();
                writer.writeCharacters("\n");
                /* End of module info outer tag. */
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
                if (closeDestination) {
                    writer.close();
                }
                return true;
            }
            catch (XMLStreamException xse) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error creating the module info xml file content!", xse);
            }
        }
        return false;
    }
}
