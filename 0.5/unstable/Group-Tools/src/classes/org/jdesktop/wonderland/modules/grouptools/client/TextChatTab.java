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

package org.jdesktop.wonderland.modules.grouptools.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * JPanel used to label text chat tabs. Provides close buttons.
 *
 * Taken heavily after this sun example: http://java.sun.com/docs/books/tutorial/uiswing/examples/components/TabComponentsDemoProject/src/components/ButtonTabComponent.java
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class TextChatTab extends JPanel {

    private final JTabbedPane pane;
    private boolean closeButtonEnabled = false;
    private JButton closeButton;
    private JLabel label;

    public TextChatTab (final JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.pane = pane;

        // So our colors don't mess up the L&F's color scheme
        // on the tab.
        setOpaque(false);

         //make JLabel read titles from JTabbedPane
        label = new JLabel() {
            String oldText = "";

            public String getText() {
                int i = pane.indexOfTabComponent(TextChatTab.this);
                if (i != -1) {
                    String newText = pane.getTitleAt(i);
                    if(oldText != newText) {
                        // Add a dummy element to force a reflow.
                        JLabel dummy = new JLabel("");
                        add(dummy);
                        remove(dummy);
                        oldText = newText;
                    }

                    return newText;
                }
                return null;
            }
        };

        closeButton = new TabButton();
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        add(label);

        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(TextChatTab.this);
            if (i != -1) {
                pane.remove(i);
            }
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }

                if (getModel().isRollover()) {
                    // Draw an outline on rollover.
                    g2.setColor(Color.DARK_GRAY);
                } else {
                    g2.setColor(Color.GRAY);
                }

                g2.setStroke(new BasicStroke(1));
                g2.drawRect(2, 2, getWidth()-4, getHeight()-4);

                g2.setStroke(new BasicStroke(2));
                g2.drawLine(6, 6, getWidth() - 6, getHeight() - 6);
                g2.drawLine(getWidth() -6, 6, 6, getHeight() - 6);

                g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

//    public void relabel() {
//        label.setText(label.getText());
//        label.invalidate();
//        label.repaint();
//        this.invalidate();
//        this.repaint();
//    }

    public void setCloseButtonEnabled(boolean enabled) {
        if(enabled & !this.closeButtonEnabled) {
            add(closeButton);
        } else if(!enabled & this.closeButtonEnabled) {
            remove(closeButton);
        }

        this.closeButtonEnabled = enabled;
        this.repaint();
    }
}