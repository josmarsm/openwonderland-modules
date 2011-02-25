package org.jdesktop.wonderland.modules.world.common.xml;

/**
 * This interface represents an object which can be used to get information an XML document
 * such as the version.
 *
 * @author Carl Jokl
 */
public interface XmlDocumentDetailsRetriever {

    /**
     * Whether the XML document being parsed is standalone.
     * 
     * @return True if the XML document being parsed is standalone.
     * @throws UnsupportedOperationException If the retriever fails to get the information.
     */
    public boolean isStandalone() throws UnsupportedOperationException;

    /**
     * Get the XML version of the document being parsed.
     *
     * @return A string containing the version of the XML document being parsed or null
     *         if it could not be retrieved.
     * @throws UnsupportedOperationException If the retriever fails to get the information.
     */
    public String getXmlVersion() throws UnsupportedOperationException;
}
