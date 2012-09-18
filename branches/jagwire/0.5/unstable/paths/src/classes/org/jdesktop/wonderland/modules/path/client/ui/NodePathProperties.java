package org.jdesktop.wonderland.modules.path.client.ui;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.path.common.PathCellServerState;

/**
 * This class represents properties or property editing as relating to a NodePath or the PathCellServerState.
 *
 * @author Carl Jokl
 */
@PropertiesFactory(PathCellServerState.class)
public class NodePathProperties implements PropertiesFactorySPI {

    public static final String DISPLAY_NAME = "Path Properties";
    private CellPropertiesEditor editor;
    private PathCellServerState editedState;
    private PathCellServerState originalState;
    private NodePathPropertiesPanel propertiesPanel;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JPanel getPropertiesJPanel() {
        if (propertiesPanel == null) {
            propertiesPanel = new NodePathPropertiesPanel(editedState);
        }
        return propertiesPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        if (editor != null) {
            CellServerState state = editor.getCellServerState();
            if (state instanceof PathCellServerState) {
                editedState = (PathCellServerState) state;
                originalState = editedState.clone();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        editedState = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
