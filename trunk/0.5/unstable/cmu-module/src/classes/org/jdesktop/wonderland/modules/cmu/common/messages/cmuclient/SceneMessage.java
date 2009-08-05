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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 *
 * @author kevin
 */
public class SceneMessage implements Serializable {

    final Vector<VisualMessage> visuals = new Vector<VisualMessage>();

    public SceneMessage(Collection<VisualMessage> visuals) {
        if (visuals != null) {
            synchronized (this.visuals) {
                for (VisualMessage visual : visuals) {
                    addVisual(visual);
                }
            }
        }
    }

    private void addVisual(VisualMessage visual) {
        synchronized (visuals) {
            visuals.add(visual);
        }
    }

    public Collection<VisualMessage> getVisuals() {
        synchronized (visuals) {
            return Collections.unmodifiableCollection(visuals);
        }
    }
}