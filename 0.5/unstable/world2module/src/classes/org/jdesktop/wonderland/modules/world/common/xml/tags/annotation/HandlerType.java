package org.jdesktop.wonderland.modules.world.common.xml.tags.annotation;

/**
 * This enumeration is intended for use in combination with the
 * @TagHandlerType annotation to specify TagContenthHandlers which
 * are intended for use with exporting snapshots vs those which are
 * used for other purposes.
 *
 * @author Carl Jokl
 */
public enum HandlerType {

    /**
     * The TagContentHandler is intended for other custom of modules specific use.
     */
    OTHER,

    /**
     * This type of handler is intended to be used in converting XML for use for
     * sub-snapshot export.
     */
    SUB_SNAPSHOT_EXPORT,

    /**
     * This type identifies a TagConentHandler which is intended to be used for
     * exporting a world or snapshot to convert it into a module.
     */
    WORLD_TO_MODULE_CONVERSION;
}
