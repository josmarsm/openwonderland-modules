/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Constant values used in app frame. Implemented as an enum to guarantee
 * it is a singleton.
 */
public enum AppFrameConstants {
    INSTANCE;
    
    /**
     * Map name
     */
    public static final String MAP_NAME = "AppFrame";
    public static final String History_MAP = "AppFrameApp";
    public static final String Prop_MAP="AppFrameProp";
    public static final String PintoMenu_MAP="AppFramePinToMenu";
   public static String dirtyMap="dirtyMap";
    
    /**
     *  property name
     */
    public static List<String> extension=new ArrayList<String>();
    public static String  BorderColor="0:192:0:255";
    public static  String Orientation="Horizontal";
    public static String  MaxHistory="5";
    public static String  AspectRatio="3*4";
    public static HashMap<String,String>  PinToMenu=new HashMap<String,String>();
}
