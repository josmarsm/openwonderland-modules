/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.cmu.client.cell;

import java.io.File;
import org.jdesktop.wonderland.modules.cmu.player.ProgramPlayer;

/**
 *
 * @author kevin
 */
public class StreamTest {
    // ID which should never be used by any visuals.
    //public static final int UNUSED_ID = -1;


    static public void main(String[] args) {
        ProgramPlayer p = new ProgramPlayer();
        p.setFile(new File("/home/kevin/Documents/Employment/Sun/src/alice-module/alice-files/girl-house.a3p"));
        new CMUCell(null, null);
    }

}

