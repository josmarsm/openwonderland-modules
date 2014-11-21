/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import com.jme.image.Texture;
import java.awt.Component;
import java.awt.Image;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;


/** 
 *  List cell renderer for textures list. This class will display texture images in a list.
 *
 * @author Vladimir Djurovic
 */
public class TextureListCellRenderer extends DefaultListCellRenderer {

    private static final Logger LOGGER = Logger.getLogger(TextureListCellRenderer.class.getName());

    /**
     * String representing server name and port of current server.
     */
    private String serverAndPort;
    
    /**
     * Constructs new instance
     * 
     * @param serverAndPort servr name and port string
     */
    public TextureListCellRenderer( String serverAndPort) {
        this.serverAndPort = serverAndPort;
    }
    

    /**
     * Returns rendering component for this list item.
     * 
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return 
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Texture) {
            label.setVerticalTextPosition(SwingConstants.BOTTOM);
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            try {

                ImageIcon icon = new ImageIcon(AssetUtils.getAssetURL(((Texture)value).getImageLocation(), serverAndPort));
                Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_FAST);
                icon.setImage(scaled);
                label.setIcon(icon);
                label.setText("");
            } catch (MalformedURLException muex) {
                LOGGER.log(Level.SEVERE, "Invalid image URL: {0}", muex.getMessage());
            }


        }
        return label;
    }
}
