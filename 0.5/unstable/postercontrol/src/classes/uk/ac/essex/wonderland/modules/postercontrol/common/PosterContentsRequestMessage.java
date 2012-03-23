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
 * Message sent to the PosterControlConnectionHandler to request
 * the contents of a poster.  A PosterContentsResponseMessage or an ErrorMessage will
 * be returned, depending if the operation failed or succeeded.
 *
 * @author Bernard Horan
 */
public class PosterContentsRequestMessage extends Message {
    private int cellID;

    public PosterContentsRequestMessage(int cellID) {
        this.cellID = cellID;
    }

    public int getCellID() {
        return cellID;
    }
}
