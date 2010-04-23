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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;

/**
 * Event used to represent a change in the amount of a scene which
 * has been loaded.
 * @author kevin
 */
public class SceneLoadedChangeEvent extends CMUChangeEvent {

    /**
     * Default load value to represent a completely unloaded scene.
     */
    public static final float DEFAULT_MIN_LOAD = 0.0f;
    /**
     * Default load value to represent a completely loaded scene.
     */
    public static final float DEFAULT_MAX_LOAD = 1.0f;
    private float loadProgress;
    private float minLoad;
    private float maxLoad;

    /**
     * Standard constructor.
     * @param cell The cell whose scene loaded value has changed
     * @param loadProgress Between 0 and 1; the fraction of a scene which
     * has been loaded
     */
    public SceneLoadedChangeEvent(CMUCell cell, float loadProgress) {
        this(cell, loadProgress, DEFAULT_MIN_LOAD, DEFAULT_MAX_LOAD);
    }

    /**
     * Specialized constructor to accomodate non-default min/max loads.
     * @param cell The cell whose scene loaded value has changed
     * @param loadProgress Between minLoad and maxLoad; the fraction of a scene
     * which has been loaded
     * @param minLoad The load value representing a minimally loaded scene
     * @param maxLoad The load value representing a maximally loaded scene
     */
    public SceneLoadedChangeEvent(CMUCell cell, float loadProgress, float minLoad, float maxLoad) {
        super(cell);
        setLoadProgress(loadProgress);
        setMinLoad(minLoad);
        setMaxLoad(maxLoad);
    }

    /**
     * Get the load progress represented by this event, with respect to
     * the event's minimum and maximum values.
     * @return Load progress for this event
     */
    public float getLoadProgress() {
        return loadProgress;
    }

    /**
     * Set the load progress for this event.
     * @param loadProgress Load progress for this event
     */
    public void setLoadProgress(float loadProgress) {
        this.loadProgress = loadProgress;
    }

    /**
     * Get the maximum load progress value represented by this event, i.e.
     * the load progress value at which the scene has been completely loaded.
     * @return Maximum load progress for this event
     */
    public float getMaxLoad() {
        return maxLoad;
    }

    /**
     * Set the maximum load progress for this event.
     * @param maxLoad Maximum load progress for this event
     */
    public void setMaxLoad(float maxLoad) {
        this.maxLoad = maxLoad;
    }

    /**
     * Get the minimum load progress value represented by this event, i.e.
     * the load progress value at which the scene is completely unloaded.
     * @return Minimum load progress for this event
     */
    public float getMinLoad() {
        return minLoad;
    }

    /**
     * Set the minimum load progress for this event.
     * @param minLoad Minimum load progress for this event
     */
    public void setMinLoad(float minLoad) {
        this.minLoad = minLoad;
    }
}
