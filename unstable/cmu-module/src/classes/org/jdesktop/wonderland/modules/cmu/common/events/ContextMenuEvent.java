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
 * Event representing a click on an entry of the right-click context menu for
 * a cell.
 * @author kevin
 */
public class ContextMenuEvent extends WonderlandEvent {

    private String menuText = "";

    public ContextMenuEvent() {
        
    }

    public ContextMenuEvent(String menuText) {
        this.setMenuText(menuText);
    }

    public String getMenuText() {
        return menuText;
    }

    public void setMenuText(String menuText) {
        this.menuText = menuText;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof ContextMenuEvent && this.getClass().equals(other.getClass())) {
            ContextMenuEvent otherEvent = (ContextMenuEvent) other;
            if (this.getMenuText().equals(otherEvent.getMenuText())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.menuText != null ? this.menuText.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Context menu event: " + this.getMenuText();
    }
}
