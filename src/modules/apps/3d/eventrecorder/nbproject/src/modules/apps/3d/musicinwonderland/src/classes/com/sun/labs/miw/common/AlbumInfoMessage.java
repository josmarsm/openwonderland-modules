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
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 * A message that contains information about an album
 * @author Jeff Moguillansky
 */
public class AlbumInfoMessage extends Message {
    private String albumName;
    private MIWAlbum album;
    
    public AlbumInfoMessage() {
        this (null,null);
    }

    public AlbumInfoMessage(String albumName, MIWAlbum album) {
        this.albumName = albumName;
        this.album = album;
    }
    
    public String getAlbumName() {
        return albumName;
    }
    public MIWAlbum getAlbumInfo() {
        return album;
    }

    protected void extractMessageImpl(ByteBuffer data) {
        albumName = DataString.value(data);
        album = MIWAlbum.value(data);
    }

    protected void populateDataElements() {
        dataElements.clear();
        dataElements.add(new DataString(albumName));
        album.toDataElements(dataElements);
    }    
}
