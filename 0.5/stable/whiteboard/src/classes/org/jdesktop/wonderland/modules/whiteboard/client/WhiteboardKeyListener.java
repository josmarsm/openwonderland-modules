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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.logging.Logger;

import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;
import org.w3c.dom.Element;

/**
 * Simple class that implements KeyListener
 *
 * @author bhoran
 * @author Abhishek Upadhyay
 */
public class WhiteboardKeyListener implements KeyListener {

    private static final int DELTA = 10;
    private WhiteboardWindow whiteboardWindow;
    private StringBuffer text = new StringBuffer();
    private StringBuffer tempText = new StringBuffer();
    private volatile int cursorPos = 0;
    private static final Logger LOGGER
            = Logger.getLogger(WhiteboardKeyListener.class.getName());

    private enum ACTION {

        ADD,
        REMOVE,
        MOVE_LEFT,
        MOVE_RIGHT,
        HOME,
        END,
        SPACE
    };

    WhiteboardKeyListener(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
    }

    /**
     * Process a key press event
     *
     * @param evt the key press event
     */
    public void keyPressed(final KeyEvent evt) {
        whiteboardWindow.getRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                addTextToWhiteboard(evt);
//                LOGGER.warning("key Code : "+evt.getKeyCode()+" | Key Char : "+evt.getKeyChar());
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_SHIFT:
                        whiteboardWindow.setShiftPressed(true);
                        break;
                }
            }
        });
        //svgCanvas.dispatchEvent(evt);
    }

    /**
     * Process a key release event
     *
     * @param evt the key release event
     */
    public void keyReleased(final KeyEvent e) {
        
        Set<WhiteboardSelection> selections = whiteboardWindow.getSelections();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
                if (selections != null) {
                    for(WhiteboardSelection selection : selections) {
                        whiteboardWindow.removeElement(selection.getSelectedElement(), true);
                    }
                    whiteboardWindow.clearSelections();
                }
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                if (selections != null) {
                    for(WhiteboardSelection selection : selections) {
                        moveSelection(selection, 1 - DELTA, 0);
                    }
                }
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                if (selections != null) {
                    for(WhiteboardSelection selection : selections) {
                        moveSelection(selection, DELTA, 0);
                    }
                }
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                if (selections != null) {
                    for(WhiteboardSelection selection : selections) {
                        moveSelection(selection, 0, 1 - DELTA);
                    }
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                if (selections != null) {
                    for(WhiteboardSelection selection : selections) {
                        moveSelection(selection, 0, DELTA);
                    }
                }
                break;
            case KeyEvent.VK_SHIFT:
                whiteboardWindow.setShiftPressed(false);
                break;
            default:
                break;
        }
        //svgCanvas.dispatchEvent(evt);
    }

    private void moveSelection(WhiteboardSelection selection, int deltaX, int deltaY) {
        whiteboardWindow.updateElement(whiteboardWindow.moveElement(selection.getSelectedElement(), deltaX, deltaY), true);
    }

    /**
     * Process a key typed event
     *
     * @param evt the key release event
     */
    public void keyTyped(KeyEvent evt) {
        //svgCanvas.dispatchEvent(evt);
    }

    private void addTextToWhiteboard(KeyEvent e) {
        WhiteboardTool currentTool = whiteboardWindow.getCurrentTool();
        if (currentTool == WhiteboardTool.NEW_TEXT) {
            Element ce = whiteboardWindow.getCurrentTextElement();
            if (ce != null) {
                Element eleById = whiteboardWindow.getDocument().getElementById(ce.getAttribute("id"));
                if (eleById != null) {
                    final Element currentTextElement = whiteboardWindow.importElement(eleById, true);
                    if (currentTextElement != null) {
                        final Element newTextElement = (Element) currentTextElement.cloneNode(true);
                        if ((e.getKeyCode() >= 65 && e.getKeyCode() <= 90) //for a to z
                                || (e.getKeyCode() >= 48 && e.getKeyCode() <= 57) //for 0 to 9
                                || (e.getKeyCode() >= 96 && e.getKeyCode() <= 105) //for numpade0 to numpad9
                                || (e.getKeyCode() >= 106 && e.getKeyCode() <= 111) //for *,+,-,/,.
                                || (e.getKeyCode() >= 186 && e.getKeyCode() <= 192) //for special chars
                                || (e.getKeyCode() >= 219 && e.getKeyCode() <= 222) //for special chars
                                || (e.getKeyCode() >= 44 && e.getKeyCode() <= 47) //for special chars
                                || (e.getKeyCode() >= 91 && e.getKeyCode() <= 93) //for special chars
                                || (e.getKeyCode() >= 35 && e.getKeyCode() <= 36) //for special chars
                                || (e.getKeyCode() == 37) //for special chars
                                || (e.getKeyCode() == 39) //for special chars
                                || (e.getKeyCode() == 59) //for special chars
                                || (e.getKeyCode() == 222) //for special chars
                                || (e.getKeyCode() == 61) //for special chars
                                || (e.getKeyCode() == 32) //for space
                                || (e.getKeyCode() == 9) //for tab
                                || (e.getKeyCode() == 8) //for backspace
                                || (e.getKeyCode() == 127) //for del
                                || (e.getKeyCode() == KeyEvent.VK_ENTER) //for enter
                                ) {
                            try {
                                String textContent = newTextElement.getTextContent();
                                if (whiteboardWindow.isNewText()) {
                                    text = new StringBuffer(textContent);
                                    tempText = new StringBuffer(textContent);
                                    cursorPos = whiteboardWindow.getInitialCursorPos();
                                    whiteboardWindow.setNewText(false);
                                }
                                if (e.getKeyCode() == 37) {
                                    //left arrow
                                    cursorPos--;
                                    changeCursorPosition(ACTION.MOVE_LEFT);
                                } else if (e.getKeyCode() == 39) {
                                    //right arrow
                                    cursorPos++;
                                    changeCursorPosition(ACTION.MOVE_RIGHT);
                                } else if (e.getKeyCode() == 35) {
                                    //end
                                    cursorPos = text.length();
                                    changeCursorPosition(ACTION.END);
                                } else if (e.getKeyCode() == 36) {
                                    //home
                                    cursorPos = 0;
                                    changeCursorPosition(ACTION.HOME);
                                } else if (e.getKeyCode() == 8) {
                                    //backspace
                                    if (cursorPos == 0) {
                                        return;
                                    }
                                    text.deleteCharAt(cursorPos - 1);
                                    if (cursorPos == 1) {
                                        tempText.deleteCharAt(0);
                                    } else {
                                        tempText.deleteCharAt(cursorPos - 1);
                                    }
                                    cursorPos--;
                                    newTextElement.setAttribute("text-decoration", "underline");
                                    newTextElement.setTextContent(text.toString());
                                    whiteboardWindow.updateTextElement(newTextElement, false);
                                    changeCursorPosition(ACTION.REMOVE);
                                } else if (e.getKeyCode() == 127) {
                                    //del key
                                    if (cursorPos != text.length()) {
                                        text.deleteCharAt(cursorPos);
                                        tempText.deleteCharAt(cursorPos);
                                    }
                                    newTextElement.setAttribute("text-decoration", "underline");
                                    newTextElement.setTextContent(text.toString());
                                    whiteboardWindow.updateTextElement(newTextElement, false);
                                    changeCursorPosition(ACTION.REMOVE);
                                } else if (e.getKeyCode() == 32) {
                                    //for space
                                    if ((text.length() == 0 && e.getKeyCode() == 32)
                                            || (cursorPos == 0 && e.getKeyCode() == 32)
                                            || (text.length() > 0 && cursorPos > 0 && e.getKeyCode() == 32 && text.charAt(cursorPos - 1) == ' ')) {
                                        return;
                                    }
                                    text.insert(cursorPos, e.getKeyChar());
                                    tempText.append(e.getKeyChar());
                                    cursorPos++;
                                    newTextElement.setAttribute("text-decoration", "underline");
                                    newTextElement.setTextContent(text.toString());
                                    whiteboardWindow.updateTextElement(newTextElement,false);
                                    changeCursorPosition(ACTION.SPACE);
                                } else if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                                    //save the element
                                    newTextElement.setAttribute("text-decoration", "none");
                                    whiteboardWindow.updateTextElement(newTextElement,false);
                                    if(whiteboardWindow.isTextExist()) {
                                        whiteboardWindow.updateTextElement(newTextElement);
                                    } else {
                                        whiteboardWindow.addNewTextElement(newTextElement);
                                        whiteboardWindow.setTextExist(true);
                                    }
                                    whiteboardWindow.emptyRunnableQueue();
                                } else {
                                    //any other key
                                    text.insert(cursorPos, e.getKeyChar());
                                    tempText.append(e.getKeyChar());
                                    cursorPos++;
                                    newTextElement.setAttribute("text-decoration", "underline");
                                    newTextElement.setTextContent(text.toString());
                                    whiteboardWindow.updateTextElement(newTextElement,false);
                                    changeCursorPosition(ACTION.ADD);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void changeCursorPosition(ACTION action) {
        try {
            //calculate text width
            Element tempEle = (Element) whiteboardWindow.getCurrentTextElement().cloneNode(true);
            try {
                tempText.delete(cursorPos, tempText.length());
            } catch (StringIndexOutOfBoundsException seob) {
                System.out.println("can't move cursor.");
                //re initialize temp msg
                tempText = null;
                tempText = new StringBuffer(text.toString());
                switch (action) {
                    case MOVE_LEFT:
                        cursorPos++;
                        break;
                    case MOVE_RIGHT:
                        cursorPos--;
                        break;
                }
                return;
            }

            tempEle.setTextContent(tempText.toString());
            tempEle.setAttribute("id", "tempText");
            whiteboardWindow.addNewElement(tempEle, false);

            //re initialize temp msg
            tempText = null;
            tempText = new StringBuffer(text.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
