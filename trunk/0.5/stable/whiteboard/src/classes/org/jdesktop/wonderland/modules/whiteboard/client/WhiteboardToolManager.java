/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.whiteboard.client;

import com.jme.math.Vector3f;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to manage the selected tool
 *
 * @author bhoran
 * @author nsimpson
 * @author Abhishek Upadhyay
 */
public class WhiteboardToolManager implements WhiteboardCellMenuListener {

    private boolean hudState = false;
    private HUDComponent fontChooserHUD;
    private HUDComponent colorChooserHUD;
    private HUDComponent removeConfirmationHUD;

    public enum WhiteboardTool {

        NEW, SELECTOR, LINE, RECT, RECT_FILL, ELLIPSE, ELLIPSE_FILL, BACKGROUND_IMAGE, CHANGE_FONT, IMAGE, NEW_TEXT, COLOR_CHOOSER
    }

    public enum WhiteboardColor {

        BLACK(Color.BLACK),
        WHITE(Color.WHITE),
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE);
        private final Color c;

        WhiteboardColor(Color c) {
            this.c = c;
        }

        Color getColor() {
            return c;
        }
    }
    private WhiteboardColor currentColor = null;
    private WhiteboardTool currentTool = null;
    private WhiteboardWindow whiteboardWindow;
    private String fontName = "Monospaced";
    private String fontSize = "24";
    private String fontWeight = "normal";
    private String fontStyle = "normal";
    private String globalColor = "#000000";
    private boolean filled = false;

    WhiteboardToolManager(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
        setTool(WhiteboardTool.SELECTOR);
        setColor(WhiteboardColor.BLACK);
    }

    // WhiteboardCellMenuListener methods
    public void remove() {
        if(removeConfirmationHUD!=null) {
            removeConfirmationHUD.setVisible(false);
        }
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        final RemoveConfirmationPanel panel = new RemoveConfirmationPanel();
        removeConfirmationHUD = mainHUD.createComponent(panel);
        removeConfirmationHUD.setPreferredLocation(CompassLayout.Layout.CENTER);
        mainHUD.addComponent(removeConfirmationHUD);
        removeConfirmationHUD.setVisible(true);
        panel.getClearButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                whiteboardWindow.removeCursor();
                String selectedOp = "";
                Enumeration<AbstractButton> eles = panel.getButtonGroup1().getElements();
                while(eles.hasMoreElements()) {
                    AbstractButton b =  eles.nextElement();
                    if (b.isSelected()) selectedOp = b.getText();
                }
                if(selectedOp.equals("Drawing")) {
                    clearDrawing();
                } else if(selectedOp.equals("Background")) {
                    removeBackgroundImage();
                } else if(selectedOp.equals("Drawing and Background")) {
                    whiteboardWindow.newDocument(true);
                }
                removeConfirmationHUD.setVisible(false);
            }
        });
        panel.getCancelButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeConfirmationHUD.setVisible(false);
            }
        });

    }

    public void openDoc() {
        whiteboardWindow.showSVGDialog();
    }

    public void selector() {
        setTool(WhiteboardTool.SELECTOR);
    }

    public void line() {
        setTool(WhiteboardTool.LINE);
    }

    public void rect(boolean filled) {
        if(filled) {
            setTool(WhiteboardTool.RECT_FILL);
        } else {
            setTool(WhiteboardTool.RECT);
        }
        this.filled = filled;
    }

    public void ellipse(boolean filled) {
        if(filled) {
            setTool(WhiteboardTool.ELLIPSE_FILL);
        } else {
            setTool(WhiteboardTool.ELLIPSE);
        }
        this.filled = filled;
    }

    public void backgroundImage() {
        setTool(WhiteboardTool.BACKGROUND_IMAGE);

        //dummy mouse event
        MouseEvent e = new MouseEvent(JmeClientMain.getFrame().getCanvas(), MouseEvent.MOUSE_PRESSED, new Date().getTime(), 0, 0, 0, -99, true);
        whiteboardWindow.getSvgMouseListener().mousePressed(e);
        MouseEvent e1 = new MouseEvent(JmeClientMain.getFrame().getCanvas(), MouseEvent.MOUSE_RELEASED, new Date().getTime(), 0, 0, 0, -99, true);
        whiteboardWindow.getSvgMouseListener().mouseReleased(e1);
    }

    public void colorChooser() {
        //setTool(WhiteboardTool.COLOR_CHOOSER);
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        final ColorChooserPanel colorChooserPanel = new ColorChooserPanel(globalColor);
        colorChooserHUD = mainHUD.createComponent(colorChooserPanel);
        colorChooserHUD.setPreferredLocation(CompassLayout.Layout.NORTH);
        colorChooserHUD.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));
        mainHUD.addComponent(colorChooserHUD);
        if (!colorChooserHUD.isVisible()) {
            colorChooserHUD.setVisible(true);
        }
        colorChooserPanel.getOkButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                globalColor = colorChooserPanel.getSelectedColor();
                colorChooserHUD.setVisible(false);
                whiteboardWindow.updateElementsColor();
            }

        });
        colorChooserHUD.addEventListener(new HUDEventListener() {

            public void HUDObjectChanged(HUDEvent event) {
                HUDEvent.HUDEventType type = event.getEventType();
                if (type == HUDEvent.HUDEventType.CLOSED) {
                    colorChooserHUD.setVisible(false);
                }
            }
        });
    }

    public void changeFont() {
        //setTool(WhiteboardTool.CHANGE_FONT);
        final HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        String fn = fontName;
        String fs = fontSize;
        String fc = globalColor;
        String ft = fontStyle;
        String fw = fontWeight;
        if (whiteboardWindow.getSelection() != null && whiteboardWindow.getSelection().getSelectedElement().getTagName().equals("text")) {
            Element e = whiteboardWindow.getSelection().getSelectedElement();
            fn = e.getAttribute("font-family");
            fs = e.getAttribute("font-size").split("px")[0];
            fc = e.getAttribute("fill");
            ft = e.getAttribute("font-style");
            fw = e.getAttribute("font-weight");
        }
        final FontChooserPanel fontChooserPanel = new FontChooserPanel(fn, fs, fc, ft, fw);
        fontChooserHUD = mainHUD.createComponent(fontChooserPanel);
        fontChooserHUD.setPreferredLocation(CompassLayout.Layout.NORTH);
        fontChooserHUD.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));
        mainHUD.addComponent(fontChooserHUD);
        fontChooserHUD.setVisible(true);
        fontChooserHUD.addEventListener(new HUDEventListener() {

            public void HUDObjectChanged(HUDEvent event) {
                HUDEvent.HUDEventType type = event.getEventType();
                if (type == HUDEvent.HUDEventType.CLOSED) {
                    //mainHUD.removeComponent(fontChooserHUD);
                    fontChooserHUD.setVisible(false);
                    newText();
                }
            }
        });
        fontChooserPanel.getOkButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //save font as current
                fontName = fontChooserPanel.getFontName();
                fontSize = fontChooserPanel.getFontSize();
                fontStyle = fontChooserPanel.getFontStyle();
                fontWeight = fontChooserPanel.getFontWeight();
                //mainHUD.removeComponent(fontChooserHUD);
                fontChooserHUD.setVisible(false);
                whiteboardWindow.updateSelectedTextElements();
                newText();
            }
        });
        fontChooserPanel.getCancelButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //mainHUD.removeComponent(fontChooserHUD);
                fontChooserHUD.setVisible(false);
                newText();
            }
        });
    }

    public void image() {
        setTool(WhiteboardTool.IMAGE);

        //dummy mouse event
        MouseEvent e = new MouseEvent(JmeClientMain.getFrame().getCanvas(), MouseEvent.MOUSE_PRESSED, new Date().getTime(), 0, 0, 0, -99, true);
        whiteboardWindow.getSvgMouseListener().mousePressed(e);
        MouseEvent e1 = new MouseEvent(JmeClientMain.getFrame().getCanvas(), MouseEvent.MOUSE_RELEASED, new Date().getTime(), 0, 0, 0, -99, true);
        whiteboardWindow.getSvgMouseListener().mouseReleased(e1);
    }

    public void removeBackgroundImage() {
        Element bImage = whiteboardWindow.importElement(whiteboardWindow.getDocument().getElementById("bImage"), true);
        if (bImage != null) {
            bImage.setAttributeNS(null, "id", "bImage");
            //bImage.setAttributeNS(null, "width", "800");
            //bImage.setAttributeNS(null, "height", "600");
            bImage.setAttributeNS(null, "x", "0");
            bImage.setAttributeNS(null, "y", "0");
            URL url = getClass().getResource("resources/white-background.png");
            bImage.setAttributeNS(WhiteboardUtils.xlinkNS, "xlink:href", url.toExternalForm());
            whiteboardWindow.updateBackgroundImage(bImage, true);
        }
    }

    int level = 0;
    public void display(Node e, int l) {
        NodeList nodeList = e.getChildNodes();
        System.out.println("n : " + e + " | " + nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            display(n, l++);
        }
    }

    private void clearDrawing() {
        NodeList nodeList = whiteboardWindow.getDocument().getDocumentElement().getChildNodes();
        List<Element> elements = new ArrayList<Element>();
        
        //get all in a list
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n instanceof Element) {
                Element e = (Element) n;
                if (!e.getAttribute("id").equals("bImage")) {
                    elements.add((Element)e);
                }
            }
        }
        
        //remove all from list
        for (Element ele : elements) {
            whiteboardWindow.removeElement(ele, true);
        }
    }
    
    public void sendToBack() {
        changeOrder(false);
    }

    public void bringToFront() {
        changeOrder(true);
    }

    private void changeOrder(boolean front) {
        if (whiteboardWindow.getSelection() != null) {
            NodeList nodeList = whiteboardWindow.getDocument().getDocumentElement().getChildNodes();
            List<Element> elements = new ArrayList<Element>();
            String selElementId = whiteboardWindow.getSelection().getSelectedElement().getAttribute("id");
            Element selectedEle = (Element) whiteboardWindow.getSelection().getSelectedElement().cloneNode(true);

            //get all in a list
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    if (!e.getAttribute("id").equals("bImage") && !e.getAttribute("id").equals(selElementId)) {
                        elements.add((Element) e.cloneNode(true));
                    }
                }
            }

            //remove all from list
            for (Element ele : elements) {
                whiteboardWindow.removeElement(whiteboardWindow.getDocument().getElementById(ele.getAttribute("id")), true);
            }
            whiteboardWindow.removeElement(whiteboardWindow.getSelection().getSelectedElement(), true);//remove selected element

            //add elemnts from the list
            if (!front) {
                whiteboardWindow.addNewElement(selectedEle, true);
            }
            for (Element ele : elements) {
                whiteboardWindow.addNewElement(ele, true);
            }
            if (front) {
                whiteboardWindow.addNewElement(selectedEle, true);
            }
            
        }
    }

    public void newText() {
        setTool(WhiteboardTool.NEW_TEXT);
    }

    public void black() {
        setColor(WhiteboardColor.BLACK);
    }

    public void white() {
        setColor(WhiteboardColor.WHITE);
    }

    public void red() {
        setColor(WhiteboardColor.RED);
    }

    public void green() {
        setColor(WhiteboardColor.GREEN);
    }

    public void blue() {
        setColor(WhiteboardColor.BLUE);
    }

    public void zoomIn() {
        //zoomTo(1.1f, true);
    }

    public void zoomOut() {
        //zoomTo(0.9f, true);
    }

    public void sync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void unsync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void toggleHUD() {
        if (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            whiteboardWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            whiteboardWindow.setDisplayMode(DisplayMode.HUD);
        }
        whiteboardWindow.showControls(true);
    }

    public boolean isOnHUD() {
        return (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD));
    }

    private void setTool(WhiteboardTool newTool) {
        whiteboardWindow.removeCursor();
        if (currentTool == newTool) {
            //no change
            return;
        }
        if (currentTool != null) {
            
            //Untoggle the tool
            whiteboardWindow.deselectTool(currentTool);
            currentTool = null;
        }
        if (newTool != null) {
            //toggle the new tool
            whiteboardWindow.selectTool(newTool);
            currentTool = newTool;
        }
    }

    /**
     * @return the currentTool
     */
    public WhiteboardTool getTool() {
        return currentTool;
    }

    private void setColor(WhiteboardColor newColor) {
        if (currentColor == newColor) {
            // no change
            return;
        }
        if (currentColor != null) {
            // untoggle the color
            whiteboardWindow.deselectColor(currentColor);
            currentColor = null;
        }
        if (newColor != null) {
            // toggle the new tool
            whiteboardWindow.selectColor(newColor);
            currentColor = newColor;
        }
    }

    /**
     * @return the currentColor
     */
    public Color getColor() {
        return currentColor.getColor();
    }

    public String getFontName() {
        return fontName;
    }

    public String getFontSize() {
        return fontSize;
    }

    public String getFontColor() {
        return globalColor;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public String getGlobalColor() {
        return globalColor;
    }

    public boolean isFilled() {
        return filled;
    }

}
