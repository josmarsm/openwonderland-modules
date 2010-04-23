/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.audiorecorder.client;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

/**
 * Extension of DefaultListSelectionModel for use in the selection of tapes
 * @author Bernard Horan
 */
public class TapeListSelectionModel extends DefaultListSelectionModel {
    
    public TapeListSelectionModel() {
        super();
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    public void setSelectedTapeIndex(int index) {
        setSelectionInterval(index, index);
    }

}
