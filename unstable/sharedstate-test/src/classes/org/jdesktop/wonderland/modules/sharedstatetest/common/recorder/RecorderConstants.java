/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.sharedstatetest.common.recorder;

/**
 *
 * @author jkaplan
 */
public class RecorderConstants {
    /** the name of the map where we store status */
    public static final String STATUS_MAP = "audiorecorder-status";

    /** the name of the map where we store the set of available tapes */
    public static final String TAPE_MAP = "audiorecorder-tapes";

    /** the key for the current status of the recorder */
    public static final String STATUS = "status";

    /** the key for the current owner (the person who most recently started
     *  playing or recording on this recorder)
     */
    public static final String OWNER = "owner";

    /** the key for the current tape in use */
    public static final String CURRENT_TAPE = "tape";
}
