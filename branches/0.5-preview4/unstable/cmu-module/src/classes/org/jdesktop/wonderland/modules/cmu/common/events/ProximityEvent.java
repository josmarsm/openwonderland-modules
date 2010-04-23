/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.cmu.common.events;

/**
 * Event corresponding to a Wonderland proximity event.
 * @author kevin
 */
public class ProximityEvent extends WonderlandEvent {

    private float distance = 0;
    private boolean eventOnEnter = true;

    public ProximityEvent() {
        
    }

    public ProximityEvent(float distance, boolean eventOnEnter) {
        super();
        this.setDistance(distance);
        this.setEventOnEnter(eventOnEnter);
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean isEventOnEnter() {
        return eventOnEnter;
    }

    public void setEventOnEnter(boolean eventOnEnter) {
        this.eventOnEnter = eventOnEnter;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof ProximityEvent && this.getClass().equals(other.getClass())) {
            ProximityEvent otherEvent = (ProximityEvent) other;

            if (this.getDistance() == otherEvent.getDistance() &&
                    this.isEventOnEnter() == otherEvent.isEventOnEnter()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Float.floatToIntBits(this.distance);
        hash = 53 * hash + (this.eventOnEnter ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        String toReturn = "Proximity event: ";
        if (this.isEventOnEnter()) {
            toReturn += "enter";
        } else {
            toReturn += "exit";
        }

        toReturn += " within " + this.getDistance() + " m";

        return toReturn;
    }
}
