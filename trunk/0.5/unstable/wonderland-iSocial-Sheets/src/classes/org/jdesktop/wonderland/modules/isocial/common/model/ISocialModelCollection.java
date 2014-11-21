/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.common.model;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * A collection of ISocialModel data
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@ISocialModel
@XmlRootElement(name="collection")
public class ISocialModelCollection<T extends ISocialModelBase> {
    private final Set<T> items = new TreeSet<T>();

    /**
     * Default, no argument constructor for JAXB
     */
    protected ISocialModelCollection() {
    }

    /**
     * Constructor for a collection
     * @param items the items to add
     */
    public ISocialModelCollection(Collection<? extends T> items) {
        this.items.addAll(items);
    }

    @XmlElementRef
    public Set<T> getItems() {
        return items;
    }
}
