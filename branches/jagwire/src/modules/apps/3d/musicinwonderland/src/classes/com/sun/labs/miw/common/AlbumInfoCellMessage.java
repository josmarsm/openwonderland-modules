/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.common;

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 * Request information about an album
 * @author Jeff Moguillansky
 */
public class AlbumInfoCellMessage extends CellMessage {
    public AlbumInfoCellMessage() {
        super();
    }
    
    public AlbumInfoCellMessage(CellID cellID, String albumName) {
        super(cellID);
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
        albumName = DataString.value(data);
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        dataElements.add(new DataString(albumName));
    }
    private String albumName;
}
