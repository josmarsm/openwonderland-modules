package org.jdesktop.wonderland.modules.path.common;

import org.jdesktop.wonderland.modules.path.common.style.PathStyle;

/**
 * This interface represents the common attributes and functionality for both the PathCell itself and the various
 * PathCellState objects.
 *
 * @author Carl Jokl
 */
public interface NodePath {

    /**
     * Get the PathStyle of this PathInfoHolder.
     *
     * @return The PathStyle of this PathInfoHolder.
     */
    public PathStyle getPathStyle();

    /**
     * Get whether the path is currently being displayed in edit mode.
     *
     * @return True if the path is currently being displayed in edit mode.
     */
    public boolean isEditMode();

    /**
     * Get whether this path is set to be a closed path i.e. it forms a loop
     * with the start end end points connecting to complete the loop.
     *
     * @return True if the path is closed and therefore forms a complete loop.
     */
    public boolean isClosedPath();

    /**
     * Set whether the path is currently being displayed in edit mode.
     *
     * @param editMode True if the path is currently being displayed in edit mode.
     */
    public void setEditMode(boolean editMode);

    /**
     * Get whether this path is set to be a closed path i.e. it forms a loop
     * with the start end end points connecting to complete the loop.
     *
     * @param closedPath True if the path is closed and therefore forms a complete loop.
     */
    public void setClosedPath(boolean closedPath);

    /**
     * Set the style of the PathCell.
     *
     * @param pathStyle Set the PathStyle of this PathCell.
     */
    public void setPathStyle(PathStyle pathStyle);
}
