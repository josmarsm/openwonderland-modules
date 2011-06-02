package org.jdesktop.wonderland.modules.path.client.ui;

import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class is used as the call back Positionable for all PathNode
 * insertions.
 *
 * @author Carl Jokl
 */
public class InsertNodePositionable implements Positionable, Disposable {

    private ClientNodePath path;
    private final boolean insertBefore;
    private final int insertionIndex;

    /**
     * Create a new instance of InsertNodePositionable to be used to insert a PathNode before or after the PathNode
     * at the specified index.
     *
     * @param path The ClientNodePath into which the new PathNode will be inserted.
     * @param insertBefore Whether the new PathNode should be inserted before the PathNode at the specified index. If false it will be inserted after.
     * @param insertionIndex The index of the PathNode relative to which the new PathNode will be inserted.
     * @throws IllegalArgumentException If the specified ClientNodePath is null.
     */
    public InsertNodePositionable(final ClientNodePath path, final boolean insertBefore, final int insertionIndex) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("The node path into which the new node will be inserted cannot be null!");
        }
        this.path = path;
        this.insertBefore = insertBefore;
        this.insertionIndex = insertionIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void usePosition(float x, float y, float z) {
        path.insertNode(insertBefore ? insertionIndex : insertionIndex + 1, x, y, z, null);
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        path = null;
    }
}
