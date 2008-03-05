/**
 * Project Looking Glass
 * 
 * $RCSfile: SampleModuleCellMessage.java,v $
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
 * $Date: 2007/09/19 23:01:39 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.samplemodule.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 * @author jkaplan
 */
public class SampleModuleCellMessage extends CellMessage {
    private String selectionID;
    
    public SampleModuleCellMessage() {
        super();
    }
    
    public SampleModuleCellMessage(CellID cellID, String selectionID) {
        super(cellID);
    
        this.selectionID = selectionID;
    }

    public String getSelectionID() {
        return selectionID;
    }

    public void setSelectionID(String selectionID) {
        this.selectionID = selectionID;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
    
        selectionID = DataString.value(data);
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        
        dataElements.add(new DataString(selectionID));
    }
}
