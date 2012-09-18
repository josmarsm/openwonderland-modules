/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.xuggle.xuggler.IPixelFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 * A drawing surface implementation for video that converts data directly
 * into the texture memory used by JME / JOGL. No intermediate copies required.
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class VideoDrawingSurface extends VideoDrawingTexture 
        implements DrawingSurface
{
    private static final Logger LOGGER =
            Logger.getLogger(VideoDrawingSurface.class.getName());

    private final Set<View2D> visibleViews = new HashSet<View2D>();
    private Window2D window;

    public synchronized Window2D getWindow() {
        return window;
    }

    public synchronized void setWindow(Window2D window) {
        this.window = window;
    }

    @Override
    public synchronized void openVideo(final int videoWidth,
                                       final int videoHeight,
                                       final IPixelFormat.Type videoType)
    {
        super.openVideo(videoWidth, videoHeight, videoType);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getWindow().setSize(videoWidth, videoHeight);
            }
        });
    }

    @Override
    protected synchronized boolean isUpdating() {
        // make sure there is at least one valid view
        if (visibleViews.isEmpty()) {
            return false;
        }

        return super.isUpdating();
        }

    public synchronized void setViewIsVisible(View2D view, boolean visible) {
        if (visible) {
            visibleViews.add(view);
        } else {
            visibleViews.remove(view);
        }

        checkUpdating();
    }
        }
