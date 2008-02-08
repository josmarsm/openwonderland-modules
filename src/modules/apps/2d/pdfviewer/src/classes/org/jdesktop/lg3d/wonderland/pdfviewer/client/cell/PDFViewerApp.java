/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.pdfviewer.client.cell;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DCell;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import com.sun.sgs.client.ClientChannel;

import java.awt.Font;
import java.util.Date;
import java.util.logging.Logger;


import javax.swing.SwingUtilities;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.scenemanager.EventController;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;

/**
 * A PDF Viewer Application
 *
 * @author nsimpson
 */
public class PDFViewerApp extends AppWindowGraphics2DApp
        implements KeyListener, MouseMotionListener, MouseWheelListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerApp.class.getName());
    private PDFDocumentDialog pdfDialog;
    private HUDButton msgButton;
    private ClientChannel channel;
    private URL docURL;
    private PDFFile currentFile;
    private PDFPage currentPage;
    private BufferedImage pageImage;
    private boolean pageDirty = false;
    private int xScroll = 0;
    private int yScroll = 0;
    private Point mousePos = new Point();
    private boolean isDragging = false;

    public PDFViewerApp(SharedApp2DCell cell) {
        this(cell, 0, 0, 1024, 768);
    }

    public PDFViewerApp(SharedApp2DCell cell, int x, int y, int width, int height) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        setShowing(true);
    }

    private void showPDFDialog() {
        if (pdfDialog == null) {
            pdfDialog = new PDFDocumentDialog(null, false);
            pdfDialog.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    hidePDFDialog();
                    if (evt.getActionCommand().equals("OK")) {
                        // attempt to open PDF document
                        openDocument(pdfDialog.getDocumentURL());
                    }
                }
            });
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                pdfDialog.setVisible(true);
            }
        });
    }

    public void hidePDFDialog() {
        if (pdfDialog != null) {
            pdfDialog.setVisible(false);
        }
    }

    private void showHUDMessage(String message) {
        showHUDMessage(message, HUD.NO_TIMEOUT);
    }

    private void showHUDMessage(String message, int timeout) {
        URL[] imgURLs = {HUD.SIMPLE_BOX_IMAGE_URL,
            EventController.class.getResource("resources/preferences-system-windows.png")
        };

        Point[] imagePoints = {new Point(), new Point(10, 10)};

        // dismiss currently active HUD message
        if ((msgButton != null) && msgButton.isActive()) {
            hideHUDMessage(true);
        }

        // display a new HUD message
        msgButton = HUDFactory.getHUD().addHUDMultiImageButton(imgURLs,
                imagePoints, message, new Point(50, 25),
                Font.decode(Font.DIALOG + "-BOLD-14"),
                -300, 50, 300, 50,
                timeout, true);
    }

    private void hideHUDMessage(boolean immediately) {
        if (msgButton != null) {
            if (!immediately) {
                msgButton.changeLocation(new Point(-45, 50));
            }
            msgButton.setActive(false);
        }
    }

    private class DocumentLoader extends Thread {

        private String doc;

        public DocumentLoader(String doc) {
            this.doc = doc;
        }

        @Override
        public void run() {
            PDFFile loadingFile = null;

            if (doc != null) {
                try {
                    docURL = new URL(doc);

                    logger.info("opening PDF document: " + doc);
                    showHUDMessage("Opening PDF: " + docURL.getFile());

                    Date then = new Date();
                    loadingFile = new PDFFile(getDocumentData(docURL));
                    Date now = new Date();

                    logger.info("document loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
                } catch (Exception e) {
                    logger.warning("failed to open PDF: " + docURL + ": " + e);
                    showHUDMessage("Failed to open PDF", 5000);
                }
                if (loadingFile != null) {
                    currentFile = loadingFile;
                    showPage(1, false);
                }
            }
        }
    }

    /**
     * Open a PDF document
     * @param doc the URL of the PDF document to open
     */
    public void openDocument(String doc) {
        openDocument(doc, false);
    }

    /**
     * Open a PDF document
     * @param doc the URL of the PDF document to open
     * @param notify whether to notify other clients
     */
    public void openDocument(String doc, boolean notify) {
        if ((doc == null) || (doc.length() == 0)) {
            return;
        }

        // close the currently open document if there is one
        if (docURL != null) {
            closeDocument(docURL.toString(), notify);
        }

        // load document in a new thread
        new DocumentLoader(doc).start();

        if (notify == true) {
            // notify other clients
            PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                    PDFCellMessage.Action.OPEN_DOCUMENT,
                    docURL.toString(),
                    1,
                    mousePos);

            logger.info("sending message: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    /**
     * Close a PDF document
     * @param doc the document to close
     */
    public void closeDocument(String doc) {
        closeDocument(doc, false);
    }

    /**
     * Close a PDF document
     * @param doc the document to close
     * @param notify whether to notify other clients
     */
    public void closeDocument(String doc, boolean notify) {
        if (doc != null) {
            // REMIND: close the document input stream
            // and check that we're closing a document that's been opened
            if (notify == true) {
                // notify other clients
                PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                        PDFCellMessage.Action.CLOSE_DOCUMENT,
                        docURL.toString(),
                        0,
                        mousePos);

                logger.info("sending message: " + msg);
                ChannelController.getController().sendMessage(msg);
            }

            this.docURL = null;
            currentPage =
                    null;
            currentFile =
                    null;
            mousePos.setLocation(0, 0);
        }

    }

    /**
     * Get the PDF document data from a URL
     * @param docURL the URL of the PDF document to open
     * @return the PDF document data
     */
    public ByteBuffer getDocumentData(
            URL docURL) throws IOException {
        ByteBuffer buf = null;

        if (docURL != null) {
            // connect to the URL
            URLConnection conn = docURL.openConnection();
            conn.connect();

            // create a buffer to load the document into
            int docSize = conn.getContentLength();
            byte[] data = new byte[docSize];

            // create a buffered stream for reading the document
            DataInputStream is = new DataInputStream(new BufferedInputStream(conn.getInputStream()));

            // read the document into the buffer
            is.readFully(data, 0, docSize);

            buf =
                    ByteBuffer.wrap((byte[]) data, 0, ((byte[]) data).length);
        }

        return buf;
    }

    /**
     * Get the PDF document data from a File
     * @param file the file name of the PDF document to open
     * @return the PDF document data
     */
    public ByteBuffer getDocumentData(
            File file) throws IOException {
        ByteBuffer buf = null;

        if (file != null) {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            buf =
                    fc.map(MapMode.READ_ONLY, 0, file.length());
        }

        return buf;
    }

    /**
     * Get the validity of the specified page in the currently open document
     * @return true if the page is within the range of pages of the current
     * document, false otherwise
     */
    public boolean isValidPage(int p) {
        return ((currentFile != null) && (p > 0) && (p <= currentFile.getNumPages()));
    }

    /**
     * Get an image of the currently selected page
     * @return the page image
     */
    public BufferedImage getPageImage() {
        return getPageImage(getPageNumber());
    }

    /**
     * Get an image of a specific page
     * @param p the page number
     * @return the image of the specified page
     */
    public BufferedImage getPageImage(
            int p) {
        BufferedImage image = null;

        try {
            if (isValidPage(p)) {
                currentPage = currentFile.getPage(p, true);
                double pw = currentPage.getWidth();
                double ph = currentPage.getHeight();
                double aw = (double) this.getWidth();
                double ah = (double) this.getHeight();

                // request a page that fits the width of the viewer and has
                // the correct aspect ratio
                int rw = (int) aw;
                int rh = (int) (ph * (aw / pw));

                Image img = currentFile.getPage(p).getImage(rw, rh, null, null, true, true);

                if (img != null) {
                    // convert the page image into a buffered image
                    image = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(img, 0, 0, rw, rh, null);
                } else {
                    logger.warning("failed to get image for page: " + p);
                }

            }
        } catch (Exception e) {
            logger.severe("failed to get page image: " + e);
        }

        return image;
    }

    /**
     * Get the page number of the currently selected page
     * @return the page number
     */
    public int getPageNumber() {
        int p = 0;  // an invalid page number

        if ((currentFile != null) && (currentPage != null)) {
            p = currentPage.getPageNumber();
        }

        return p;
    }

    /**
     * Display the currently selected page
     */
    public void showPage() {
        showPage(getPageNumber(), true);
    }

    /**
     * Display the specified page
     * @param p the page to display
     */
    public void showPage(int p) {
        showPage(p, true);
    }

    /**
     * Display the specified page
     * @param p the page to display
     * @param notify whether to notify other clients
     */
    public void showPage(int p, boolean notify) {
        if (isValidPage(p)) {
            logger.info("showing page: " + p);
            mousePos.setLocation(0, 0);
            currentPage = currentFile.getPage(p);
            pageDirty = true;
            repaint();

            showHUDMessage("Page: " + p, 3000);

            if (notify == true) {
                // notify other clients that the page changed
                PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                        PDFCellMessage.Action.SHOW_PAGE,
                        docURL.toString(),
                        p,
                        mousePos);

                logger.info("sending message: " + msg.getAction());
                ChannelController.getController().sendMessage(msg);
            }
        }
    }

    /**
     * Display the next page after the currently selected page
     */
    public void nextPage() {
        showPage(getPageNumber() + 1, true);
    }

    /**
     * Display the previous page to the currently selected page
     */
    public void previousPage() {
        showPage(getPageNumber() - 1, true);
    }

    /**
     * Set the view position
     * @param position the desired position
     */
    public void setViewPosition(Point position) {
        setViewPosition(position, false);
    }

    /**
     * Set the view position
     * @param position the desired position
     * @param whether to notify other clients
     */
    public void setViewPosition(Point position, boolean notify) {
        xScroll = (int) position.getX();
        yScroll =
                (int) position.getY();

        repaint();

        if (notify == true) {
            // notify other clients that the page moved
            PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                    PDFCellMessage.Action.SET_VIEW_POSITION,
                    docURL.toString(),
                    getPageNumber(),
                    position);

            logger.info("sending message: " + msg.getAction());
            ChannelController.getController().sendMessage(msg);
        }

    }

    /**
     * Render the current page of the PDF document
     * @param g the surface on which to draw the page
     */
    @Override
    protected void paint(Graphics2D g) {
        logger.finest("paint");

        if (pageDirty == true) {
            pageImage = getPageImage();
            xScroll =
                    0;
            yScroll =
                    0;
            isDragging =
                    false;
            pageDirty =
                    false;
        }

        if (pageImage != null) {
            // calculate page to view scale
            double scale = (double) this.getWidth() / pageImage.getWidth();
            // handle short pages that won't fit the page height
            double subHeight = Math.min((double) this.getHeight() / scale, pageImage.getHeight());

            // get a sub-image of the page that fits the view
            BufferedImage visibleImage = pageImage.getSubimage(xScroll, yScroll, pageImage.getWidth(), (int) subHeight);
            g.drawImage(visibleImage, 0, 0, this.getWidth(), this.getHeight(), null);
        }

    }

    /**
     * Process a mouse motion event
     * @param evt the mouse motion event
     */
    public void mouseMoved(MouseEvent evt) {
        logger.finest("mouseMoved: " + evt);
        isDragging =
                false;
        mousePos.setLocation(evt.getX(), evt.getY());
    }

    /**
     * Process a mouse drag event
     * @param evt the mouse drag event
     */
    public void mouseDragged(MouseEvent evt) {
        logger.finest("mouseDragged: " + evt);

        if (isDragging == false) {
            // drag started
            isDragging = true;
        } else {
            // drag in progress
            // calculate distance moved in x and y
            double xDelta = mousePos.getX() - evt.getX();
            double yDelta = mousePos.getY() - evt.getY();

            if ((yDelta != 0) &&
                    ((yScroll + getHeight() + yDelta) < pageImage.getHeight()) &&
                    ((yScroll + yDelta) > 0)) {
                yScroll += yDelta;

                setViewPosition(new Point(xScroll, yScroll), true);
            }

        }
        mousePos.setLocation(evt.getX(), evt.getY());
    }

    /**
     * Process a mouse wheel event
     * @param evt the mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent evt) {
        logger.finest("mouseWheelMoved: " + evt);

        if (evt.getWheelRotation() < 0) {
            nextPage();
        } else {
            previousPage();
        }

    }

    /**
     * Process a key press event
     * @param evt the key press event
     */
    public void keyPressed(KeyEvent evt) {
        logger.finest("keyPressed: " + evt);
    }

    /**
     * Process a key release event
     * @param evt the key release event
     */
    public void keyReleased(KeyEvent evt) {
        logger.finest("keyReleased: " + evt);

        switch (evt.getKeyCode()) {
            case KeyEvent.VK_O:
                if (evt.isControlDown() == true) {
                    showPDFDialog();
                }

                break;
            case KeyEvent.VK_PAGE_UP:
                nextPage();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                previousPage();
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                //nextDocument();
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_UNDERSCORE:
                //prevDocument();
                break;
        }

    }

    /**
     * Process a key typed event
     * @param evt the key release event
     */
    public void keyTyped(KeyEvent evt) {
        logger.finest("keyTyped: " + evt);
    }
}
