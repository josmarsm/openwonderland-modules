/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.io.Serializable;

/**
 * An image with an associated date
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedNews implements DatedObject, Serializable {
    private TimelineDate date;
    private String title;
    private String uri;
    private String text;

    // image is optional
    private String imageURI;
    private int width = -1;
    private int height = -1;

    public DatedNews(TimelineDate date, String title, String uri, String text) {
        this.date = date;
        this.title = title;
        this.uri = uri;
        this.text = text;
    }

    public TimelineDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getURI() {
        return uri;
    }

    public String getText() {
        return text;
    }

    /**
     * Return the optional image, or null if there is no image
     */
    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
    
    /**
     * Return the image width, or -1 if not known
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Return the image height, or -1 if not known
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "[DatedNews " + uri + "]";
    }
}
