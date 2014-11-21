/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * This class represents color theme, ie. contains mapping between base colors and theme colors.
 *
 * @author Vladimir Djurovic
 */
@ServerState
@XmlRootElement (name = "color-theme")
public class ColorTheme extends SharedData implements Comparable<ColorTheme> {
    
    private static final Logger LOGGER = Logger.getLogger(ColorTheme.class.getName());
    
    /** Theme name for empty theme in a list. */
    public static final String NONE_THEME_NAME = "- None -";
    /** Default theme name, if none is specified.  */
    public static final String DEFAULT_THEME_NAME = "Untitled Theme";
    
    /** Display name of the theme. */
    private String themeName;
    /** Color mapping for current theme. Map key is base color, and value is replacement color.
     *  All colors are hex strings in form "RRGGBB" (red, green and blue value).
     */
    private Map<String, String> colorMap;
    /** 
     * Holds reverse mapping information, ie. theme colors mapped to base grey scale colors. This 
     * map is updated each time new color map is set.
     * This field is used to keep track of changes to color theme, so it can be consistently applied
     *  to models.
     */
    private Map<String, String> reverseMap;
    
    public ColorTheme(){
        themeName = DEFAULT_THEME_NAME;
        colorMap = new HashMap<String, String>();
        reverseMap = new HashMap<String, String>();
    }
    
    public ColorTheme(String name){
        this();
        themeName = name;
    }
    
    public ColorTheme(ColorTheme base){
        this();
        themeName = "Copy of " + base.getThemeName();
        setColorMap(base.getColorMap());
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeName() {
        return themeName;
    }

    public final void setColorMap(Map<String, String> colorMap) {
        this.colorMap = colorMap;
        // add reverse entries
        for(Map.Entry<String, String> entry : colorMap.entrySet()){
            reverseMap.put(entry.getValue(), entry.getKey());
        }
    }

    public Map<String, String> getColorMap() {
        return colorMap;
    }
    
    /**
     * Returns base grey scale color for specified color theme color. If no specified 
     * base color is found for a supplied argument, the argument is the return value.
     * 
     * @param themeColor theme color
     * @return  base color if found, otherwise argument color
     */
    public String getBaseColor(String themeColor){
        return reverseMap.containsKey(themeColor) ? reverseMap.get(themeColor) : themeColor;
    }

    @Override
    public String toString() {
        return themeName;
    }

    public int compareTo(ColorTheme o) {
        return this.themeName.compareTo(o.getThemeName());
    }
    
    /**
     * Creates empty color theme, ie. ome where each of the base colors is mapped
     * to itself.
     * 
     * @return  color theme
     */
    public static ColorTheme createNeutralColorTheme(){
        ColorTheme ct = new ColorTheme(NONE_THEME_NAME);
        Properties props = new Properties();
        try{
            props.load(ColorTheme.class.getResourceAsStream("/com/wonderbuilders/modules/colortheme/client/resources/colors.properties"));
            Map<String, String> colors = new HashMap<String, String>();
            for(Object val : props.values()){
                colors.put(val.toString(), val.toString());
            }
            ct.setColorMap(colors);
        } catch (IOException iex){
            LOGGER.log(Level.SEVERE, "Could not load color mappings from properties file: {0}", iex.getMessage());
        }
        return ct;
        
    }
    
}
