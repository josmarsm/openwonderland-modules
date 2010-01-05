/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.layout.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.layout.api.common.annotation.LayoutFactory;
import org.jdesktop.wonderland.modules.layout.api.common.spi.LayoutFactorySPI;

/**
 * A registry of all layouts registered on the system. Layouts register them-
 * selves by implementing the <code>LayoutFactorySPI</code> interface and be
 * annotated with <code>LayoutFactory</code>
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class LayoutRegistry implements PrimaryServerListener {

    // A set of all registered layout factories
    private Set<LayoutFactorySPI> layoutFactorySet =
            new HashSet<LayoutFactorySPI>();

    /** Default constructor */
    public LayoutRegistry() {
        LoginManager.addPrimaryServerListener(this);
    }
    
    /**
     * Singleton to hold instance of this class.
     */
    private static class SingletonHolder {
        private final static LayoutRegistry INSTANCE = new LayoutRegistry();
    }
    
    /**
     * Returns a single instance of this class.
     * @return single instance of this class.
     */
    public static final LayoutRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Returns a set of all cell factories. If no factories are registered,
     * returns an empty set.
     * @return a set of registered cell factories
     */
    public synchronized Set<LayoutFactorySPI> getLayoutFactories() {
        return new HashSet(layoutFactorySet);
    }

    /**
     * Notification that the primary server has changed. Update our maps
     * accordingly.
     *
     * @param server the new primary server (may be null)
     */
    public synchronized void primaryServer(ServerSessionManager manager) {
        // remove any existing entries
        layoutFactorySet.clear();

        // find new entries
        if (manager != null) {
            Iterator<LayoutFactorySPI> it = manager.getClassloader().getAll(
                    LayoutFactory.class, LayoutFactorySPI.class);

            while (it.hasNext() == true) {
                layoutFactorySet.add(it.next());
            }
        }
    }
}
