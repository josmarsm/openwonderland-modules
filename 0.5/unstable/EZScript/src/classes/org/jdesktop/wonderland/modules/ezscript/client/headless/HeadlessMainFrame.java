/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.HeadlessException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import org.jdesktop.wonderland.client.jme.MainFrame;
import org.jdesktop.wonderland.client.jme.MainFrame.ServerURLListener;

/**
 *
 * @author ryan
 */
    class FakeMainFrame implements MainFrame {

        private JFrame frame;
        private JPanel canvasPanel;
        private Canvas canvas;
        private static final Logger logger = Logger.getLogger(FakeMainFrame.class.getName());

        public FakeMainFrame() {
            try {
                frame = new JFrame();
            } catch (HeadlessException he) {
                // ignore
                logger.log(Level.INFO, "Running in headless mode");
            }
            canvasPanel = new JPanel(new BorderLayout());
            canvas = new Canvas();

            canvasPanel.add(canvas, BorderLayout.CENTER);

            if (frame != null) {
                frame.setContentPane(canvasPanel);
            }
        }

        public JFrame getFrame() {
            return frame;
        }

        public Canvas getCanvas() {
            return canvas;
        }

        public JPanel getCanvas3DPanel() {
            return canvasPanel;
        }

        public void setMessageLabel(String msg) {
            //ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToMenu(JMenu menu, JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToFileMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToFileMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToEditMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToEditMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToViewMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToViewMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToToolsMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToToolsMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToPlacemarksMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToPlacemarksMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToWindowMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToWindowMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToHelpMenu(JMenuItem menuItem) {
            // ignore
        }

        /**
         * {@inheritDoc}
         */
        public void addToHelpMenu(JMenuItem menuItem, int index) {
            // ignore
        }

        public void setServerURL(String serverURL) {
            // ignore
        }

        public void addServerURLListener(ServerURLListener listener) {
            // ignore
        }

        public void removeFromFileMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromEditMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromViewMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromToolsMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromPlacemarksMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromWindowMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addToInsertMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addToInsertMenu(JMenuItem menuItem, int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromInsertMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void connected(boolean connected) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDesiredFrameRate(int desiredFrameRate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addToCameraChoices(JRadioButtonMenuItem cameraMenuItem, int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromCameraChoices(JRadioButtonMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFromHelpMenu(JMenuItem menuItem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

