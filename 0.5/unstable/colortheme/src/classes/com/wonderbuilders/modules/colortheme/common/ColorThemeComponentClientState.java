/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.common;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for color theme component.
 */
public class ColorThemeComponentClientState extends CellComponentClientState {
    
    /** Name of the current color theme in use. */
    private String currentColorTheme;
    
    /** A theme previously in use. */
    private String previousTheme;
    
    private Map<String,String> textureMapping;
    
    /** Default constructor */
    public ColorThemeComponentClientState() {
        textureMapping = new HashMap<String, String>();
        currentColorTheme = ColorTheme.NONE_THEME_NAME;
        previousTheme = ColorTheme.NONE_THEME_NAME;
    }

    public void setCurrentColorTheme(String currentColorTheme) {
        this.currentColorTheme = currentColorTheme;
    }

    public String getCurrentColorTheme() {
        return currentColorTheme;
    }

    public void setPreviousTheme(String previousTheme) {
        this.previousTheme = previousTheme;
    }

    public String getPreviousTheme() {
        return previousTheme;
    }
    
    public void addTextureMapping(String oldTx, String newTx){
        textureMapping.put(oldTx, newTx);
    }

    public void setTextureMapping(Map<String, String> textureMapping) {
        this.textureMapping = textureMapping;
    }

    public Map<String, String> getTextureMapping() {
        return textureMapping;
    }
    
    
}
