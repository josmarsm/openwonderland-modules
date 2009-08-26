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
package org.jdesktop.wonderland.modules.cmu.client.events;

/**
 *
 * @author kevin
 */
public class SceneLoadedChangeEvent {

    private static final float DEFAULT_MIN_LOAD = 0.0f;
    private static final float DEFAULT_MAX_LOAD = 1.0f;

    private float loadProgress;
    private float minLoad;
    private float maxLoad;

    /**
     * Standard constructor.
     * @param loadProgress Between 0 and 1; the fraction of a scene which
     * has been loaded
     */
    public SceneLoadedChangeEvent(float loadProgress) {
        this(loadProgress, DEFAULT_MIN_LOAD, DEFAULT_MAX_LOAD);
    }

    public SceneLoadedChangeEvent(float loadProgress, float minLoad, float maxLoad) {
        this.loadProgress = loadProgress;
        this.minLoad = minLoad;
        this.maxLoad = maxLoad;
    }

    public float getLoadProgress() {
        return loadProgress;
    }

    public float getMaxLoad() {
        return maxLoad;
    }

    public float getMinLoad() {
        return minLoad;
    }
}
