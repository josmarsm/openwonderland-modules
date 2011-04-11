package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface is used when a StyleType is to be de-serialized from its corresponding
 * unique StyleType id integer value. The use of this interface supports other modules
 * which may extends this module with new StyleType to use their own StyleTypeResolver
 * which resolves the StyleTypes. This interface is generic to allow it to be used
 * for NodeStyleTypes and SegmentStyleTypes (and possibly others as needed in the future).
 *
 * @author Carl Jokl
 */
public interface StyleTypeResolver<T extends StyleType> {

    /**
     * Get the StyleType group name for which this StyleTypeResolver is used to resolve StyleTypes.
     * 
     * @return The name of the group of StyleTypes for which this StyleTypeResolver is intended to resolve. 
     */
    public String getGroupName();

    /**
     * Resolve the StyleType which corresponds to the specified unique StyleType id integer value.
     *
     * @param styleTypeId The integer id of the StyleType to be resolved.
     * @return The StyleType which corresponds to the specified id or null if the StyleType was not found.
     */
    public T resolveStyleType(int styleTypeId);
}
