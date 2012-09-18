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

import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import twitter4j.Tweet;

/*********************************************
 * UI: controls the open//close of the twitter panel
 * @author Bernard Horan
 */

public class TwitterPanelUI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("uk/ac/essex/wonderland/modules/twitter/client/resources/Bundle");

    /** The Swing panel. */
    private TwitterPanel twitterPanel;

    /** The HUD. */
    private HUD mainHUD;

    /** The HUD component for the panel. */
    private HUDComponent hudComponent;

    public TwitterPanelUI (final TwitterCell cell) {

        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    twitterPanel = new TwitterPanel(cell);
                    hudComponent = mainHUD.createComponent(twitterPanel);
                    hudComponent.setPreferredLocation(Layout.NORTHEAST);
                    hudComponent.setName(bundle.getString("TWITTER CONTROL PANEL"));
                    mainHUD.addComponent(hudComponent);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create twitter panel", ex);
        }

        

         try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Cannot add hud component to main hud", ex);
        }

    }

    /** Control the visibility of the window.
     * @param visible if true, show the hud, otherwise hide it
     */
    public void setVisible (final boolean visible) {
        try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    hudComponent.setVisible(visible);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Cannot add hud component to main hud", ex);
        }
        
    }

    void addTweet(Tweet tweet) {
        twitterPanel.addTweet(tweet);
    }

    void setQueryString(String query) {
        twitterPanel.setQueryString(query);
    }

    
}
