/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.MirroredStickyNoteCellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;

/**
 *
 * @author ryan
 */
@CellFactory
public class MirroredStickyNoteCellFactory extends GenericStickyNoteCellFactory
implements CellFactorySPI {

	@Override
	public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
		MirroredStickyNoteCellServerState state = new MirroredStickyNoteCellServerState();
		state.setNoteType(StickyNoteTypes.GENERIC);
		//HACK
		Map<String, String> metadata = new HashMap();
		metadata.put("sizing-hint",  "2.0");
		state.setMetaData(metadata);
		state.setName("StickyNote");

		return (T) state;
	}

        @Override
        public String getDisplayName() {
            return "Mirrored Sticky Note";
        }
}
