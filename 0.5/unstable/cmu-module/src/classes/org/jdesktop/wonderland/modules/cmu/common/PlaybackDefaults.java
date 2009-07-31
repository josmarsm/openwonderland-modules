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
package org.jdesktop.wonderland.modules.cmu.common;

/**
 * Defines default values for scene playback speed.
 * @author kevin
 */
public final class PlaybackDefaults {

    public static final float DEFAULT_PLAYBACK_SPEED = 1.0f;
    public static final float PAUSE_SPEED = 0.0f;
    public static final float DEFAULT_START_SPEED = PAUSE_SPEED;

    /**
     * Class should never be instantiated.
     */
    private PlaybackDefaults() {
    }
}
