package org.jdesktop.wonderland.modules.world.common.xml;

import javax.xml.stream.XMLStreamException;
import org.jdesktop.wonderland.modules.world.common.xml.tags.TagContentHandler;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamWriter;
import org.jdesktop.wonderland.modules.world.common.xml.tags.ActiveStateListeningTagContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This class is an implementation of a DefaultHandler used to modify
 * XML files as part of the process of converting a snapshot or deployed
 * world into a module or for other potential xml conversions besides this.
 * 
 * This class wraps a content handler which is the destination for the converted
 * content.
 *
 * @author Carl Jokl
 */
public class XmlConversionHandler extends DefaultHandler2 {

    private XMLStreamWriter contentDestination;
    private XmlDocumentDetailsRetriever xmlDocDetailsRetriever;
    private String encodingName;
    private TagHandlerGroup handlers;
    private List<PrefixMapping> pendingPrefixMappings;
    private boolean inCDATA;
    private XmlStateAttributes state;

    /**
     * This is a no argument constructor for the XmlConversionHandler
     * which may be more suitable if this instances is to be reused with
     * different XmlWriters and so the specific XmlWriter can be set
     * at the time of writing.
     *
     * Failure to set an XmlWriter before this instance is used will
     * result in a NullPointerException.
     */
    public XmlConversionHandler() {
        handlers = new TagHandlerGroup();
        pendingPrefixMappings = new LinkedList<PrefixMapping>();
        state = new ModuleXmlConversionHandlerState();
    }

    /**
     * Create a new instance of a XmlConversionHandler to modify XML
     * as needed when converting a snapshot or world into a module.
     *
     * @param contentDestination The destination for the processed XML content.
     * @param encodingName The name of the Character Encoding for the written XML.
     * @throws IllegalArgumentException If the supplied XmlWriter is null.
     */
    public XmlConversionHandler(XMLStreamWriter contentDestination, String encodingName) {
        this();
        setContentDestination(contentDestination, encodingName);
    }

    /**
     * Create a new instance of a XmlConversionHandler to modify XML
     * as needed when converting a snapshot or world into a module.
     *
     * @param contentDestination The destination for the processed XML content.
     * @param encodingName The name of the Character Encoding for the written XML.
     * @param xmlDocDetailsRetriever The XmlDocumentDetailsRetriever to get extra details about the document being parsed.
     * @throws IllegalArgumentException If the supplied XmlWriter is null.
     */
    public XmlConversionHandler(XMLStreamWriter contentDestination, String encodingName, XmlDocumentDetailsRetriever xmlDocDetailsRetriever) {
        this();
        setContentDestination(contentDestination, encodingName);
        setXmlDocumentDetailsRetriever(xmlDocDetailsRetriever);
    }

    /**
     * Set the destination for the content after it has been processed by this class.
     *
     * @param contentDestination The destination content handler to which the processed
     *                           content will be sent in its modified or unmodified state.
     * @param encodingName The name of the Character Encoding for the written XML.
     */
    public final void setContentDestination(XMLStreamWriter contentDestination, String encodingName) {
        if (contentDestination == null) {
            throw new IllegalArgumentException("The content destination cannot be null!");
        }
        this.contentDestination = contentDestination;
        this.encodingName = encodingName;
    }

    /**
     * Set the XmlDocumentDetailsRetriever which can be used to retrieve details of the XML document
     * being parsed.
     *
     * This method is final so that it can be safely called in a constructor.
     *
     * @param xmlDocDetailsRetriever The XmlDocumentDetailsRetriever to get extra details about the document being parsed.
     */
    public final void setXmlDocumentDetailsRetriever(XmlDocumentDetailsRetriever xmlDocDetailsRetriever) {
        this.xmlDocDetailsRetriever = xmlDocDetailsRetriever;
    }

    /**
     * Add the specified TagContentHandler to this XmlConversionHandler.
     *
     * @param handler The TagContentHandler to be added.
     * @return A set containing any TagContentHandlers which were replaced by the
     *         specified TagContentHandler for handing the same XML tags.
     */
    public Set<TagContentHandler> addHandler(TagContentHandler handler) {
        return handlers.addHandler(handler);
    }

