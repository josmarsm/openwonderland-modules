/**
 * Project Looking Glass
 * 
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$ 
 */
package org.jdesktop.lg3d.wonderland.pdfviewer.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFMessage.Action;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 *
 * @author jkaplan
 */
public class PDFMessage extends Message {
    public enum Action { START_HOVER, STOP_HOVER, SELECT };
    
    private String selectionID;
    private Action action;
    
    public PDFMessage() {
        this (null, null);
    }
    
    public PDFMessage(String selectionID, Action action) {
        this.selectionID = selectionID;
        this.action = action;
    }

    public String getSelectionID() {
        return selectionID;
    }

    public Action getAction() {
        return action;
    }

    protected void extractMessageImpl(ByteBuffer data) {
        selectionID = DataString.value(data);
        action = Action.values()[DataInt.value(data)];
    }

    protected void populateDataElements() {
        dataElements.clear();
        dataElements.add(new DataString(selectionID));
        dataElements.add(new DataInt(action.ordinal()));
    }    
}
