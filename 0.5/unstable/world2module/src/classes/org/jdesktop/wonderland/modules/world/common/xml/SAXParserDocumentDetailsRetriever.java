package org.jdesktop.wonderland.modules.world.common.xml;

import javax.xml.parsers.SAXParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class wraps a SAX parser and uses it to retrieve XML document details when the parser
 * is far enough through the parsing to have the details available.
 *
 * @author Carl Jokl
 */
public class SAXParserDocumentDetailsRetriever implements XmlDocumentDetailsRetriever {

    /**
     * The SAX property name to use to try and find whether an XML document is standalone.
     */
    public static final String SAX_IS_STANDALONE_PROPERTY_NAME = "http://xml.org/sax/features/is-standalone";

    /**
     * The SAX property name to use to try and find the XML version of a document.
     */
    public static final String SAX_VERSION_PROPTERTY_NAME = "http://xml.org/sax/properties/document-xml-version";

    /**
     * The SAX property name to use to try and set the lexical handler for a parser.
     */
    public static final String SAX_LEXICAL_HANDLER_PROPERTY_NAME = "http://xml.org/sax/properties/lexical-handler";

    private SAXParser parser;
    private boolean forceStandalone;

    /**
     * Create a new instance of SAXParserDocumentDetailsRetriever to wrap the specified
     * SAXParser.
     * 
     * @param parser The SAXParser to be wrapped which cannot be null.
     * @throws IllegalArgumentException If the specified SAX Parser was null.
     */
    public SAXParserDocumentDetailsRetriever(SAXParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("The SAX Parser to be wrapped cannot be null!");
        }
        this.parser = parser;
    }

    /**
     * Create a new instance of SAXParserDocumentDetailsRetriever to wrap the specified
     * SAXParser.
     *
     * @param parser The SAXParser to be wrapped which cannot be null.
     * @param forceStandalone If true then standalone will return true regardless of the standalone value retrieved from the
     *                        SAXParser. This can be used in situations where XML documents are known to be standalone but
     *                        where the standalone features is not properly implemented by the SAX parser.
     * @throws IllegalArgumentException If the specified SAX Parser was null.
     */
    public SAXParserDocumentDetailsRetriever(SAXParser parser, boolean forceStandalone) {
        this(parser);
        this.forceStandalone = forceStandalone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStandalone() throws UnsupportedOperationException {
        try {
            return forceStandalone || parser.getXMLReader().getFeature(SAX_IS_STANDALONE_PROPERTY_NAME);
        }
        catch (SAXNotRecognizedException snre) {
            throw new UnsupportedOperationException("XML Parser does not support getting whether standalone mode is being used from the document!", snre);
        }
        catch (SAXNotSupportedException snse) {
            throw new UnsupportedOperationException("XML Parser does not support getting whether standalone mode is being used from the document!", snse);
        }
        catch (SAXException se) {
            throw new UnsupportedOperationException("XML Parser does not support getting whether standalone mode is being used from the document!", se);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXmlVersion() throws UnsupportedOperationException {
        try {
            Object version = parser.getProperty(SAX_VERSION_PROPTERTY_NAME);
            return version != null ? version.toString() : null;
        }
        catch (SAXNotRecognizedException snre) {
            throw new UnsupportedOperationException("XML Parser does not support getting the XML version from the document!", snre);
        }
        catch (SAXNotSupportedException snse) {
            throw new UnsupportedOperationException("XML Parser does not support getting the XML version from the document!", snse);
        }
    }


}
