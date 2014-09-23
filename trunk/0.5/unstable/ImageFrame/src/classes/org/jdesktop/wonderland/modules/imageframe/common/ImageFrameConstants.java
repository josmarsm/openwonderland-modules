/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.common;


/**
 * Constant values used in app frame. Implemented as an enum to guarantee
 * it is a singleton.
 */
public enum ImageFrameConstants {
    INSTANCE;
    
    /**
     * Map name
     */
    public static final String MAP_NAME = "ImageFrame";
    public static String propertyMap="propertyMap";
    public static String ImageFrameProperty="ImageFrameProperty";
    public static String[] fitArray = {"Fit Image","Constrain Height","Constrain Width"};
    public static String[] aspectRatioArray = {"1:1","5:4","4:3","16:9","3:2"};
    public static String[] orientationArray = {"Horizontal","vertical"};
    public static double[] daspectRatioArray = {(double)1/(double)1,(double)5/(double)4
            ,(double)4/(double)3,(double)16/(double)9,(double)2/(double)3};
    
    public static  int fit = -1;
    public static int aspectRatio = -1;
    public static int orientation = -1;
    /**
     *  property name
     */
  }
