/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class HighlightCellMethod implements ScriptMethodSPI {

    static String[] colors = { "green", "red", "yellow", "blue", "white", "black" };

    static Map<String, ColorRGBA> stringToColors;

    static {
        stringToColors = new HashMap<String, ColorRGBA>();
        stringToColors.put(colors[0], ColorRGBA.green);
        stringToColors.put(colors[1], ColorRGBA.red);
        stringToColors.put(colors[2], ColorRGBA.yellow);
        stringToColors.put(colors[3], ColorRGBA.blue);
        stringToColors.put(colors[4], ColorRGBA.white);
        stringToColors.put(colors[5], ColorRGBA.black);
        
    }
    private Cell cell;
    private Node rootNode;
    private BasicRenderer renderer;
    private boolean highlight;
    private String color;
    public String getFunctionName() {
        return "HighlightCell";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        highlight = ((Boolean)args[1]).booleanValue();
        color = (String)args[2];
        
        renderer = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        rootNode = renderer.getSceneRoot();

    }

    public String getDescription() {
        return "Highlights a cell by making it glow!\n\n"
                + "usage: HighlightCell(cell, true, ''red''); \n\\n"
                + " -supported colors include: red, yellow, green, blue, black, and white";
    }

    public String getCategory() {
        return "graphics";
    }

    public void run() {
        TreeScan.findNode(rootNode, Geometry.class, new ProcessNodeInterface() {
            public boolean processNode(final Spatial s) {
                RenderUpdater updater = new RenderUpdater() {
                    public void update(Object arg0) {                            
                        s.setGlowEnabled(highlight);
                        s.setGlowColor(getColorRGBA(color));
                        s.setGlowScale(new Vector3f(1.3f, 1.3f, 1.3f));
                        ClientContextJME.getWorldManager().addToUpdateList(s);

                    }
                };
                ClientContextJME.getWorldManager().addRenderUpdater(updater, null);
                return true;
            }
        },
        false,
        false);
    }

    private static ColorRGBA getColorRGBA(String s) {
        if(stringToColors.containsKey(s)) {
            return stringToColors.get(s);
        }
        return null;
    }
}
