/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.postercontrol.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * A message sent to the PosterControlConnectionHandler to change the
 * contents of a particular poster.  The state of this message includes the
 * id of the cell to update and the contents to change it to.
 *
 * @author Bernard Horan
 */
public class ChangeContentsMessage extends Message {
    private int cellID;
    private String contents;

    public ChangeContentsMessage(int cellID, String contents) {
        super();

        this.cellID = cellID;
        this.contents = contents;
    }

    public int getCellID() {
        return cellID;
    }

    public String getContents() {
        return contents;
    }
}
