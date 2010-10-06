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
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;

/**
 *
 * @author nsimpson
 */
public interface FrameListener {
    public void openVideo(int videoWidth, int videoHeight,
                          IPixelFormat.Type videoFormat);

    public void previewFrame(IVideoPicture frame);
    public void playVideo(FrameQueue queue);
    public void stopVideo();

    public void closeVideo();

    public interface FrameQueue {
        /**
         * Get the next frame in the queue
         * @return the next frame, or null if the current frame is active
         */
        public IVideoPicture nextFrame();
    }
}
