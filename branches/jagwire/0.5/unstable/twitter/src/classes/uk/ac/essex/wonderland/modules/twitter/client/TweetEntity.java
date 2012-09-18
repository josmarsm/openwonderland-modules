/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.twitter.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.animationbase.client.interpolators.FloatInterpolator;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelineScenario.Sequence;
import org.pushingpixels.trident.callback.TimelineScenarioCallback;
import org.pushingpixels.trident.ease.Spline;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;
import twitter4j.Tweet;

/**
 * A visual Entity that displays the contents of a tweet
 * 
 * @author Bernard Horan
 */
public class TweetEntity extends Entity {
    public static float Y_OFFSET = 1.25f;
    public static float Z_OFFSET = -0.5f;

    // The Cell to display the tweet
    private Cell cell = null;

    // Listener for changes in the transform of the cell
    private TransformChangeListener updateListener = null;

    // True if the tweet Entity is visible, false if not
    private boolean isVisible = false;
    private final Sequence tweetScenario;

    private static int POINT_INDEX = 0;
    private final static List<Point> POINT_ARRAY;

    static {
        POINT_ARRAY = new ArrayList<Point>();
        POINT_ARRAY.add(new Point(-4,0));
        POINT_ARRAY.add(new Point(-2,0));
        POINT_ARRAY.add(new Point(2,0));
        POINT_ARRAY.add(new Point(4,0));
    }


    public TweetEntity(Cell cell, Tweet tweet) {
        super("Tweet for " + tweet.getText());

        // Create a new Node that serves as the root node of
        // scene graph
        this.cell = cell;
        final Node tweetNode = new TweetNode(tweet, 0.01f);
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(tweetNode);
        this.addComponent(RenderComponent.class, rc);

        final BoundingVolume bounds = cell.getLocalBounds();

        // Fetch the world translation for the root node of the cell and set
        // the translation for the tweet node
        Vector3f translation = cell.getWorldTransform().getTranslation(null);
	translation = translation.add(bounds.getCenter());
        translation = translation.add(new Vector3f(0f, Y_OFFSET, Z_OFFSET));//offset
        tweetNode.setLocalTranslation(translation);

        // Listen for changes to the cell's translation and apply the same
        // update to the tweet node. We also re-set the size
        // of the bounds: this handles the case where the bounds of the
        // scene graph has changed and we need to update the bounds viewer
        // accordingly.
        updateListener = new TransformChangeListener() {
            public void transformChanged(final Cell cell, ChangeSource source) {
                // We need to perform this work inside a proper updater, to
                // make sure we are MT thread safe
                final WorldManager wm = ClientContextJME.getWorldManager();
                RenderUpdater u = new RenderUpdater() {
                    public void update(Object obj) {
                        CellTransform transform = cell.getWorldTransform();
                        Vector3f translation = transform.getTranslation(null);
		        translation = translation.add(bounds.getCenter());
                        tweetNode.setLocalTranslation(translation);
                        wm.addToUpdateList(tweetNode);
                    }
                };
                wm.addRenderUpdater(u, this);
            }
        };
        cell.addTransformChangeListener(updateListener);

        Timeline openingTimeline = new Timeline(tweetNode);
        tweetNode.setLocalScale(0.25f);
        Point targetPoint = POINT_ARRAY.get(POINT_INDEX % POINT_ARRAY.size());
        POINT_INDEX ++;
        Vector3f targetTranslation = translation.add(new Vector3f(targetPoint.x, 4, targetPoint.y));
        openingTimeline.addPropertyToInterpolate("localTranslation", translation, targetTranslation, new Vector3fInterpolator());
        openingTimeline.addPropertyToInterpolate("scale", 0.25f, 1.00f, new FloatInterpolator());
        openingTimeline.setEase(new Spline(0.8f));
        openingTimeline.setDuration(8000);

        Timeline closingTimeline = new Timeline(tweetNode);
        closingTimeline.addPropertyToInterpolate("scale", 1.00f, 0.25f, new FloatInterpolator());
        closingTimeline.setDuration(2000);

        TimelineScenarioCallback scenarioCallback = new TimelineScenarioCallback(){

            public void onTimelineScenarioDone() {
                setVisible(false);
            }           
        };

        tweetScenario = new Sequence();
        tweetScenario.addScenarioActor(openingTimeline);
        tweetScenario.addScenarioActor(closingTimeline);
        tweetScenario.addCallback(scenarioCallback);

    }

    /**
     * Sets whether the tweet node is visible (true) or invisible (false).
     *
     * @param visible True to make the tweet node visible, false to not
     */
    public synchronized void setVisible(boolean visible) {
        WorldManager wm = ClientContextJME.getWorldManager();

        // If we want to make the tweet node visible and it already is not
        // visible, then make it visible. We do not need to put add/remove
        // Entities on the MT Game render thread, they are already thread safe.
        if (visible == true && isVisible == false) {
            wm.addEntity(this);
            isVisible = true;
            tweetScenario.play();
            return;
        }

        // If we want to make the tweet node invisible and it already is
        // visible, then make it invisible
        if (visible == false && isVisible == true) {
            wm.removeEntity(this);
            isVisible = false;
            return;
        }
    }

    public void dispose() {
        // First, to make sure the tweet node is no longer visible
        setVisible(false);

        // Clean up all of the listeners so this class gets properly garbage
        // collected.
        cell.removeTransformChangeListener(updateListener);
        updateListener = null;
    }

    static class Vector3fInterpolator implements PropertyInterpolator<Vector3f> {
		@Override
		public Class getBasePropertyClass() {
                    return Vector3f.class;
		}

		@Override
		public Vector3f interpolate(Vector3f from, Vector3f to, float timelinePosition) {
                    return new Vector3f(from.x + (timelinePosition * (to.x - from.x)),
                            from.y + (timelinePosition * (to.y - from.y)),
                            from.z + (timelinePosition * (to.z - from.z)));
		}
	}

}
