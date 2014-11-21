/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.tokensheet.client;

import java.awt.Color;

/**
 * This class manages the images for token system. It assigns one image per student
 * depending upon the student's color assignment. The assigned color and image
 * remains same per cohort.
 *
 * 
 * @author Kaustubh
 */
public class ImageManager {

    private static final String baseUrl = "/org/jdesktop/wonderland/modules/isocial/tokensheet/client/resources/";
    //This color collection is same as ColorStore in color-manager.
    private static final String[] ASSIGN_COLORS = new String[]{
        "#C02F64", // pink
        "#008848", // green
        "#005CA7", // blue
        "#A5CD39", // lime
        "#D1662C", // gold
        "#7E4298", // purple
        "#47A4AD", // turquoise
    };
    private static final String[] ASSIGN_IMG = new String[]{
        "pink.png",
        "green.png",
        "blue.png",
        "lime.png",
        "gold.png",
        "purple.png",
        "turquoise.png",};

    /**
     * This method returns the appropriate image name for the given color.
     * If no matching image name found, it returns the white image.
     * @param color
     * @return
     */
    public static String getImageNameFor(Color color) {
        for (int i = 0; i < ASSIGN_COLORS.length; i++) {
            String colorName = ASSIGN_COLORS[i];
            if (Color.decode(colorName).equals(color)) {
                return baseUrl + ASSIGN_IMG[i];
            }
        }
        return baseUrl + "white.png";
    }
}
