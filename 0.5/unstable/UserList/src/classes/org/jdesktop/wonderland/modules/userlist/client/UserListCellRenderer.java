/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author JagWire
 */
public class UserListCellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer =
            new DefaultListCellRenderer();
    private Font inRangeFont = new Font("SansSerif", Font.PLAIN, 14);
    private Font outOfRangeFont = new Font("SansSerif", Font.PLAIN, 14);

    
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer =
                (JLabel) defaultRenderer.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        if (index < UserListManager.INSTANCE.getLastPositionInList()) {
            renderer.setFont(inRangeFont);
            renderer.setForeground(Color.BLUE);
        } else {
            renderer.setFont(outOfRangeFont);
            renderer.setForeground(Color.BLACK);
        }
        return renderer;
    }
}
