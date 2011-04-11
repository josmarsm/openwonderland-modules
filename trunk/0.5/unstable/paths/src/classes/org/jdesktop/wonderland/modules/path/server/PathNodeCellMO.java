package org.jdesktop.wonderland.modules.path.server;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * This managed object represents a PathNode on a Path.
 * This should be accessed via managed references.
 *
 * @author Carl Jokl
 */
public class PathNodeCellMO extends CellMO implements PathNode {

    /**
     * This is the name of the client PathNodeCell class.
     */
    public static final String CLIENT_CELL_CLASS_NAME = "org.jdesktop.wonderland.modules.path.client.PathNodeCell";

    private int sequenceIndex;

    public PathNodeCellMO() {
        sequenceIndex = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return CLIENT_CELL_CLASS_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNamed() {
        return getName() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3f getLocalPosition() {
        CellTransform localTransform = new CellTransform();
        getLocalTransform(localTransform);
        return localTransform.getTranslation(new Vector3f());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3f getGlobalPosition() {
        CellTransform localTransform = new CellTransform();
        getLocalTransform(localTransform);
        return localTransform.getTranslation(new Vector3f());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSequenceIndex() {
        return sequenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }
}
