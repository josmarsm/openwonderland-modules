package org.jdesktop.wonderland.modules.world.common.xml.tags;

import java.io.FileFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jdesktop.wonderland.modules.world.common.xml.XmlStateAttributes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This abstract class is intended to simplify the common TagContentHandler
 * scenario whereby the intention is just to modify the text within a
 * specific tag. The implementation of this class should much simpler
 * than implementing the entire TagContentHandler interface but
 * TagContentHandlers which require more control than this
 * adapter provides can still implement the full interface
 * instead of using this adapter.
 *
 * @author Carl Jokl
 */
public abstract class SimpleTagContentHandlerAdapter implements TagContentHandler {

    private StringBuilder tagContentBuilder;
    private String[] compatibleRootTags;
    private String[] handledTags;
    private FileFilter handledFiles;
    private Attributes currentAttributes;

    /**
     * This constructor is intended to be called by implementations of this class to
     * set the tags and files which the implementation handles.
     *
     * @param handledFiles A FileFilter used to filter out just those files which the
     *                     implementing TagContentHandler is set up to handle.
     * @param handledTags The local names or fully qualified names of the tags which
     *                    the implementation of this class handles.
     */
    protected SimpleTagContentHandlerAdapter(FileFilter handledFiles, String... handledTags) {
        this.handledTags = handledTags;
        this.handledFiles = handledFiles;
        tagContentBuilder = new StringBuilder();
    }

    /**
     * This method is intended to be called by implementors of this class to
     * set the compatible root tags with which the TagContentHandler is
     * designed to work. The method is final so that is can be safely called
     * inside a constructor.
     *
     * @param compatibleRootTags Variable number of Strings which are names of
     *                           compatible root tags.
     */
    protected final void setCompatibleRootTags(String... compatibleRootTags) {
        this.compatibleRootTags = compatibleRootTags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileFilter getHandledFiles() {
        return handledFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getHandledTags() {
        return handledTags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getCompatibleRootTags() {
        return compatibleRootTags;
    }

    /**
     * Get the current Attributes for the tag being handled.
     * This method can be called by an implementation of the
     * adapter if the attributes are needed to modify the
     * contents of the tag.
     *
     * @return The XML Attributes of the tag currently being handled.
     */
    protected Attributes getCurrentAttributes() {
        return currentAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean characters(char[] ch, int start, int length, XMLStreamWriter destination, XmlStateAttributes state) throws SAXException {
        tagContentBuilder.append(ch, start, length);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ignorableWhitespace(char[] ch, int start, int length, XMLStreamWriter destination, XmlStateAttributes state) throws SAXException {
        tagContentBuilder.append(ch, start, length);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startElement(String uri, String localName, String qName, Attributes attributes, XMLStreamWriter destination, XmlStateAttributes state) throws SAXException {
        tagContentBuilder.delete(0, tagContentBuilder.length());
        this.currentAttributes = attributes;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endElement(String uri, String localName, String qName, XMLStreamWriter destination, XmlStateAttributes state) throws SAXException {
        currentAttributes = null;
        try {
            destination.writeCharacters(modifyValue(tagContentBuilder.toString(), uri, localName, qName));
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Error writing the modified contents of a tag!", xse);
        }
        tagContentBuilder.delete(0, tagContentBuilder.length());
        return true;
    }

    /**
     * This method is intended to be implemented by concrete implementations of this adapter.
     * The method is supplied the text content of a tag which this TagContentHandler is registered
     * handle and this method is called when the end tag is reached and all the text and whitespace
     * in between the start and end tags is converted to a the value supplied to this method.
     *
     * Other details of the tag for which the content is being supplied for modification are also
     * supplied in case the implementation needs to know which tag this text originated from.
     *
     * The method then returns the text modified by the TagContentHandler which will be written between
     * the handled start and end tags instead of the original value.
     *
     * @param value All the text and whitespace encountered between the start and end tags.
     * @param uri The URI of the tag from which the text originates (if needed).
     * @param localName The local name of the tag from which the text originates (if needed).
     * @param qName The qualified name of the tag from which the text originates (if needed).
     * @return The modified text value to be put inside the handled tag instead of its original content.
     */
    public abstract String modifyValue(String value, String uri, String localName, String qName);
}
