/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

/**
 * @author Alexios Akrimpai (akrim777@gmail.com)
 */

package org.jdesktop.wonderland.modules.wexplorer.client;

import javax.swing.*;
import java.awt.*;


public class ListRenderer  extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // for default cell renderer behavior
        Component c = super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);


        String title = value.toString();
        if (title.length()>7) {
            title = title.substring(0,7);
            title += "...";
        }

        //set the labels
        ((JLabel) c).setText(title);
        ((JLabel) c).setIcon(((WHModel)value).getThumbnail()); //change the path accordingly
        ((JLabel) c).setVerticalTextPosition(JLabel.NORTH);
        ((JLabel) c).setHorizontalTextPosition(JLabel.CENTER);
        ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) c).setToolTipText(Warehouse.cutString(((WHModel)value).getDesc()));
        return c;

    }

}
