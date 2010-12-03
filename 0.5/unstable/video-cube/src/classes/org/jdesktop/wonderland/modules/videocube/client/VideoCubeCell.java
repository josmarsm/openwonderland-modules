/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.videocube.client;

import java.util.EnumMap;
import java.util.Map;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.videocube.client.VideoCubeRenderer.Side;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerImpl;

public class VideoCubeCell extends Cell {
    private static final Map<Side, String> videos = 
            new EnumMap<Side, String>(Side.class);
    static {
        videos.put(Side.FRONT, "http://dl.dropbox.com/u/1937997/Video%20Player/tronlegacy-tsr1_480p.mov");
        videos.put(Side.BACK, "wlcontent://users/Jonathan/SocialNetwork.mov");
        videos.put(Side.LEFT, "wlcontent://users/Jonathan/LifeDuringWartime.mov");
        videos.put(Side.RIGHT, "wlcontent://users/Jonathan/TrueGrit.mov");
        videos.put(Side.TOP, "wlcontent://users/Jonathan/Freakonomics.mov");
        videos.put(Side.BOTTOM, "wlcontent://users/Jonathan/Megamind.mov");
    }

    public VideoCubeCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false) {
           
        } else if (status == CellStatus.ACTIVE && increasing == true) {
            for (Map.Entry<Side, String> e : videos.entrySet()) {
                VideoPlayerImpl player = new VideoPlayerImpl();
                VideoCubeRenderer renderer =
                        (VideoCubeRenderer) getCellRenderer(RendererType.RENDERER_JME);
                renderer.attach(e.getKey(), player);
                player.openMedia(e.getValue());
            }
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new VideoCubeRenderer(this);
        } else {
            return super.createCellRenderer(rendererType);
        }
    }
}
