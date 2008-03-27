/**
 * Project Looking Glass
 *
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.server.cell;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.PlayerState;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellSetup;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoSource;

/**
 *
 * @author nsimpson
 */
public class VideoAppStateMO implements Serializable, ManagedObject {
    private static final Logger logger =
            Logger.getLogger(VideoAppStateMO.class.getName());

    private VideoCellSetup setup;
    private String id;
    private Calendar lastStateChangeDate = null;
    private Calendar controlOwnedDate = null;
    
    public VideoAppStateMO(VideoCellSetup setup) {
        this.setup = setup;
    }

    public void setControllingCell(String id) {
        this.id = id;
        if (id != null) {
            controlOwnedDate = Calendar.getInstance();
        } else {
            controlOwnedDate = null;
        }
    }
    
    public String getControllingCell() {
        return id;
    }
    
    public VideoCellSetup getCellSetup() {
        return setup;
    }
    
    public void setSource(String source) {
        setup.setSource(source);
    }

    public String getSource() {
        return setup.getSource();
    }

    public void setPlayOnLoad(boolean playOnLoad) {
        setup.setPlayOnLoad(playOnLoad);
    }

    public boolean getPlayOnLoad() {
        return setup.getPlayOnLoad();
    }

    public void setSynced(boolean synced) {
        setup.setSynced(synced);
    }

    public boolean getSynced() {
        return setup.getSynced();
    }

    public void setFrameRate(float frameRate) {
        setup.setFrameRate(frameRate);
    }

    public float getFrameRate() {
        return setup.getFrameRate();
    }

    public void setPreferredWidth(double preferredWidth) {
        setup.setPreferredWidth(preferredWidth);
    }

    public double getPreferredWidth() {
        return setup.getPreferredWidth();
    }

    public void setPreferredHeight(double preferredHeight) {
       setup.setPreferredHeight(preferredHeight);
    }

    public double getPreferredHeight() {
        return setup.getPreferredHeight();
    }

    public void setVideoClass(String videoClass) {
        setup.setVideoClass(videoClass);
    }

    public String getVideoClass() {
        return setup.getVideoClass();
    }

    public void setState(PlayerState playerState) {
        setup.setState(playerState);
        lastStateChangeDate = Calendar.getInstance();
    }

    public PlayerState getState() {
        return setup.getState();
    }

    public void setPosition(double position) {
        setup.setPosition(position);
    }

    public double getPosition() {
        return setup.getPosition();
    }

    public void setPanoramic(boolean panoramic) {
        setup.setPanoramic(panoramic);
    }

    public boolean getPanoramic() {
        return setup.getPanoramic();
    }

    public void setPan(float pan) {
        setup.setPan(pan);
    }

    public float getPan() {
        return setup.getPan();
    }

    public void setTilt(float tilt) {
        setup.setTilt(tilt);
    }

    public float getTilt() {
        return setup.getTilt();
    }

    public void setZoom(float zoom) {
        setup.setZoom(zoom);
    }
    
    public float getZoom() {
        return setup.getZoom();
    }

    public VideoSource getVideoInstance() {
        return setup.getVideoInstance();
    }
    
    public Calendar getLastStateChange() {
        if ((getPlayOnLoad() == true) && (lastStateChangeDate == null)) {
            lastStateChangeDate = Calendar.getInstance();
        }
        return lastStateChangeDate;
    }
    
    public long getControlOwnedDuration() {
        long ownedDuration = 0;
        
        if (controlOwnedDate != null) {
            Calendar now = Calendar.getInstance();
            ownedDuration = now.getTimeInMillis() - controlOwnedDate.getTimeInMillis();
        }
        
        return ownedDuration;
    }
}
