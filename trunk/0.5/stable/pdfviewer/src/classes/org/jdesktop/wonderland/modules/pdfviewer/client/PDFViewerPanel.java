/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.pdfviewer.client;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.pdfviewer.client.PDFDocumentLoader.PDFDocumentLoaderListener;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.pdfviewer.common.PDFViewerConstants;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;

/**
 * A panel for displaying images from PDF documents
 */
public class PDFViewerPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(PDFViewerPanel.class.getName());
    private PDFDocumentLoader loader;
    private PDFFile currentDocument;
    private PDFPage currentPage;
    private BufferedImage viewImage;
    private int pageNumber = 1;
    private float zoom = 1.0f;
    private boolean synced = true;
    private boolean playing = false;
    private WindowSwing window;
    private HUDComponent messageComponent;

    public enum VIEW_MODE {

        PAGE, GRID
    };
    private VIEW_MODE mode = VIEW_MODE.PAGE;

    public interface Container {

        public void validate();

        public void setHud(boolean enable);
    }
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;

    public PDFViewerPanel(WindowSwing window) {
        this.window = window;
        initComponents();
        loader = new PDFDocumentLoader();
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        // load the PDF viewer's status map
        statusMap = ssc.get(PDFViewerConstants.STATUS_MAP);
    }

    public void setViewMode(VIEW_MODE mode) {
        if (this.mode != mode) {
            this.mode = mode;
            // TODO: trigger mode change
        }
    }

    public VIEW_MODE getViewMode() {
        return mode;
    }

    public void toggleHUD() {
    }

    public void openDocument(final String documentURI) {
        URL documentURL;

        // reset state
        currentDocument = null;
        currentPage = null;
        viewImage = null;
        pageNumber = 1;
        zoom = 1.0f;

        try {
            documentURL = new URL(documentURI);
        } catch (MalformedURLException e) {
            logger.warning("invalid PDF document: " + documentURI + ": " + e);
            return;
        }

        if (messageComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            messageComponent = mainHUD.createMessage("Loading PDF...");
            messageComponent.setPreferredLocation(Layout.NORTHEAST);
            messageComponent.setDecoratable(false);
            mainHUD.addComponent(messageComponent);
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setVisible(true);
            }
        });

        loader.setDocument(documentURL);
        loader.addListener(new PDFDocumentLoaderListener() {

            public void documentLoadStateChanged(URL url, boolean loaded, Exception e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        messageComponent.setVisible(false);
                    }
                });
                if (loaded) {
                    logger.info("successfully loaded: " + url);
                    currentDocument = loader.getPDFFile();
                    resizeToFit();
                    repaint();
                } else {
                    logger.warning("failed to load: " + url + ": " + e);
                    // TODO: display message
                }
            }
        });
        new Thread(loader).start();
    }

    private void resizeToFit() {
        if (currentDocument != null) {
            // get the image of the first page scaled to fit the width of
            // the window
            BufferedImage image = getPageImage(1, getWidth(), getHeight());
            if (image != null) {
                // adjust the window size to fit the page image
                window.setSize(image.getWidth(), image.getHeight());
            }
        }
    }

    /**
     * Display the first page in the document
     */
    public void firstPage() {
        gotoPage(1);
    }

    /**
     * Display the previous page to the currently selected page
     */
    public void previousPage() {
        gotoPage(getPreviousPage());
    }

    /**
     * Display the next page after the currently selected page
     */
    public void nextPage() {
        gotoPage(getNextPage());
    }

    /**
     * Display the last page in the document
     */
    public void lastPage() {
        gotoPage(currentDocument.getNumPages());
    }

    public void gotoPage(int page, boolean notify) {
        // update shared state
        if (isSynced() && notify) {
            statusMap.put(PDFViewerConstants.PAGE_NUMBER,
                    SharedInteger.valueOf(page));
        }
        showPage(page);
    }

    /**
     * Display the specified page
     * @param page the page to display
     */
    public void gotoPage(int page) {
        gotoPage(page, true);
    }

    public void play() {
        if (isSynced() && !playing) {
            playing = true;
            statusMap.put(PDFViewerConstants.SLIDE_SHOW_MODE,
                    SharedBoolean.valueOf(playing));
        }
    }

    public void pause() {
        if (isSynced() && playing) {
            playing = false;
            statusMap.put(PDFViewerConstants.SLIDE_SHOW_MODE,
                    SharedBoolean.valueOf(playing));
        }
    }

    public void zoomIn() {
        zoom *= 1.25f;
        repaint();
    }

    public void zoomOut() {
        zoom *= 0.75f;
        if (zoom < 1.0f) {
            zoom = 1.0f;
        }
        repaint();
    }

    public void sync() {
    }

    public void unsync() {
    }

    public boolean isSynced() {
        return synced;
    }

    /**
     * Get the validity of the specified page in the currently open document
     * @return true if the page is within the range of pages of the current
     * document, false otherwise
     */
    public boolean isValidPage(int p) {
        return ((currentDocument != null) && (p > 0) && (p <= currentDocument.getNumPages()));
    }

    /**
     * Get the page number of the currently selected page
     * @return the page number
     */
    public int getPageNumber() {
        int p = 0;  // an invalid page number

        if ((currentDocument != null) && (currentPage != null)) {
            p = currentPage.getPageNumber();
        }

        return p;
    }

    /**
     * Get the page number of the next page, looping to the first page
     * after the last page
     * @return the next page number
     */
    public int getNextPage() {
        int next = getPageNumber() + 1;
        next = (isValidPage(next)) ? next : 1;
        return next;
    }

    /**
     * Get the page number of the previous page
     * @return the previous page number
     */
    public int getPreviousPage() {
        int prev = getPageNumber() - 1;
        prev = isValidPage(prev) ? prev : 1;
        return prev;
    }

    /**
     * Display the currently selected page
     */
    public void showPage() {
        showPage(getPageNumber());
    }

    /**
     * Display the specified page
     * @param p the page to display
     */
    public void showPage(int p) {
        if (isValidPage(p)) {
            viewImage = getPageImage(p, getWidth(), getHeight());
            repaint();

            // pre-cache the next page
            if (isValidPage(p + 1)) {
                logger.fine("PDF viewer pre-caching page: " + (p + 1));
                currentDocument.getPage(p + 1);
            }
        } else {
            logger.warning("PDF page " + p + " is not a valid page");
        }
    }

    /**
     * Get the image for the specific page
     * @param p the page number
     * @return the image of the specified page
     */
    public BufferedImage getPageImage(int p, int width, int height) {
        BufferedImage image = null;

        try {
            if (isValidPage(p)) {
                currentPage = currentDocument.getPage(p, true);
                double pw = currentPage.getWidth();
                double ph = currentPage.getHeight();

                // request a page that fits the width of the viewer and has
                // the correct aspect ratio
                int rw = width;
                int rh = (int) (ph * ((double) width / (double) pw));

                Image img = currentDocument.getPage(p).getImage(rw, rh, null, null, true, true);

                if (img != null) {
                    // convert the page image into a buffered image
                    image = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(img, 0, 0, rw, rh, null);
                } else {
                    logger.warning("PDF viewer failed to get image for page: " + p);
                }
                logger.fine("page size: " + pw + "x" + ph);
                logger.fine("image size: " + image.getWidth() + "x" + image.getHeight());
            }
        } catch (Exception e) {
            logger.severe("PDF viewer failed to get page image: " + e);
        }

        return image;
    }

    public BufferedImage getGridImage(int width, int height) {
        return null;
    }

    public BufferedImage getImage(int width, int height) {
        BufferedImage image = null;

        if (mode == VIEW_MODE.PAGE) {
            image = getPageImage(pageNumber, width, height);
        } else if (mode == VIEW_MODE.GRID) {
            image = getGridImage(width, height);
        }

        return image;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // size of app which page will be scaled to fit
        double appWidth = (double) this.getWidth();
        double appHeight = (double) this.getHeight();

        if (viewImage == null) {
            viewImage = getImage(getWidth(), getHeight());
        }

        if (viewImage != null) {
            g2.clearRect(0, 0, getWidth(), getHeight());
            g2.scale(zoom, zoom);
            g2.drawImage(viewImage, 0, 0, viewImage.getWidth(), viewImage.getHeight(), null);
        } else {
            logger.finest("no page image!");
            g2.clearRect(0, 0, (int) appWidth, (int) appHeight);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 640, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 480, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
