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
package org.jdesktop.wonderland.modules.timeline.provider.spi;

/**
 * The interface for implementing a timeline provider.  A TimelineProvider is
 * responsible for reading actual data from the internet and formatting
 * it for use on the timeline.
 * <p>
 * A TimelineProvider must provider a public, no-argument constructor.  When
 * a query is added to the system, a new instance of the provider will be
 * instantiated.  Immediately after instantiation, the setup method will be
 * called with the context for this provider.  The context includes the
 * query and associated configuration, as well as methods to send results
 * back to the client.
 * 
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineProvider {
    /**
     * Initialize this provider
     * @param context the setup information for the provider
     */
    public void initialize(TimelineProviderContext context);

    /**
     * Shut down this provider and release all associated resources.
     */
    public void shutdown();
}
