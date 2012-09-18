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

import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A response to a PosterContentsRequestMessage.  The response contains
 * the name of the poster and its contents.
 *
 * @author Bernard Horan
 */
public class PosterContentsResponseMessage extends ResponseMessage {
    private String posterContents;
    private String cellName;

    public PosterContentsResponseMessage(MessageID id, String cellName, String contents) {
        super (id);
        this.cellName = cellName;
        this.posterContents = contents;
    }

    public String getPosterContents() {
        return posterContents;
    }

    public String getCellName() {
        return cellName;
    }
}
