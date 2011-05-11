package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.PathNodeGroup;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This interface contains the functionality of a client version of the NodePath.
 * This extends other interfaces and overrides some such that it handles ClientPathNodes.
 *
 * @author Carl Jokl
 */
public interface ClientNodePath extends NodePath, PathNodeGroup, Serializable {

    /**
     * Get the ClientPathNode in the group at the specified index in the path.
     * This is a covariant override of the same method in the PathNodeGroup
     * interface to have the more specific ClientPathNode returned instead of
     * just a PathNode.
     *
     * @param index The index of the PathNode in the path to be retrieved.
     * @return The PathNode at the specified index in the path.
     * @throws IndexOutOfBoundsException If the specified index is outside the range of PathNode indices.
     */
    @Override
    public ClientPathNode getPathNode(int index) throws IndexOutOfBoundsException;

    /**
     * Get the first ClientPathNode in the ClientNodePath.
     *
     * @return The ClientPathNode at the beginning of the chain of ClientPathNodes.
     */
    public ClientPathNode getFirstPathNode();

    /**
     * Get the last ClientPathNode is the ClientNodePath.
     * 
     * @return The ClientPathNode at the end of the chain of ClientPathNodes.
     */
    public ClientPathNode getLastPathNode();

    /**
     * Get the PathNode with the specified label.
     *
     * @param label The label of the PathNode to be returned.
     * @return The PathNode with the specified label or null if no PathNode
     *         was found with that with that label.
     */
    public ClientPathNode getPathNode(String label);

    /**
     * Add a new node to this PathCell.
     *
     * @param node The node to be added to this PathCell.
     * @return True if the node was able to be added successfully.
     */
    public boolean addNode(ClientPathNode node);

    /**
     * Add a new node to this PathCell.
     *
     * @param position The position of the of the new node to be added to this ClientNodePath.
     * @param name The name of the new node to be added to this ClientNodePath (optional).
     * @return True if the node was able to be added successfully.
     */
    public boolean addNode(Vector3f position, String name);

    /**
     * Add a new node to this PathCell.
     *
     * @param x The x position of the of the new node to be added to this ClientNodePath.
     * @param y The y position of the of the new node to be added to this ClientNodePath.
     * @param z The z position of the of the new node to be added to this ClientNodePath.
     * @param name The name of the new node to be added to this ClientNodePath (optional).
     * @return True if the node was able to be added successfully.
     */
    public boolean addNode(float x, float y, float z, String name);

    /**
     * Insert the specified ClientPathNode at the specified node index.
     *
     * @param nodeIndex The index at which the ClientPathNode is to be inserted. If the insertion index is the
     *                  same as the number of nodes before insertion then the method is essentially like addNode
     *                  except there is no ClientPathNode to be returned by the method in that case.
     * @param node The ClientPathNode to be inserted at the specified index.
     * @return The ClientPathNode which used to be at the specified index (if any).
     * @throws IndexOutOfBoundsException If the specified nodeIndex at which to insert the node is invalid.
     * @throws IllegalArgumentException If the specified ClientPathNode to be inserted was null.
     */
    public ClientPathNode insertNode(int nodeIndex, ClientPathNode node) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Insert the specified ClientPathNode at the specified node index.
     *
     * @param nodeIndex The index at which the ClientPathNode is to be inserted. If the insertion index is the
     *                  same as the number of nodes before insertion then the method is essentially like addNode
     *                  except there is no ClientPathNode to be returned by the method in that case.
     * @param position The position of the of the new node to be added to this ClientNodePath.
     * @param name The name of the new node to be added to this ClientNodePath (optional).
     * @return The ClientPathNode which used to be at the specified index (if any).
     * @throws IllegalArgumentException If the specified position of the ClientPathNode to be inserted was null.
     * @throws IndexOutOfBoundsException If the specified nodeIndex at which to insert the node is invalid.
     */
    public ClientPathNode insertNode(int nodeIndex, Vector3f position, String name) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Insert the specified ClientPathNode at the specified node index.
     *
     * @param nodeIndex The index at which the ClientPathNode is to be inserted. If the insertion index is the
     *                  same as the number of nodes before insertion then the method is essentially like addNode
     *                  except there is no ClientPathNode to be returned by the method in that case.
     * @param node The ClientPathNode to be inserted at the specified index.
     * @param x The x position of the of the new node to be added to this ClientNodePath.
     * @param y The y position of the of the new node to be added to this ClientNodePath.
     * @param z The z position of the of the new node to be added to this ClientNodePath.
     * @param name The name of the new node to be added to this ClientNodePath (optional).
     * @return The ClientPathNode which used to be at the specified index (if any).
     * @throws IndexOutOfBoundsException If the specified nodeIndex at which to insert the node is invalid.
     */
    public ClientPathNode insertNode(int nodeIndex, float x, float y, float z, String name) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Remove the specified PathNode from the PathCell.
     *
     * @param node The PathNode to be removed from the PathCell.
     * @return True if the PathNode was not null and existed in the PathCell
     *         and was able to be removed successfully.
     */
    public boolean removeNode(ClientPathNode node);

