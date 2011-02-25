package org.jdesktop.wonderland.modules.world.common.xml.tags.annotation;

import org.jdesktop.wonderland.modules.world.common.xml.tags.annotation.HandlerType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is intended to mark TagContentHandlers (i.e. is a Marker annotation)
 * which are intended to be used with converter applications such as the snapshot to module conversion.
 * This allows custom modules which require their XML data stored in a snapshot to be processed when being
 * converted into a module without the conversion functionality having to know about every type of module
 * XML structure in advance.
 *
 * @author Carl Jokl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TagHandlerType {
    /**
     * The type of the TagContentHandler which helps identify which ones can be used to export XML into a Module.
     *
     * @return The HandlerType which represents what type of TagContentHandler the marked TagContentHandler is and
     *         so provide a clue as to what it was intended to be used for. This can help to find custom TagContentHandlers
     *         which should be used to export XML from custom modules.
     */
    HandlerType type();
}
