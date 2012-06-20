/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *  
 *  Copyright (c) 2011, University of Essex, UK, 2011, All Rights Reserved.
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

package uk.ac.essex.wonderland.modules.countdowntimer.common;

import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 *
 * @author Bernard Horan, bernard@essex.ac.uk
 */
public class StartTimerResponseMessage extends ResponseMessage {
    private final String text;
    private CountdownFailureException ex = null;

    /**
     * 
     * @param messageID
     * @param string
     * @param ex
     */
    public StartTimerResponseMessage(MessageID messageID, String string, CountdownFailureException ex) {
        super(messageID);
        text = string;
        this.ex = ex;
    }

    /**
     *
     * @return
     */
    public CountdownFailureException getException() {
        return ex;
    }

    /**
     * 
     * @return
     */
    public String getText() {
        return text;
    }

}
