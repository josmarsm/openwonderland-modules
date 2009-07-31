/*
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

package org.jdesktop.wonderland.modules.annotations.client.display;

import org.jdesktop.mtgame.Entity;
import java.awt.event.MouseEvent;
import java.util.List;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;




/**
 * Based off of WindowContextEvent. Used with the global context menu system
 * to provide context menus for annotations.
 * @author mabonner
 */
public class AnnotationContextEvent extends ContextEvent {

        // Default constructor -- For clone only.
        private AnnotationContextEvent () {
            super();
        }

        public AnnotationContextEvent (List<Entity> entities, MouseEvent mouseEvent) {
          super(entities, mouseEvent);
        }
//
////        public Window2D getWindow () {
////            return window;
////        }
//
        /**
         * {@inheritDoc}
         * <br>
         * If event is null, a new event of this class is created and returned.
         */
        @Override
        public Event clone (Event event) {
            if (event == null) {
                event = new AnnotationContextEvent();
            }
//            ((WindowContextMenuEvent)event).window = window;
            return super.clone(event);
        }
    }
