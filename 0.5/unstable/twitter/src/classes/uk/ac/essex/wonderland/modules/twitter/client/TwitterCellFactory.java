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

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterCellServerState;

/**
 * The cell factory for the twitter cell.
 * 
 */
@CellFactory
public class TwitterCellFactory implements CellFactorySPI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("uk/ac/essex/wonderland/modules/twitter/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    @SuppressWarnings("unchecked")
    public <T extends CellServerState> T getDefaultCellServerState(
            Properties props) {
        TwitterCellServerState state = new TwitterCellServerState();
        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1f,1f,1f);
        BoundingVolumeHint hint = new BoundingVolumeHint(false, box);
        state.setBoundingVolumeHint(hint);
        state.setName(bundle.getString("TWITTER"));

        return (T) state;
    }

    public String getDisplayName() {
        return (bundle.getString("TWITTER VIEWER"));
    }

    public Image getPreviewImage() {
        URL url = TwitterCellFactory.class.getResource("resources/Pigeon.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
