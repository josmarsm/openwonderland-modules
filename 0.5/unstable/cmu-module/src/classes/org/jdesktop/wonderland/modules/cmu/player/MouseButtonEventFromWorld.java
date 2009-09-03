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
package org.jdesktop.wonderland.modules.cmu.player;

import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import org.alice.apis.moveandturn.Model;
import org.alice.apis.moveandturn.event.MouseButtonEvent;

/**
 * A CMU MouseButtonEvent which the model at mouse location is already known
 * based on in-world calculations.  Avoids additional computation.
 * @author kevin
 */
public class MouseButtonEventFromWorld extends MouseButtonEvent {

    private final Model model;
    private static final JPanel DUMMY_PANEL = new JPanel();

    /**
     * Standard constructor.
     * @param model The Model which received the click
     */
    public MouseButtonEventFromWorld(Model model) {
        super(new MouseEvent(DUMMY_PANEL, 0, 0, 0, 0, 0, 0, false), model.getScene());
        this.model = model;
    }

    /**
     * Get the node which was clicked.
     * @return Node which was clicked
     */
    @Override
    public Model getModelAtMouseLocation() {
        return model;
    }

    /**
     * Get the node which was clicked.
     * @return Node which was clicked
     */
    @Override
    public Model getPartAtMouseLocation() {
        return model;
    }
}
