/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.common;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for the color theme component.
 */
@XmlRootElement(name="colortheme-component")
@ServerState
public class ColorThemeComponentServerState extends CellComponentServerState {
    
    /** Color theme currently in use. */
    private String currentColorTheme;
    
    /** Previous color theme. This is used for changing colors of already applied theme,
     *  since base color data is lost.
     */
    private String previousTheme;
    
    /** Maps cell texture to alternative texture. Both key and value are string
     which represent texture location.*/
    private Map<String, String> textureMapping;
    
    
    /** Default constructor */
    public ColorThemeComponentServerState() {
        textureMapping = new HashMap<String, String>();
        currentColorTheme = ColorTheme.NONE_THEME_NAME;
        previousTheme = ColorTheme.NONE_THEME_NAME;
    }

    @Override
    public String getServerComponentClassName() {
        return "com.wonderbuilders.modules.colortheme" +
               ".server.ColorThemeComponentMO";
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
