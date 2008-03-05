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
package org.jdesktop.lg3d.wonderland.videomodule.common;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DCellSetup;

/**
 *
 * @author nsimpson
 */
public class VideoModuleCellSetup extends SharedApp2DCellSetup {
    
    private static final Logger logger =
            Logger.getLogger(VideoModuleCellSetup.class.getName());
    
    private String source;
    private String videoClass;
    private VideoSource videoInstance;
    private float frameRate = 2.0f;  // frames per second
    private boolean playOnLoad = false;
    private boolean syncPlayback = true;
    private double preferredWidth = 0;  // none, use media width
    private double preferredHeight = 0; // none, use media height
    
    public VideoModuleCellSetup() {
        this(null, null);
    }
    
    public VideoModuleCellSetup(String appName, Matrix4f viewRectMat) {
        super(appName, viewRectMat);
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setPlayOnLoad(boolean playOnLoad) {
        this.playOnLoad = playOnLoad;
    }
    
    public boolean getPlayOnLoad() {
        return playOnLoad;
    }

    public void setSyncPlayback(boolean syncPlayback) {
        this.syncPlayback = syncPlayback;
    }
    
    public boolean getSyncPlayback() {
        return syncPlayback;
    }
    
    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }
    
    public float getFrameRate() {
        return frameRate;
    }
    
    public void setPreferredWidth(double preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    public double getPreferredWidth() {
        return preferredWidth;
    }
    
    public void setPreferredHeight(double preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    public double getPreferredHeight() {
        return preferredHeight;
    }    
    
    public void setVideoClass(String videoClass) {
        this.videoClass = videoClass;
        try {
            Class clss = Class.forName(this.videoClass);
            Constructor cons = clss.getConstructor(new Class[] { String.class });
            videoInstance = (VideoSource)cons.newInstance(this.source);
        } catch (ClassNotFoundException e) {
            logger.warning("unrecognized camera class: " + this.videoClass + ": " + e);
        } catch (Exception e) {
            logger.warning("failed to create instance of: " + this.videoClass + ": " + e);
        }
    }
    
    public String getVideoClass() {
        return videoClass;
    }
    
    public VideoSource getVideoInstance() {
        return videoInstance;
    }
}
