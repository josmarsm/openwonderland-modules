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
package org.jdesktop.wonderland.modules.mediaboard.client;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.w3c.dom.Element;

/**
 *
 * @author jbarratt
 */
public class WhiteboardSelection {

    private Element selectedElement = null;
    private Shape selectedShape = null;
    private List<Shape> selectionHandles = new LinkedList<Shape>();
    private int selectionHandleSize = 10;

    public WhiteboardSelection(Element element, Rectangle2D elementBounds) {
        selectedElement = element;

        selectedShape = WhiteboardUtils.getElementShape(selectedElement, elementBounds);

        if (selectedShape instanceof Line2D.Double) {
            Line2D line = (Line2D) selectedShape;
            addSelectionHandle(new Point((int) line.getX1(), (int) line.getY1()));
            addSelectionHandle(new Point((int) line.getX2(), (int) line.getY2()));
        } else if (selectedShape instanceof Rectangle2D.Double) {
            Rectangle2D rectangle = selectedShape.getBounds2D();
//            selectionHandles.add(rectangle);
            int x, y;
            // Point 1 - top left
            x = (int) rectangle.getX();
            y = (int) rectangle.getY();
            addSelectionHandle(new Point(x, y));
            // Point 2 - top right
            x = (int) (rectangle.getX() + rectangle.getWidth());
            y = (int) rectangle.getY();
            addSelectionHandle(new Point(x, y));
            // Point 3 - bottom left
            x = (int) rectangle.getX();
            y = (int) (rectangle.getY() + rectangle.getHeight());
            addSelectionHandle(new Point(x, y));
            // Point 4 - bottom right
            x = (int) (rectangle.getX() + rectangle.getWidth());
            y = (int) (rectangle.getY() + rectangle.getHeight());
            addSelectionHandle(new Point(x, y));
        } else if (selectedShape instanceof Ellipse2D.Double) {
            Rectangle2D rectangle = selectedShape.getBounds2D();
            int x, y;
            // Point 1 - top
            x = (int) (rectangle.getX() + rectangle.getWidth() / 2);
            y = (int) rectangle.getY();
            addSelectionHandle(new Point(x, y));
            // Point 2 - right
            x = (int) (rectangle.getX() + rectangle.getWidth());
            y = (int) (rectangle.getY() + rectangle.getHeight() / 2);
            addSelectionHandle(new Point(x, y));
            // Point 3 - bottom
            x = (int) (rectangle.getX() + rectangle.getWidth() / 2);
            y = (int) (rectangle.getY() + rectangle.getHeight());
            addSelectionHandle(new Point(x, y));
            // Point 4 - left
            x = (int) rectangle.getX();
            y = (int) (rectangle.getY() + rectangle.getHeight() / 2);
            addSelectionHandle(new Point(x, y));
        }
    }

    public Element getSelectedElement() {
        return selectedElement;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public List<Shape> getSelectionPoints() {
        return selectionHandles;
    }

    private void addSelectionHandle(Point p) {
        Rectangle r = new Rectangle((int) p.getX() - (selectionHandleSize / 2),
                (int) p.getY() - (selectionHandleSize / 2),
                selectionHandleSize, selectionHandleSize);
        selectionHandles.add(0, r);
    }
}
