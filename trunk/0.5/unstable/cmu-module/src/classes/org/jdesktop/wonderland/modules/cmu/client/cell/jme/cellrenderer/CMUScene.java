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

package org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer;

import com.jme.scene.Node;
import edu.cmu.cs.dennisc.scenegraph.Component;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import org.alice.apis.moveandturn.Scene;
import org.alice.apis.moveandturn.Transformable;

/**
 * Wraps a CMU Scene object to extract JME equivalents of the nodes.
 *
 * @author kevin
 */
public class CMUScene extends Node {
    private Scene sc;

    /**
     * Get the wrapped scene.
     * @return The wrapped scene
     */
    public Scene getScene() {
        return sc;
    }

    /**
     * Set the wrapped scene, and parse it to extract the JME nodes.
     *
     * @param sc The scene to wrap
     */
    public void setScene(Scene sc) {
        this.sc = sc;
        this.fillJMENodes();
    }

    /**
     * Parse the current scene and (re)populate the child JME nodes.
     */
    protected void fillJMENodes() {
        this.detachAllChildren();
        processNode(this.sc.getSGComposite());
    }

    /**
     * Recursively parse scene tree components, ending at the visible leaves.
     * @param c The component to parse
     */
    private void processNode(Component c) {
        assert c != null;

        /* Process this node. */

        // Check to see if it's a visual element, and if so, parse it and add it to the collection.
        if (Visual.class.isAssignableFrom(c.getClass())) {
            Node n = new CMUVisualNode((Visual)c);
            // System.out.println(n);
            this.attachChild(n);
        }

        /* Process this node's children. */
        if (Composite.class.isAssignableFrom(c.getClass())) {
            for (Component child : ((Composite) c).accessComponents()) {
                processNode(child);
            }
        }
    }
/*
    @Override
    public void addComponent(Transformable c) {
    }
 * */
}
