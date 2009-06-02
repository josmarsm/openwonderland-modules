/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventrecorder.common;

import java.util.Set;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 *
 * @author bh37721
 */
public class TapeStateMessageResponse extends ResponseMessage {



    public enum TapeStateAction {
        TAPE_STATE,
        FAILED
    };

    private TapeStateAction action;
    private Tape selectedTape;
    private Set<Tape> tapes;

    public TapeStateMessageResponse(MessageID messageID) {
        super(messageID);
    }

    public static TapeStateMessageResponse tapeStateMessage(MessageID messageID, EventRecorderCellServerState serverState) {
        TapeStateMessageResponse tsm = new TapeStateMessageResponse(messageID);
        tsm.action = TapeStateAction.TAPE_STATE;
        tsm.selectedTape = serverState.getSelectedTape();
        tsm.tapes = serverState.getTapes();
        return tsm;
    }

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
