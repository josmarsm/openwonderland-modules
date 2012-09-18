package org.jdesktop.wonderland.modules.world.common.xml;

/**
 * Simple value holder class for prefix mappings.
 */
public class PrefixMapping {

    /**
     * The XML namespace prefix
     */
    public static final String XML_NAMESPACE_PREFIX = "xmlns:";

    /**
     * Get the prefix of a fully qualified tag name.
     *
     * @param qName The fully qualified tag name for which to get a prefix.
     * @return The prefix of the fully qualified tag name or null if the tag had no prefix.
     */
    public static String getPrefix(String qName) {
        if (qName != null) {
            int colonIndex = qName.indexOf(':');
            if (colonIndex > 0) {
                return qName.substring(0, colonIndex);
            }
        }
        return null;
    }

    public final String prefix;
    public final String uri;

    public PrefixMapping(final String prefix, final String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    /**
     * Combine the prefix with the standard XML name-space prefix so that
     * it can be used as an attribute name.
     *
     * This can then be used as the attribute name in code used to write attributes.
     *
     * @return The the prefix definition as an attribute name string.
     */
    public String getAsAttributeName() {
        return XML_NAMESPACE_PREFIX.concat(prefix);
    }
}
