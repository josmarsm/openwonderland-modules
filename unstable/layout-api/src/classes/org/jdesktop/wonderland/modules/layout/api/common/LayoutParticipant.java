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
package org.jdesktop.wonderland.modules.layout.api.common;

import com.jme.bounding.BoundingVolume;
import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A layout participant represents an object that can be added to a layout.
 * The participant manages a few key pieces of information, such as size
 * and position.  The layout will change the position of the participant by
 * calling the <code>setPosition()</code> method on the participant.
 * <p>
 * Participants may or may not support resizing.  If a participant does support
 * resizing, it should return true from the <code>isResizable()</code> method.
 * If an object is resizable, the layout may choose to change its size using
 * <code>setSize()</code>.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public interface LayoutParticipant extends Serializable {
    /**
     * Get the preferred bounds of this object. This is the size of the object
     * unless setSize() is called.  Note that setSize() will not be called to
     * set the participant to the given size.
     * @return this object's preferred bounds.
     */
    public BoundingVolume getPreferredSize();

    /**
     * Move this object to the given position.
     * @param position the position to move to.
     */
    public void setPosition(CellTransform transform);

    /**
     * Get the current position of this object.
     * @return the current position of this object.
     */
    public CellTransform getPosition();

    /**
     * Return true if this participant supports resizing.
     * @return true if the participant supports resizing, or false if not.
     */
    public boolean isResizable();

    /**
     * Set the current size of this object to fit within the given bounds.
     * @param bounds the bounds the object should fit in.
     * @throws UnsupportedOperationException if isResizable() returns false.
     */
    public void setSize(BoundingVolume bounds);

    /**
     * Get the current size of the object. This is either the preferred size
     * of the object if setSize() has never been called, or the last value
     * set with setSize() if it has.
     * @return the current size of the object.
     */
    public BoundingVolume getSize();

    /**
     * Called when the participant is added to a layout. A participant can only
     * be part of a single layout at a time. <code>removed()</code> is
     * therefore guaranteed to be called before <code>added()</code> is called
     * again.
     * @param layout the layout the participant was added to.
     */
    public void added(Layout layout);

    /**
     * Called when the participant is removed from a layout.
     * @param layout the layout the participant was removed from.
     */
    public void removed(Layout layout);
}
