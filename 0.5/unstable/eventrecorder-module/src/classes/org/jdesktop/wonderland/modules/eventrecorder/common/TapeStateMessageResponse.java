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

package org.jdesktop.wonderland.modules.eventrecorder.common;

import java.util.Set;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A message to respond with the state of tapes... i.e. the tapes that are
 * available, their state and the selected tape.
 * @author Bernard Horan
 */
public class TapeStateMessageResponse extends ResponseMessage {

    public enum TapeStateAction {

        TAPE_STATE,
        FAILED
    };
    private TapeStateAction action;
    private Tape selectedTape;
    private Set<Tape> tapes;

    /**
     * Constructor, passing in the message id of the originating message
     * @param messageID id of the originating message
     */
    public TapeStateMessageResponse(MessageID messageID) {
        super(messageID);
    }

    /**
     * Create a message that contains the tapes available and the selected tape
     * @param messageID the id of the originating message
     * @param serverState the serverState of the cell
     * @return a response message
     */
    public static TapeStateMessageResponse tapeStateMessage(MessageID messageID, EventRecorderCellServerState serverState) {
        TapeStateMessageResponse tsm = new TapeStateMessageResponse(messageID);
        tsm.action = TapeStateAction.TAPE_STATE;
        tsm.selectedTape = serverState.getSelectedTape();
        tsm.tapes = serverState.getTapes();
        return tsm;
    }

    /**
     * Create a message that indicates that we failed to get information
     * about the tapes available
     * @param messageID the id of the originating message
     * @return a response message
     */
    public static TapeStateMessageResponse tapeStateFailedMessage(MessageID messageID) {
        TapeStateMessageResponse tsm = new TapeStateMessageResponse(messageID);
        tsm.action = TapeStateAction.FAILED;
        return tsm;
    }

    public Set<Tape> getTapes() {
        return tapes;
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public TapeStateAction getAction() {
        return action;
    }
}
