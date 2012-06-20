/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
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
package uk.ac.essex.wonderland.modules.countdowntimer.client;

import com.jme.scene.Node;
import java.awt.Color;
import java.awt.Font;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import uk.ac.essex.wonderland.modules.countdowntimer.common.Time;

/**
 * @author Bernard Horan
 */
public class CountdownRenderer extends BasicRenderer {
    private Node cellRoot = new Node("Countdown Cell Root");

    TextLabel2D textLabel;
    
    public CountdownRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {
        Font font = Font.decode("Courier BOLD");
        textLabel = new TextLabel2D("00:00", new Color(1f, 1f, 1f), new Color(0f, 0f, 0f), 1f, true, font);
        textLabel.setShadowOffsetX(0);
        textLabel.setShadowOffsetY(0);
        Time t = ((CountdownCell)getCell()).getTime(); 
        if (t != null) {
            updateTime(t);
        }
        cellRoot.attachChild(textLabel);
        return cellRoot;
    }

    void updateTime(Time t) {
        textLabel.setText(t.toString(), new Color(1f, 1f, 1f), new Color(0f, 0f, 0f));
    }

    
}
