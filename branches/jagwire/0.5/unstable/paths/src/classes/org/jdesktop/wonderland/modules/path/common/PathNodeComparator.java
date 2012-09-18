package org.jdesktop.wonderland.modules.path.common;

import java.util.Comparator;

/**
 * This is a Comparator which is used in sorting PathNodes.
 *
 * @author Carl Jokl
 */
public class PathNodeComparator implements Comparator<PathNode> {

    /**
     * Compare the order of the first PathNode relative to the second PathNode.
     *
     * @param first The first PathNode that will have its order compared to the second PathNode.
     * @param second The second PathNode to which the order of the first PathNode is relative.
     * @return Positive if the first PathNode is greater than the second PathNode, negative if
     *         the first PathNode is less than the second PathNode and zero of the two are equal.
     */
    @Override
    public int compare(PathNode first, PathNode second) {
        int firstIndex = first != null ? first.getSequenceIndex() : -1;
        int secondIndex = second != null ? second.getSequenceIndex() : -1;
        return firstIndex < secondIndex ? -1 : firstIndex > secondIndex ? 1 : 0;
    }

}
