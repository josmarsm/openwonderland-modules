/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider;

/**
 * An object for use on a timeline.  Each object has an associated date.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface DatedObject {
    /**
     * Get the date associated with this object.
     * @return this object's date
     */
    public TimelineDate getDate();
}
