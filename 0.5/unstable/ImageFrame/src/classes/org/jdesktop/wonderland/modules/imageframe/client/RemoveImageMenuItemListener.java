/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import java.util.List;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;
import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;

/**
 * listener for right click menu item
 */
class RemoveImageMenuItemListener implements ContextMenuActionListener {

    private ImageFrameCell cell = null;

    public RemoveImageMenuItemListener(ImageFrameCell cell) {
        this.cell = cell;
    }

    public void actionPerformed(ContextMenuItemEvent event) {

        List l = cell.getChildren();
        if (l != null && !l.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(JmeClientMain.getFrame().getFrame(),
                    "Are you sure want to remove the image?", "Remove Image", JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                //just remove the child image viewer cell of image frame cell.
                ImageViewerCell ivCell = (ImageViewerCell) l.get(0);
                CellUtils.deleteCell(ivCell);

                ImageFrameProperties props = (ImageFrameProperties) cell.propertyMap
                        .get(ImageFrameConstants.ImageFrameProperty, ImageFrameProperties.class);

                ImageFrameProperties ifp = new ImageFrameProperties();

                ifp.setFit(props.getFit());
                ifp.setAspectRatio(props.getAspectRatio());
                ifp.setOrientation(props.getOrientation());
                ifp.setIsRemoveImage(true);
                cell.propertyMap.put(ImageFrameConstants.ImageFrameProperty, ifp);

            }
        } else {
            JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(), "No Image found on the frame.");
        }
    }
}
