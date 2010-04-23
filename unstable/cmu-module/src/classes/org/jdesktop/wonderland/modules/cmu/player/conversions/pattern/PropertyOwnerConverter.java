/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.cmu.player.conversions.pattern;

import edu.cmu.cs.dennisc.alice.ast.AbstractType;
import edu.cmu.cs.dennisc.pattern.DefaultInstancePropertyOwner;
import edu.cmu.cs.dennisc.property.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Abstract class to wrap a CMU property owner.  Primarily provides debug
 * functionality, to keep track of properties which have or have not been
 * accounted for.
 * @author kevin
 */
public abstract class PropertyOwnerConverter {
    /**
     * Standard constructor.
     * @param propertyOwner The property owner to convert
     */
    public PropertyOwnerConverter(DefaultInstancePropertyOwner propertyOwner) {
        for (Property p : propertyOwner.getProperties()) {
            if (!isPropertyExpected(p)) {
                Logger.getLogger(this.getClass().getName()).warning("Unrecognized property in " + propertyOwner + ": " + p);
            }
        }
        Collection<String> missingPropertyNames = getMissingProperties(propertyOwner.getProperties());
        for (String name : missingPropertyNames) {
            Logger.getLogger(this.getClass().getName()).warning("Expected property \"" + name + "\" not present in " + propertyOwner);
        }
    }

    /**
     * Find out whether the given property is expected by this converter.
     * @param p The property to check
     * @return True if the property is recognized, false otherwise
     */
    private boolean isPropertyExpected(Property p) {
        Collection<String> expectedPropertyNames = getExpectedPropertyNames();

        if (expectedPropertyNames == null) {
            return true;
        } else {
            return expectedPropertyNames.contains(p.getName());
        }
    }

    /**
     * Get the property names which don't appear in the given collection
     * of properties, but which are expected by this converter.
     * @param properties The collection of properties to compare against the recognized ones
     * @return The property names which are expected, but don't appear in the given collection
     */
    @SuppressWarnings("unchecked")
    private Collection<String> getMissingProperties(Iterable<Property<?>> properties) {

        Collection<String> expectedPropertyNames = getExpectedPropertyNames();
        Collection<String> expectedPropertyNamesCopy = null;
        if (expectedPropertyNames != null) {
            expectedPropertyNamesCopy = new ArrayList<String>(getExpectedPropertyNames());
        }

        if (expectedPropertyNamesCopy != null && properties != null) {
            Collection<String> existingPropertyNames = new ArrayList<String>();
            for (Property p : properties) {
                existingPropertyNames.add(p.getName());
            }
            expectedPropertyNamesCopy.removeAll(existingPropertyNames);
            return expectedPropertyNamesCopy;
        } else {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * To get debug information about properties which are unhandled in
     * conversion, subclasses can override this method to provide all and only
     * all of the names of properties which are expected from a property owner
     * of the converted type.  If the method is not overridden, no debug
     * information will be supplied.
     * @return All and only all of the names of properties which the wrapped
     * property owner is expected to possess
     */
    protected Collection<String> getExpectedPropertyNames() {
        return null;
    }
}
