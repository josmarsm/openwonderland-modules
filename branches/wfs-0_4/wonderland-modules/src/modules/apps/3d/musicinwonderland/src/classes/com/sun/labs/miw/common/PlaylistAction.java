/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.common;

/**
 *
 * @author jkaplan
 */
public enum PlaylistAction {
    /**
     * the playlist action to take:<ul>
     * <li><code>NEW</code> replace the contents of the current playlist with
     * this playlist
     * <li><code>APPEND_FRONT</code> add the given songs to the front of the current playlist
     ** <li><code>APPEND_BACK</code> add the given songs to the back of the current playlist
     *</ul>
     */
    
    NEW, APPEND_FRONT, APPEND_BACK;
}
