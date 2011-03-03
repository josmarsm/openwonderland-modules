package org.jdesktop.wonderland.modules.world.common.xml.tags;

/**
 * This is an extension to the basic TagContentHandler to provide methods
 * for notifying the TagContentHandler when it is switched in and out of
 * active state by a TagConentHandler which is handling inner tags of
 * the tags which the TagContentHandler is handling.
 *
 * @author Carl Jokl
 */
public interface ActiveStateListeningTagContentHandler extends TagContentHandler {

    /**
     * This method is used to inform a TagContentHandler when XML content handling has switched to another
     * TagContentHandler. This normally happens if while inside an XML tag which is handled by this
     * TagContentHandler another XML tag is encountered for which a different TagContentHandler is registered
     * to handle. When that happens this method is called and the argument is the TagContentHandler which
     * has taken over handing XML tag content.
     *
     * The Tag content handler is not required to do anything as a result of being notified but this
     * notification is given in case the logic of the TagContentHandler requires knowing when XML
     * content handling switches to another TagContentHandler.
     *
     * @param handler The TagContentHandler which has taken over the loading of xml tag content.
     */
    public void switchedTo(TagContentHandler handler);

    /**
     * This method is used to inform a TagContentHandler when XML content handling has switched back to this
     * TagContentHandler from another TagContentHandler. This normally happens if while inside an XML tag which
     * is handled by this TagContentHandler another XML tag is encountered for which a different TagContentHandler
     * is registered to handle and then the end XML tag for which that TagContentHandler is registered to handle is
     * encountered switching handling back to this TagContentHandler.
     * When that happens this method is called and the argument is the TagContentHandler which has switch handing
     * back to this TagContentHandler to handle XML tag content.
     *
     * The Tag content handler is not required to do anything as a result of being notified but this
     * notification is given in case the logic of the TagContentHandler requires knowing when XML
     * content handling switches back from another TagContentHandler.
     *
     * @param handler The TagContentHandler which has has finished handling XML content content handling is being
     *                switched from.
     */
    public void switchedBackFrom(TagContentHandler handler);
}
