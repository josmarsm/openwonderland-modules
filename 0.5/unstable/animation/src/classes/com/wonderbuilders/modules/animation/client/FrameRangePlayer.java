/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.client;

import com.jmex.model.collada.ColladaAnimation;
import com.jmex.model.collada.ColladaAnimationGroup;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This class is used to control playing specific frame range of animation.
 *
 * @author Vladimir Djurovic
 */
public class FrameRangePlayer implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(FrameRangePlayer.class.getName());
    private static final float ANIMATION_TIME_DIVIDER = 1000f;
    
    private float startTime;
    private float endTime;
    private ColladaAnimationGroup group;
    
    /**
     * Creates new instance.
     * 
     * @param animGroup Collada animation group that will be played
     * @param start index of start key frame
     * @param end index of end key frame
     */
    public FrameRangePlayer(ColladaAnimationGroup animGroup, int start, int end) {
        this.group = animGroup;
        startTime = -1;
        endTime = -1;
        // find times for specified frames
        SortedSet<Float> ftimes = new TreeSet<Float>();
//        try {
            List<ColladaAnimation> anims = group.getAnimations();
            for (ColladaAnimation anim : anims) {
                ftimes.addAll(anim.getKeyFrames().keySet());
            }
            int i = 0;
            for(Float time : ftimes){
                if(startTime > -1 && endTime > -1){
                    break;
                }
                if(i == start){
                    startTime = time;
                    i++;
                    continue;
                }
                if(i == end){
                    endTime = time;
                    i++;
                    continue;
                }
                i++;
            }
//        } catch (Exception ex) {
//            LOGGER.log(Level.SEVERE, "Could not find key frames", ex);
//        }
    }

    @Override
    public void run() {
        List<ColladaAnimation> anims = group.getAnimations();
        for(ColladaAnimation anim : anims){
            anim.setCurrentTime(startTime);
        }
        float time = startTime;
        group.setPlayDirection(ColladaAnimation.PlayDirection.FORWARD);
        group.setLoopMode(ColladaAnimationGroup.LoopMode.NONE);
        group.setPlaying(true);
        while(time < endTime){
          for(ColladaAnimation anim : anims){
              time = anim.getCurrentTime();
          }
          
        }
        // stop after time has elapsed
        group.setPlaying(false);
    }

}
