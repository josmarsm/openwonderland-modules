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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 * Message containing data to load an entire scene; contains visual messages,
 * as well a root address to access visuals in the content repository.  Visuals
 * (in the form of VisualAttributes objects) should be uploaded to the
 * repository before this message is sent.
 * @author kevin
 */
public class SceneMessage extends CMUClientMessage {

    private final Collection<VisualMessage> visuals = new Vector<VisualMessage>();
    private String repoRoot;

    /**
     * Standard constructor.
     * @param visuals The visual elements in this scene
     * @param repoRoot The repository root to which the elements have been uploaded
     */
    public SceneMessage(Collection<VisualMessage> visuals, String repoRoot) {
        if (visuals != null) {
            synchronized (this.visuals) {
                for (VisualMessage visual : visuals) {
                    addVisual(visual);
                }
            }
        }
        setRepoRoot(repoRoot);
    }

    /**
     * Add a visual to the scene represented by this message.
     * @param visual Message representing the visual to add
     */
    public void addVisual(VisualMessage visual) {
        synchronized (visuals) {
            visuals.add(visual);
        }
    }

    /**
     * Get the collection of visuals in this scene.
     * @return All the visuals in this scene as VisualMessage instances
     */
    public Collection<VisualMessage> getVisuals() {
        synchronized (visuals) {
            return Collections.unmodifiableCollection(visuals);
        }
    }

    /**
     * Get the repository root to which visuals have been uploaded.
     * @return Repository root as a URI prefix
     */
    public String getRepoRoot() {
        return repoRoot;
    }

    /**
     * Set the repository root to which visuals have been uploaded.
     * @param repoRoot Repository root as a URI prefix
     */
    public void setRepoRoot(String repoRoot) {
        this.repoRoot = repoRoot;
    }

}