/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *  Preview panel used for browsing texture images. It shows texture image preview with 
 * some additional info (eg. resolution). Currently, only <code>.jpg</code> and <code>.png</code> 
 * file types are supported.
 *
 * @author Vladimir Djurovic
 */
public class TextureImagePreviewPanel extends JPanel implements PropertyChangeListener {

    /** File extension which this preview panel supports. Currently, .jpg and .png. */
    private static final String[] ALLOWED_EXTENSIONS;
    /** default preview panel width. */
    private static final int ACC_WIDTH = 200;
    /** Size of preview image. */
    private static final int PREVIEW_SIZE = 150;
    
    /**
     * Static initializer.
     */
    static {
        ALLOWED_EXTENSIONS = new String[]{".jpg", ".png", ".jpeg"};
    }
    
    
    private ImageIcon icon;
    private Image image;
    /** Label's icon is used to display image. */
    private JLabel displayLabel;
    
    /**
     * Construct new panel.
     */
    public TextureImagePreviewPanel(){
        setPreferredSize(new Dimension(ACC_WIDTH, -1));
        setLayout(new BorderLayout());
        displayLabel = new JLabel();
        displayLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        displayLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        add(displayLabel, BorderLayout.CENTER);
    }
    
    /**
     * Handle selected file change events.
     * 
     * @param evt  event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if(propName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)){
            File selection = (File)evt.getNewValue();
            
            if(selection != null){
                String filePath = selection.getAbsolutePath();
                String extension = filePath.substring(filePath.lastIndexOf('.'));
                if(Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)){
                    icon = new ImageIcon(filePath);
                    image = icon.getImage();
                    icon.setImage(image.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_FAST));
                    displayLabel.setIcon(icon);
                    displayLabel.setText( + image.getWidth(this) + " x " + image.getHeight(this));

                }
            }
        }
    }
    
}
