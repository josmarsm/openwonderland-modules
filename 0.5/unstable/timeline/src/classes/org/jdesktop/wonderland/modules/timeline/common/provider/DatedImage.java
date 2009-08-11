/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serializable;
import java.net.URL;

/**
 * An image with an associated date
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedImage implements DatedObject, Serializable {
    private TimelineDate date;
    private URL imageURL;
    
    public DatedImage(TimelineDate date, URL imageURL) {
        this.date = date;
        this.imageURL = imageURL;
    }

    public TimelineDate getDate() {
        return date;
    }

    public Image getImage() {
        return Toolkit.getDefaultToolkit().createImage(imageURL);
    }

    @Override
    public String toString() {
        return "[DatedImage " + imageURL + "]";
    }
}