    /**
     * Add all of the specified TagContentHandlers to handle tags which need to be converted
     * to convert XML for use with a module.
     *
     * @param handlers The TagContentHandlers to be added for use in the conversion of XML.
     * @return A set of TagContentHandlers which were replaced for handling tags with the same
     *         names (if any).
     */
    public Set<TagContentHandler> addHandlers(TagContentHandler... handlers) {
        return this.handlers.addHandlers(handlers);
    }

    /**
     * Add all of the specified TagContentHandlers to handle tags which need to be converted
     * to convert XML for use with a module.
     *
     * @param handlers The TagContentHandlers to be added for use in the conversion of XML.
     * @return A set of TagContentHandlers which were replaced for handling tags with the same
     *         names (if any).
     */
    public Set<TagContentHandler> addHandlers(Collection<TagContentHandler> handlers) {
        return this.handlers.addHandlers(handlers);
    }

    /**
     * Remove all the TagContentHandlers associated registered to handle tags for this
     * XmlConversionHandler.
     */
    public void clearHandlers() {
        handlers.clearHandlers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (handlers.isNoCurrentTagHandlerSet() || handlers.getCurrent().characters(ch, start, length, contentDestination, state)) {
            try {
                if (inCDATA) {
                    contentDestination.writeCData(new String(ch, start, length));
                }
                else {
                    contentDestination.writeCharacters(ch, start, length);
                }

            }
            catch (XMLStreamException xse) {
                throw new SAXException("Error writing characters!", xse);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (handlers.isNoCurrentTagHandlerSet() || handlers.getCurrent().ignorableWhitespace(ch, start, length, contentDestination, state)) {
            try {
                if (inCDATA) {
                    contentDestination.writeCData(new String(ch, start, length));
                }
                else {
                    contentDestination.writeCharacters(ch, start, length);
                }
            }
            catch (XMLStreamException xse) {
                throw new SAXException("Error writing characters!", xse);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        String version = null;
        boolean isStandalone = true;
        boolean standaloneCheckSuccess = false;
        if (xmlDocDetailsRetriever != null) {
            try {
                isStandalone = xmlDocDetailsRetriever.isStandalone();
                standaloneCheckSuccess = true;
            }
            catch (UnsupportedOperationException uoe)
            {
                Logger.getLogger(getClass().getName()).warning(uoe.getMessage());
                standaloneCheckSuccess = false;
            }
            try {
                version = xmlDocDetailsRetriever.getXmlVersion();
            }
            catch (UnsupportedOperationException uoe)
            {
                Logger.getLogger(getClass().getName()).warning(uoe.getMessage());
            }
        }
        try {
            if (standaloneCheckSuccess) {
                //The XMLStreamWriter provides no way to support writing the XML declaration with the standalone attribute.
                XmlUtil.writeXMLDeclaration(contentDestination, version, encodingName, isStandalone);
            }
            else {
                if (encodingName != null) {
                    if (version != null) {
                        contentDestination.writeStartDocument(encodingName, version);
                    }
                    else {
                        contentDestination.writeStartDocument(encodingName, XmlUtil.DEFAULT_XML_VERSION);
                    }
                }
                else {
                    if (version != null) {
                        contentDestination.writeStartDocument(version);
                    }
                    else {
                        contentDestination.writeStartDocument();
                    }
                }
                contentDestination.writeCharacters("\n");
            }
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Failed to open document!", xse);
        }
        handlers.documentStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        try {
            contentDestination.writeEndDocument();
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Failed to write end of document", xse);
        }
        handlers.documentEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            contentDestination.writeProcessingInstruction(target, data);
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Failed to write processing instruction!", xse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        pendingPrefixMappings.add(new PrefixMapping(prefix, uri));
        try {
            contentDestination.setPrefix(prefix, uri);
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Failed to set prefix mapping!", xse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        Logger.getLogger(getClass().getName()).warning(String.format("The entity %s was skipped!", name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        TagContentHandler handler = handlers.startTag(uri, localName, qName);
        if (handler == null || handler.startElement(uri, localName, qName, attributes, contentDestination, state)) {
            String prefix = null;
            try {
                if (uri != null && !uri.isEmpty()) {
                    if (qName != null) {
                        prefix = PrefixMapping.getPrefix(qName);
                        if (prefix != null) {
                            contentDestination.writeStartElement(prefix, localName, uri);
                        }
                        else {
                            contentDestination.writeStartElement(qName);
                        }
                    }
                    else {
                       contentDestination.writeStartElement(uri, localName);
                    }
                }
                else {
                    if (qName != null) {
                        contentDestination.writeStartElement(qName);
                    }
                    else {
                        contentDestination.writeStartElement(localName);
                    }
                }
                if (!pendingPrefixMappings.isEmpty()) {
                    for (PrefixMapping mapping : pendingPrefixMappings) {
                        contentDestination.writeNamespace(mapping.prefix, mapping.uri);
                    }
                    pendingPrefixMappings.clear();
                }
                if (attributes != null) {
                    final int attributeCount = attributes.getLength();
                    for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
                        qName = attributes.getQName(attributeIndex);
                        localName = attributes.getLocalName(attributeIndex);
                        uri = attributes.getURI(attributeIndex);
                        if (qName != null) {
                            prefix = PrefixMapping.getPrefix(qName);
                            if (prefix != null) {
                                if (uri != null && !uri.isEmpty()) {
                                    contentDestination.writeAttribute(prefix, uri, localName, attributes.getValue(attributeIndex));
                                }
                                else {
                                    contentDestination.writeAttribute(localName, attributes.getValue(attributeIndex));
                                }
                            }
                            else {
                                if (uri != null && !uri.isEmpty()) {
                                    contentDestination.writeAttribute(uri, localName, attributes.getValue(attributeIndex));
                                }
                                else {
                                    contentDestination.writeAttribute(localName, attributes.getValue(attributeIndex));
                                }
                            }
                        }
                        else {
                            contentDestination.writeAttribute(localName, attributes.getValue(attributeIndex));
                        }
                    }
                }
            }
            catch (XMLStreamException xse) {
                throw new SAXException("Failed to write opening tag!", xse);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        TagContentHandler handler = handlers.endTag(uri, localName, qName);
        if (handler == null || handler.endElement(uri, localName, qName, contentDestination, state)) {
            try {
                contentDestination.writeEndElement();
                contentDestination.flush();
            }
            catch (XMLStreamException xse) {
                throw new SAXException("An error occurred while trying to write the end element", xse);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            contentDestination.writeComment(new String(ch, start, length));
        }
        catch (XMLStreamException xse) {
            throw new SAXException("Error writing comment!", xse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startCDATA() throws SAXException {
        inCDATA = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endCDATA() throws SAXException {
        inCDATA = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        try {
            contentDestination.writeDTD(XmlUtil.toDTDString(name, publicId, systemId));
        }
        catch (XMLStreamException xse) {
            throw new SAXException("The doctype definition failed to be written!", xse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDTD() throws SAXException {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEntity(String name) throws SAXException {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void internalEntityDecl(String name, String value) throws SAXException {
        super.internalEntityDecl(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endEntity(String name) throws SAXException {
        
    }

    private class ModuleXmlConversionHandlerState implements XmlStateAttributes {

        /**
         * {@inheritDoc}
         */
        @Override
        public List<PrefixMapping> getPendingPrefixMappings() {
            return pendingPrefixMappings;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clearPendingPrefixMappings() {
            pendingPrefixMappings.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getDepth() {
            return handlers.getDepth();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isInCDATA() {
            return inCDATA;
        }

    }

    /**
     * Utility class for encapsulating the functionality relating to tag handlers.
     */
    protected static class TagHandlerGroup {

        /**
         * Utility static method to get a TagContentHandler out of a map of TagContentHandlers stored by tag name.
         * The qualified tag name is tried first and then the local name.
         *
         * @param handlers The handlers from which to get a TagContentHandler for the specified TagName (if any).
         * @param uri The URI of the XML tag.
         * @param localName The local name of the XML tag.
         * @param qName The fully qualified name of the XML tag.
         * @return A TagContentHandler found in the specified map to handle a tag with the specified name or null
         *         if no TagContentHandler can be found for the specified tag.
         */
        private static TagContentHandler getFrom(Map<String, TagContentHandler> handlers, String uri, String localName, String qName) {
            if (handlers != null && !handlers.isEmpty()) {
                TagContentHandler handler = handlers.get(qName);
                if (handler == null) {
                    handler = handlers.get(localName);
                }
                return handler;
            }
            return null;
        }

        private Map<String, TagContentHandler> generalHandlers;
        private Map<String, Map<String, TagContentHandler>> rootTagSpecificHandlers;
        private Map<String, TagContentHandler> currentRootHandlers;
        private Deque<TagContentHandler> currentTagHandler;
        private int depth;

        public TagHandlerGroup() {
            generalHandlers = new HashMap<String, TagContentHandler>();
            rootTagSpecificHandlers = new HashMap<String, Map<String, TagContentHandler>>();
            currentTagHandler = new ArrayDeque<TagContentHandler>(2);
        }

        /**
         * Method to be called when the start of the document is encountered.
         * This resents the internal state of the TagHandlerGroup.
         */
        public void documentStart() {
            currentRootHandlers = null;
            currentTagHandler.clear();
            depth = 0;
        }

        /**
         * Method to be called when the end of the document is encountered.
         * This resents the internal state of the TagHandlerGroup.
         */
        public void documentEnd() {
            currentRootHandlers = null;
            currentTagHandler.clear();
            depth = 0;
        }

        /**
         * Method to be called when a start tag is encountered in an XML document.
         * This method returns the associated TagContentHandler (if any).
         *
         * @param uri The URI of the tag which has been opened (possible future use).
         * @param localName The local name of the XML tag.
         * @param qName The qualified name of the XML tag.
         * @return The TagContentHandler which is to handle this start tag (if any).
         */
        public TagContentHandler startTag(String uri, String localName, String qName) {
            if (depth == 0) {
                currentRootHandlers = rootTagSpecificHandlers.get(qName);
                if (currentRootHandlers == null) {
                    currentRootHandlers = rootTagSpecificHandlers.get(localName);
                }
            }
            depth++;
            TagContentHandler handler = getFrom(currentRootHandlers, uri, localName, qName);
            if (handler == null) {
                handler = getFrom(generalHandlers, uri, localName, qName);
            }
            if (handler != null) {
                TagContentHandler currentHandler = currentTagHandler.peekFirst();
                if (currentHandler != handler) {
                    currentTagHandler.push(handler);
                    if (currentHandler instanceof ActiveStateListeningTagContentHandler) {
                        ((ActiveStateListeningTagContentHandler) currentHandler).switchedTo(handler);
                    }
                }
            }
            else {
                handler = currentTagHandler.peekFirst();
            }
            return handler;
        }

        /**
         * Method to be called when a start tag is encountered in an XML document.
         *
         * @param uri The URI of the tag which has been closed (future use).
         * @param localName The local XML tag name of the tag which was closed.
         * @param qName The fully qualified XML tag name of the tag which was closed.
         * @return The tag content handler associated with this tag (if any).
         */
        public TagContentHandler endTag(String uri, String localName, String qName) {
            TagContentHandler handler = getFrom(currentRootHandlers, uri, localName, qName);
            if (handler == null) {
                handler = getFrom(generalHandlers, uri, localName, qName);
            }
            if (handler != null) {
                if (currentTagHandler.peekFirst() == handler) {
                    handler = currentTagHandler.pop();
                    TagContentHandler currentHandler = currentTagHandler.peekFirst();
                    if (currentHandler instanceof ActiveStateListeningTagContentHandler) {
                        ((ActiveStateListeningTagContentHandler) currentHandler).switchedBackFrom(handler);
                    }
                }
            }
            else {
                handler = currentTagHandler.peekFirst();
            }
            depth--;
            if (depth == 0) {
                currentRootHandlers = null;
            }
            return handler;
        }

        /**
         * Whether a TagContentHandler is no currently set TagContentHandler.
         *
         * @return True if there is no current TagContentHandler is set.
         */
        public boolean isNoCurrentTagHandlerSet() {
            return currentTagHandler.isEmpty();
        }

        /**
         * Get the current TagContentHandler (if any).
         *
         * @return The current TagContentHandler or null if none is currently set.
         */
        public TagContentHandler getCurrent() {
            return currentTagHandler.peekFirst();
        }

        /**
         * Add the specified TagContentHandler to this TagHandlerGroup.
         *
         * @param handler The TagContentHandler to be added to the group.
         * @param replacedHandlers A destination set into which will be put any
         *                         TagContentHandlers which were replaced for handling
         *                         the same tags as the added TagContentHandler.
         */
        public void addHandler(TagContentHandler handler, Set<TagContentHandler> replacedHandlers) {
            if (handler != null) {
                TagContentHandler currentReplacedHandler = null;
                String[] rootTags = handler.getCompatibleRootTags();
                String[] tags = null;
                if (rootTags != null && rootTags.length > 0) {
                    Map<String, TagContentHandler> currentForRoot = null;
                    for (String rootTag : rootTags) {
                        currentForRoot = rootTagSpecificHandlers.get(rootTag);
                        if (currentForRoot == null) {
                            currentForRoot = new HashMap<String, TagContentHandler>();
                            rootTagSpecificHandlers.put(rootTag, currentForRoot);
                        }
                        tags = handler.getHandledTags();
                        if (tags != null && tags.length > 0) {
                            for (String tag : tags) {
                                currentReplacedHandler = currentForRoot.put(tag, handler);
                                if (currentReplacedHandler != null && replacedHandlers != null) {
                                    replacedHandlers.add(currentReplacedHandler);
                                }
                            }
                        }
                    }
                }
                else {
                    tags = handler.getHandledTags();
                    if (tags != null && tags.length > 0) {
                        for (String tag : tags) {
                            currentReplacedHandler = generalHandlers.put(tag, handler);
                            if (currentReplacedHandler != null && replacedHandlers != null) {
                                replacedHandlers.add(currentReplacedHandler);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Add the specified TagContentHandler to this TagHandlerGroup.
         *
         * @param handler The TagContentHandler to be added to the group.
         * @return A set of TagContentHandlers which were replaced for handling
         *         the same tags as the added TagContentHandler.
         */
        public Set<TagContentHandler> addHandler(TagContentHandler handler) {
            Set<TagContentHandler> replacedHandlers = new HashSet<TagContentHandler>();
            addHandler(handler, replacedHandlers);
            return replacedHandlers;
        }

        /**
         * Add the specified TagContentHandler to this TagHandlerGroup.
         *
         * @param handlers A variable number of TagContentHandlers to be added to the group.
         * @return A set of TagContentHandlers which were replaced for handling
         *         the same tags as the added TagContentHandlers.
         */
        public Set<TagContentHandler> addHandlers(TagContentHandler... handlers) {
            Set<TagContentHandler> replacedHandlers = new HashSet<TagContentHandler>();
            if (handlers != null && handlers.length > 0) {
                for (TagContentHandler handler : handlers) {
                    addHandler(handler, replacedHandlers);
                }
            }
            return replacedHandlers;
        }

        /**
         * Add the specified TagContentHandler to this TagHandlerGroup.
         *
         * @param handlers A collection of TagContentHandlers to be added to the group.
         * @return A set of TagContentHandlers which were replaced for handling
         *         the same tags as the added TagContentHandlers.
         */
        public Set<TagContentHandler> addHandlers(Collection<TagContentHandler> handlers) {
            Set<TagContentHandler> replacedHandlers = new HashSet<TagContentHandler>();
            if (handlers != null && !handlers.isEmpty()) {
                for (TagContentHandler handler : handlers) {
                    addHandler(handler, replacedHandlers);
                }
            }
            return replacedHandlers;
        }

        /**
         * Clear all the TagContentHandlers in this group.
         */
        public void clearHandlers() {
            if (currentRootHandlers != null) {
                currentRootHandlers.clear();
                currentRootHandlers = null;
            }
            generalHandlers.clear();
            currentTagHandler.clear();
        }

        /**
         * Get the current XML tag depth.
         *
         * @return The number of tags deep the current XML parsing is.
         */
        public int getDepth() {
            return depth;
        }
    }
}
