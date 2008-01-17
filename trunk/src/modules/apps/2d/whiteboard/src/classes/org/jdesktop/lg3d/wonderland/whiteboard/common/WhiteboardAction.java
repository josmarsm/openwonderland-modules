/**
 * Project Looking Glass
 *
 * $RCSfile: WhiteboardAction.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.1 $
 * $Date: 2007/09/21 16:54:20 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.common;

/**
 * Whiteboard action types
 * @author nsimpson
 */
public interface WhiteboardAction {
    public enum Action { 
        NO_ACTION, 
        SET_TOOL, 
        SET_COLOR, 
        MOVE_TO, 
        DRAG_TO,
        REQUEST_SYNC, 
        EXECUTE_COMMAND 
    };
    
    public final static Action NO_ACTION = Action.NO_ACTION;
    public final static Action SET_TOOL = Action.SET_TOOL;
    public final static Action SET_COLOR = Action.SET_COLOR;
    public final static Action MOVE_TO = Action.MOVE_TO;
    public final static Action DRAG_TO = Action.DRAG_TO;
    public final static Action REQUEST_SYNC = Action.REQUEST_SYNC;  
    public final static Action EXECUTE_COMMAND = Action.EXECUTE_COMMAND; 
}