    /**
     * Remove the PathNode at the specified node index.
     *
     * @param nodeIndex The index of the PathNode to be removed.
     * @return The PathNode removed from the specified Index.
     * @throws IndexOutOfBoundsException If the specified node index of the ClientPathNode
     *                                   to be removed is outside the range of valid node indices.
     */
    public ClientPathNode removeNodeAt(int nodeIndex) throws IndexOutOfBoundsException;

    /**
     * Remove all of the PathNodes which are part of this ClientNodePath.
     */
    public void removeAllNodes();

    /**
     * Set the IndexedPathNode position and send a message to the server to notify of the change.
     *
     * @param index The index of the IndexedPathNode for which to set the position.
     * @param x The new X position of the IndexedPathNode.
     * @param y The new Y position of the IndexedPathNode.
     * @param z The new Z position of the IndexedPathNode.
     * @return True of the new IndexedPathNode position was able to be set and a notification was sent to the server.
     */
    public boolean setNodePosition(int index, float x, float y, float z);

    /**
     * Get the NodeStyleType of the PathNode at the specified NodeIndex.
     *
     * @param nodeIndex The index of the PathNode for which the NodeStyleType is to be retrieved.
     * @return The NodeStyleType of the PathNode at the specified index (if set).
     * @throws IndexOutOfBoundsException If the specified PathNode index is outside the range of
     *                                   valid PathNode indices.
     */
    public NodeStyleType getNodeStyleType(int nodeIndex) throws IndexOutOfBoundsException;

    /**
     * Get the NodeStyle of the PathNode at the specified index.
     *
     * @param nodeIndex The index of the PathNode for which the NodeStyle is to be retrieved.
     * @return The NodeStyle of the PathNode at the specified index (if set).
     * @throws IndexOutOfBoundsException If the specified PathNode index is outside the range of
     *                                   valid PathNode indices.
     */
    public NodeStyle getNodeStyle(int nodeIndex) throws IndexOutOfBoundsException;

    /**
     * Get the SegmentStyleType for the path segment at the specified segment index.
     *
     * @param segmentIndex The index of the path segment for which the SegmentStyle is to be retrieved.
     * @return The SegmentStyleType for the path segment
     * @throws IndexOutOfBoundsException If the specified path segment index is outside the range of
     *                                   valid path segment indices.
     */
    public SegmentStyleType getSegmentStyleType(int segmentIndex) throws IndexOutOfBoundsException;

    /**
     * Get the SegmentStyle for the path segment at the specified segment index.
     *
     * @param segmentIndex The index of the path segment for which the SegmentStyle is to be retrieved.
     * @return The SegmentStyle of the path segment at the specified index (if set).
     * @throws IndexOutOfBoundsException If the specified path segment index is outside the range of
     *                                   valid path segment indices.
     */
    public SegmentStyle getSegmentStyle(int segmentIndex) throws IndexOutOfBoundsException;

    /**
     * Set the state of this ClientNodePath using the specified PathCellState.
     * Any changes should update the graphical representation of the ClientNodePath.
     *
     * @param state The PathCellState to use to set the values within this ClientNodePath.
     */
    public void setFrom(PathCellState state);
}
