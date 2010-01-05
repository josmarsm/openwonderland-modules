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

package org.jdesktop.wonderland.modules.thoughtbubbles.common;

import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class ThoughtBubblesComponentChangeMessage extends CellMessage {

    private final ThoughtBubblesAction action;

    public enum ThoughtBubblesAction {
        NEW_THOUGHT, 
    }

    private ThoughtRecord thought;

    public ThoughtBubblesComponentChangeMessage(ThoughtBubblesAction action) {
        this.action = action;
    }
    
    public ThoughtBubblesAction getAction() {
        return action;
    }

    public void setThought(ThoughtRecord thought) {
        this.thought = thought;
    }

    public ThoughtRecord getThought() {
        return this.thought;
    }
}