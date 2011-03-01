package org.jdesktop.wonderland.modules.world.server.converter.xml;

import org.jdesktop.wonderland.modules.world.common.xml.SAXParserDocumentDetailsRetriever;
import org.jdesktop.wonderland.modules.world.common.xml.XmlDocumentDetailsRetriever;
import org.jdesktop.wonderland.modules.world.common.xml.XmlConversionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jdesktop.wonderland.modules.world.common.xml.XmlUtil;
import org.jdesktop.wonderland.modules.world.common.xml.tags.TagContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class is used to encapsulate the functionality to take an XML file to be modified as it is converted from a World or 
 * Snapshot into a module.
 *
 * @author Carl Jokl
 */
public class ModuleXmlConverter {

    private XmlConversionHandler xmlModifier;
    private XmlDocumentDetailsRetriever xmlDocDetailsRetriever;
    private SAXParser conversionParser;

    /**
     * Create a new ModuleXmlConverter to use to convert XmlFiles from
     * Snapshot / World versions to versions suitable for use in
     * a Module.
     */
    public ModuleXmlConverter() {
        xmlModifier = new XmlConversionHandler();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setXIncludeAware(true);
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(true);
        try {
            conversionParser = parserFactory.newSAXParser();
            //ToDo: Remove force standalone setting if a way is found to get this to be properly detected from the parser.
            xmlDocDetailsRetriever = new SAXParserDocumentDetailsRetriever(conversionParser, true);
            xmlModifier.setXmlDocumentDetailsRetriever(xmlDocDetailsRetriever);
            try {
                conversionParser.setProperty(SAXParserDocumentDetailsRetriever.SAX_LEXICAL_HANDLER_PROPERTY_NAME, xmlModifier);
            }
            catch (SAXNotRecognizedException snre) {
                Logger.getLogger(getClass().getName()).warning("Cannot set the LexicalHandler as it is not supported!");
            }
            catch (SAXNotSupportedException snse) {
                Logger.getLogger(getClass().getName()).warning("Cannot set the LexicalHandler as it is not supported!");
            }
        }
        catch (ParserConfigurationException pce) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to configure Parser for Module XML conversion!", pce);
        }
        catch (SAXException se) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error while creating a parser for Module XML conversion!", se);
        }
    }

    /**
     * The TagContentHandlers to be used in converting the XML for use in a Module.
     * 
     * @param tagHandlers The TagContentHandlers to be used for converting XML for use
     *                    in a Module.
     */
    public void setHandlers(Collection<TagContentHandler> tagHandlers) {
        xmlModifier.clearHandlers();
        xmlModifier.addHandlers(tagHandlers);
    }

    /**
     * This is the method with the main functionality for this class. It will perform a conversion of the source XML from the
     * specified source InputStream to Module specific XML for use in a Module.
     *
     * @param xmlInputStream An InputStream containing the source XML to be converted.
     * @param convertedXmlDestination An OutputStream to which the converted XML should be written.
     * @param closeSourceAfter Whether the source stream should be closed after the conversion.
     * @param closeDestinationAfter Whether the destination stream should be closed after the conversion.
     * @return True if the conversion completed successfully.
     */
    public boolean convertXml(InputStream xmlInputStream, OutputStream convertedXmlDestination, boolean closeSourceAfter, boolean closeDestinationAfter) {
        if (conversionParser != null && xmlInputStream != null && convertedXmlDestination != null) {
            String encodingName = XmlUtil.UTF8.displayName();
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
            try {
                XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter(new OutputStreamWriter(convertedXmlDestination, XmlUtil.UTF8));
                xmlModifier.setContentDestination(xmlWriter, encodingName);
                conversionParser.parse(xmlInputStream, xmlModifier);
                xmlWriter.flush();
                if (closeSourceAfter) {
                    xmlInputStream.close();
                }
                if (closeDestinationAfter) {
                    xmlWriter.close();
                }
                return true;
            }
            catch (SAXException se) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "A SAX Exception occurred while trying to convert an XML document for use within a module!", se);
            }
            catch (XMLStreamException xse) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to create a XMLStreamWriter to write the destination XML!", xse);
            }
            catch (IOException ioe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "An IO Exception occurred while trying to convert an XML document for use within a module", ioe);
            }
        }
        return false;
    }

    /**
     * This is an overloaded version of the convertXml method provided for convenience to
     * convert XML from the specified source
     *
     * @param sourceXmlFile The file which contains the source XML to be converted.
     * @param convertedXmlDestination The destination stream into which to write the converted XML.
     * @param closeDestinationAfter Whether the destination stream should be closed after the conversion.
     * @return True if the XML was able to be converted successfully.
     */
    public boolean convertXml(File sourceXmlFile, OutputStream convertedXmlDestination, boolean closeDestinationAfter) {
        try {
            return convertXml(new FileInputStream(sourceXmlFile), convertedXmlDestination, true, closeDestinationAfter);
        }
        catch (FileNotFoundException fnfe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "File did not exit for which to convert XML to Module suitable format!", fnfe);
            return false;
        }
    }
}
