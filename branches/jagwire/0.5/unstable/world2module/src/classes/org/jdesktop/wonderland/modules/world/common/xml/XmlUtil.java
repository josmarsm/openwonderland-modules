package org.jdesktop.wonderland.modules.world.common.xml;

import java.nio.charset.Charset;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This is a simple utility class to provide static utility methods to
 * prevent duplication of code for some workarounds regarding working with XML.
 *
 * @author Carl Jokl
 */
public abstract class XmlUtil {

    /**
     * The UTF-8 Character set which is used by default for encoding XML.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * The default XML version to use in the XML document prefix.
     */
    public static final String DEFAULT_XML_VERSION = "1.0";

    /**
     * Convert an encoding name from the java encoding name
     * which often omits a hyphen in the name to the standard
     * XML encoding name with hyphens.
     *
     * @param encodingName The raw name of the encoding.
     * @return The corresponding XML encoding name.
     */
    public static String toXmlEncodingName(String encodingName) {
        if (encodingName.matches("UTF\\d{1,2}")) {
            encodingName = encodingName.replace("UTF", "UTF-");
        }
        else if (encodingName.matches("ISO\\d{4}\\.*")) {
            encodingName = encodingName.replace("ISO", "ISO-");
        }
        return encodingName;
    }

    /**
     * Convert the specified DOCTYPE attributes into a DOCTYPE string.
     *
     * @param name The DOCTYPE name.
     * @param publicId The public id of the DOCTYPE.
     * @param systemId The system id of the DOCTYPE.
     * @return
     */
    public static String toDTDString(String name, String publicId, String systemId) {
        StringBuilder dtdBuilder = new StringBuilder();
        dtdBuilder.append("<!DOCTYPE ");
        dtdBuilder.append(name);
        if (publicId != null) {
            dtdBuilder.append(" PUBLIC ");
            dtdBuilder.append('"');
            dtdBuilder.append(publicId);
            dtdBuilder.append('"');
        }
        if (systemId != null) {
            dtdBuilder.append(' ');
            dtdBuilder.append('"');
            dtdBuilder.append(systemId);
            dtdBuilder.append('"');
        }
        dtdBuilder.append(">\n");
        return dtdBuilder.toString();
    }

    /**
     * This method is used to write the XML document declaration at the beginning of an XML
     * document. This version is used when the standalone attribute is to be written as the
     * XMLStreamWriter provides no way for writing this attribute.
     *
     * @throws SAXException If an error occurs writing the identifier.
     */
    public static void writeXMLDeclaration(XMLStreamWriter outputWriter, String xmlVersion, String encodingName, boolean standalone) throws XMLStreamException {
        if (xmlVersion == null || xmlVersion.trim().isEmpty()) {
            xmlVersion = DEFAULT_XML_VERSION;
        }
        StringBuilder xmlDeclarationBuilder = new StringBuilder();
        xmlDeclarationBuilder.append("<?xml version=\"");
        xmlDeclarationBuilder.append(xmlVersion);
        xmlDeclarationBuilder.append('"');
        if (encodingName != null) {
            xmlDeclarationBuilder.append(" encoding=\"");
            xmlDeclarationBuilder.append(encodingName);
            xmlDeclarationBuilder.append('"');
        }
        xmlDeclarationBuilder.append(" standalone=\"");
        xmlDeclarationBuilder.append(standalone ? "yes" : "no");
        xmlDeclarationBuilder.append("\"?>\n");
        //Work around to avoid the angle brackets getting escaped.
        outputWriter.writeDTD(xmlDeclarationBuilder.toString());
    }
}
