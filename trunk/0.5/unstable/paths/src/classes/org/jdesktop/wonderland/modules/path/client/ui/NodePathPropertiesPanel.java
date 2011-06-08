package org.jdesktop.wonderland.modules.path.client.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.modules.path.common.PathCellState;

/**
 * This class is a swing JPanel which contains the UI with which to interact with the
 *
 * @author Carl Jokl
 */
public class NodePathPropertiesPanel extends JPanel {

    private PathCellState pathState;
    private JCheckBox editModeField;
    private JCheckBox closedPathField;

    /**
     * Create a new instance of a NodePathPropertiesPanel to edit the properties
     * of the specified PathCellState.
     *
     * @param pathState The PathCellState to be edited using this NodePathPropertiesPanel
     */
    public NodePathPropertiesPanel(PathCellState pathState) {
        this.pathState = pathState;
    }

    protected void initUI() {
        setLayout(new BorderLayout());
        add(createBasicPropertiesPanel(), BorderLayout.NORTH);
        add(createStylePropertiesPanel(), BorderLayout.CENTER);
    }

    /**
     * Create a JPanel which contains the basic NodePath properties.
     *
     * @return A JPanel which contains the basic NodePath properties.
     */
    protected JPanel createBasicPropertiesPanel() {
        JPanel basicPropertiesPanel = new JPanel();
        GridBagLayout panelLayout = new GridBagLayout();
        basicPropertiesPanel.setLayout(panelLayout);
        Insets insets = new Insets(1, 1, 1, 1);
        GridBagConstraints layoutConstraints = new GridBagConstraints(0, 0, 1, 1, 0.5, 0.3, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 2, 1);
        JLabel editModeLabel = new JLabel("Edit Mode");
        basicPropertiesPanel.add(editModeLabel, layoutConstraints);
        editModeField = new JCheckBox();
        if (pathState != null) {
            editModeField.setSelected(pathState.isEditMode());
            editModeField.addChangeListener(new EditModeChangeListener());
        }
        layoutConstraints.gridx = 1;
        layoutConstraints.anchor = GridBagConstraints.WEST;
        basicPropertiesPanel.add(editModeField, layoutConstraints);
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.anchor = GridBagConstraints.EAST;
        JLabel closedPathLabel = new JLabel("Closed Path");
        basicPropertiesPanel.add(closedPathLabel, layoutConstraints);
        closedPathField = new JCheckBox();
        if (pathState != null) {
            closedPathField.setSelected(pathState.isClosedPath());
            closedPathField.addChangeListener(new ClosedPathChangedListener());
        }
        layoutConstraints.gridx = 1;
        layoutConstraints.anchor = GridBagConstraints.WEST;
        basicPropertiesPanel.add(closedPathField, layoutConstraints);
        basicPropertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Basic"));
        return basicPropertiesPanel;
    }

    /**
     * Create a JPanel which contains the NodePath style properties.
     *
     * @return A JPanel which contains the NodePath style properties.
     */
    protected JPanel createStylePropertiesPanel() {
        JPanel stylePropertiesPanel = new JPanel();

        stylePropertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Style"));
        return stylePropertiesPanel;
    }

    private class EditModeChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Object sourceObject = e.getSource();
            if (pathState != null && sourceObject instanceof JCheckBox) {
                pathState.setEditMode(((JCheckBox) sourceObject).isSelected());
            }
        }
    }

    private class ClosedPathChangedListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Object sourceObject = e.getSource();
            if (pathState != null && sourceObject instanceof JCheckBox) {
                pathState.setClosedPath(((JCheckBox) sourceObject).isSelected());
            }
        }
    }
}
