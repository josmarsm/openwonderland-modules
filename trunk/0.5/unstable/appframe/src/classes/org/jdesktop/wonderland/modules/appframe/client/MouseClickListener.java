/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import java.awt.Color;
import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import static org.jdesktop.wonderland.modules.appframe.client.MouseClickListener.HIGHLIGHT_COLOR;
import static org.jdesktop.wonderland.modules.appframe.client.MouseClickListener.HIGHLIGHT_SCALE;

/**
 * Base listener for mouse events. This listener highlights the selected object
 * whenever the mouse is over it. Subclasses can respond to clicks by
 * overwriting computeEvent() or commitEvent().
 */
public abstract class MouseClickListener extends EventClassListener {

    public static ColorRGBA HIGHLIGHT_COLOR = new ColorRGBA(1.0f, 0, 0, 0.5f);
    public static Vector3f HIGHLIGHT_SCALE = new Vector3f(1.1f, 1.1f, 1.1f);

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{
                    MouseEnterExitEvent3D.class,
                    MouseButtonEvent3D.class,};
    }

    @Override
    public boolean consumesEvent(Event event) {
        if (!super.consumesEvent(event)) {
            return false;
        }

        if (event instanceof MouseButtonEvent3D) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            // mbe.getPickDetails().g;

            return mbe.isClicked()
                    && mbe.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1;
        } else {
            return true;
        }
    }

    @Override
    public void commitEvent(Event event) {
        if (event instanceof MouseEnterExitEvent3D) {
            MouseEnterExitEvent3D meee = (MouseEnterExitEvent3D) event;

            if (meee.isEnter()) {
                highlight(meee.getEntity(), true);
            } else {
                highlight(meee.getEntity(), false);
            }
        }
    }

    public void highlight(final Entity entity, final boolean enabled) {
        try {
            RenderComponent rc = entity.getComponent(RenderComponent.class);
            TreeScan.findNode(rc.getSceneRoot(), Geometry.class, new ProcessNodeInterface() {

                public boolean processNode(final Spatial s) {
                    s.setGlowEnabled(enabled);
                    float[] color = HIGHLIGHT_COLOR.getColorArray();

                    ColorRGBA newColor = new ColorRGBA();
                    if ((color[0] + color[1] + color[2]) == 765) {
                        newColor = new ColorRGBA(color[0], color[1], color[2], color[3]);
                    } else if ((color[0] + color[1]) == 510) {
                        color[2] = (1 - (color[2] / 255)) * 255;
                        newColor = new ColorRGBA(color[0], color[1], 0, color[2]);
                    } else if ((color[0] + color[2]) == 510) {
                        color[1] = (1 - (color[1] / 255)) * 255;
                        newColor = new ColorRGBA(color[0], 0, color[2], color[1]);
                    } else if ((color[1] + color[2]) == 510) {
                        color[0] = (1 - (color[0] / 255)) * 255;
                        newColor = new ColorRGBA(0, color[1], color[2], color[0]);
                    } else if (color[0] == 255) {
                        newColor = new ColorRGBA(color[0], 0, 0, (1 - (color[2] / 255)) * 255);
                    } else if (color[1] == 255) {
                        newColor = new ColorRGBA(0, color[1], 0, (1 - (color[2] / 255)) * 255);

                    } else if (color[2] == 255) {
                        newColor = new ColorRGBA(0, 0, color[2], (1 - (color[1] / 255)) * 255);
                    } else {
                        newColor = HIGHLIGHT_COLOR;
                    }

                    s.setGlowColor(newColor);
                    s.setGlowScale(HIGHLIGHT_SCALE);
                    ClientContextJME.getWorldManager().addToUpdateList(s);

                    return true;
                }
            }, false, false);
        } catch (Exception ei) {
            ei.printStackTrace();
        }
    }
}
