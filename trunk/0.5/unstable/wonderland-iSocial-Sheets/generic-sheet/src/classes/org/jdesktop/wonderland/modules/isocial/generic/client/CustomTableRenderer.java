/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.isocial.generic.client;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Kaustubh
 */
public class CustomTableRenderer extends JTextArea implements TableCellRenderer {

    public CustomTableRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setAlignmentX(CENTER_ALIGNMENT);
        setAlignmentY(CENTER_ALIGNMENT);
        setEditable(false);
        setPreferredSize(new Dimension(100, 80));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean isFocused, int row, int column) {
        setText((value == null) ? "" : value.toString());
//        int stringWidth = getFontMetrics(getFont()).stringWidth(getText());
//        int headerwidth = table.getColumnModel().getColumn(column).getWidth();
//        if (stringWidth >= headerwidth) {
//            setSize(stringWidth, (int) getPreferredSize().getHeight());
//            table.getColumnModel().getColumn(column).setWidth(stringWidth);
//        } else {
//            setSize(headerwidth, (int) getPreferredSize().getHeight());
//        }
        return this;
    }
}
