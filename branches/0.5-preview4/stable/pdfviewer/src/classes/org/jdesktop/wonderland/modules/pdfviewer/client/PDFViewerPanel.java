/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.pdfviewer.client.PDFDocumentLoader.PDFDocumentLoaderListener;

/**
 * A panel for displaying images from PDF documents
 *
 * @author nsimpson
 */
public class PDFViewerPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(
            PDFViewerPanel.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/pdfviewer/client/resources/Bundle");
    private String documentURI;
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

    public PDFViewerPanel(WindowSwing window) {
        this.window = window;
        initComponents();
        loader = new PDFDocumentLoader();
    }

    public void openDocument(String documentURI) {
        openDocument(documentURI, 1);
    }

    public void openDocument(final String documentURI, final int pageNumber) {
        URL documentURL;
        this.documentURI = documentURI;
        this.pageNumber = pageNumber;

        // reset state
        currentDocument = null;
        currentPage = null;
        viewImage = null;
        zoom = 1.0f;

        try {
            documentURL = new URL(documentURI);
        } catch (MalformedURLException e) {
            LOGGER.warning("invalid PDF document: " + documentURI + ": " + e);
            return;
        }

        if (messageComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            messageComponent = mainHUD.createMessage(BUNDLE.getString("Loading_PDF..."));
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
                    LOGGER.info("successfully loaded: " + url);
                    currentDocument = loader.getPDFFile();
                    gotoPage(pageNumber);
                    resizeToFit();
                    repaint();
                } else {
                    LOGGER.warning("failed to load: " + url + ": " + e);
                    // TODO: display message
                }
            }
        });
        new Thread(loader).start();
    }

    public String getDocumentURI() {
        return documentURI;
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

    public int getPageCount() {
        return currentDocument.getNumPages();
    }

    /**
     * Display the specified page
     * @param page the page to display
     */
    public void gotoPage(int page) {
        showPage(page);
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
        LOGGER.info("showing page: " + p);
        if (isValidPage(p)) {
            viewImage = getPageImage(p, getWidth(), getHeight());
            repaint();

            // pre-cache the next page
            if (isValidPage(p + 1)) {
                LOGGER.fine("PDF viewer pre-caching page: " + (p + 1));
                currentDocument.getPage(p + 1);
            }
        } else {
            LOGGER.warning("PDF page " + p + " is not a valid page");
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
                LOGGER.fine("page size: " + pw + "x" + ph);

                // request a page that fits the width of the viewer and has
                // the correct aspect ratio
                int rw = width;
                int rh = (int) (ph * ((double) width / (double) pw));

                Image img = currentDocument.getPage(p).getImage(rw, rh, null, null, true, true);

                if (img != null) {
                    // convert the page image into a buffered image
                    image = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);
                    LOGGER.fine("image size: " + image.getWidth() + "x" + image.getHeight());
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(img, 0, 0, rw, rh, null);
                } else {
                    LOGGER.warning("PDF viewer failed to get image for page: " + p);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("PDF viewer failed to get page image: " + e);
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
            viewImage = getPageImage(pageNumber, getWidth(), getHeight());
        }

        if (viewImage != null) {
            g2.clearRect(0, 0, getWidth(), getHeight());
            g2.scale(zoom, zoom);
            g2.drawImage(viewImage, 0, 0, viewImage.getWidth(), viewImage.getHeight(), null);
        } else {
            LOGGER.finest("no page image!");
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
