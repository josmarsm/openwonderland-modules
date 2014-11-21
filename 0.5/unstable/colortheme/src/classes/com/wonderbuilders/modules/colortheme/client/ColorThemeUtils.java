/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import java.awt.Color;

/**
 * Contains utility methods for color theme component.
 *
 * @author Vladimir Djurovic
 */
public class ColorThemeUtils {
    
    /**
     * Converts a given string into {@code Color}. Parameter must represent a hex RGB value (eg. FFEEFF).
     * 
     * @param colorString color string
     * @return  color
     */
    public static Color convertStringToColor(String colorString){
        int r = Integer.parseInt(colorString.substring(0, 2), 16);
        int g = Integer.parseInt(colorString.substring(2, 4), 16);
        int b = Integer.parseInt(colorString.substring(4, 6), 16);
        Color color = new Color(r, g, b);
        
        return color;
    }
    
    /**
     * Converts color to hex RGB string. Each part (R,G,B) is zero-padded to 2 digits.
     * @param color
     * @return 
     */
    public static String convertColorToString(Color color){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", color.getRed()));
        sb.append(String.format("%02X", color.getGreen()));
        sb.append(String.format("%02X", color.getBlue()));
        
        // return string as upper case, padded to 6 characters
        return String.format(sb.toString().toUpperCase());
    }
}
