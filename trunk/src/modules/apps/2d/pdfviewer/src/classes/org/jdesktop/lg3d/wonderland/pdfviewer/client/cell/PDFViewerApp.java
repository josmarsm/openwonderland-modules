/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
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

import java.awt.Font;
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

import javax.swing.SwingUtilities;

import java.util.Date;
import java.util.logging.Logger;

import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DCell;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.Action;
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
    private static final int DEFAULT_WIDTH = 791;
    private static final int DEFAULT_HEIGHT = 1024;
    private PDFDocumentDialog pdfDialog;
    private HUDButton msgButton;
    private URL docURL;
    private PDFFile currentFile;
    private PDFPage currentPage;
    private BufferedImage pageImage;
    private boolean pageDirty = true;
    private int xScroll = 0;
    private int yScroll = 0;
    private Point mousePos = new Point();
    private boolean isDragging = false;
    private boolean paused = true;

    public PDFViewerApp(SharedApp2DCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public PDFViewerApp(SharedApp2DCell cell, int x, int y, int width, int height) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        initPDFDialog();
        addEventListeners();

        setShowing(true);
    }

    /**
     * Set up event listeners for keyboard and mouse events
     */
    private void addEventListeners() {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    /**
     * Initialize the dialog for opening PDF documents
     */
    private void initPDFDialog() {
        pdfDialog = new PDFDocumentDialog(null, false);
        pdfDialog.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hidePDFDialog();
                if (evt.getActionCommand().equals("OK")) {
                    openDocument(pdfDialog.getDocumentURL(), 1, new Point(), true);
                }
            }
        });
    }

    /**
     * Display the open PDF document dialog
     */
    private void showPDFDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                pdfDialog.setVisible(true);
            }
        });
    }

    /**
     * Hide the open PDF document dialog
     */
    public void hidePDFDialog() {
        if (pdfDialog != null) {
            pdfDialog.setVisible(false);
        }
    }

    /**
     * Show a status message in the HUD
     * @param message the string to display in the message
     */
    private void showHUDMessage(String message) {
        showHUDMessage(message, HUD.NO_TIMEOUT);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
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
                Font.decode("dialog" + "-BOLD-14"),
                -300, 50, 300, 50,
                timeout, true);
    }

    /**
     * Hide the HUD message
     * @param immediately if true, remove the message now, otherwise slide it
     * off the screen first
     */
    private void hideHUDMessage(boolean immediately) {
        if (msgButton != null) {
            if (!immediately) {
                msgButton.changeLocation(new Point(-45, 50));
            }
            msgButton.setActive(false);
        }
    }

    /**
     * Set the size of of the Window (same as awt.Component.setSize)
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        repaint();
    }

    /**
     * A class for handling the loading of PDF documents. This can be time
     * consuming, so load in a thread
     */
    private class DocumentLoader extends Thread {

        private URL url;
        private int page;
        private Point position;

        public DocumentLoader(URL url, int page, Point position) {
            this.url = url;
            this.page = page;
            this.position = position;
        }

        @Override
        public void run() {
            if (url != null) {
                PDFFile loadingFile = null;
                String fileName = new File(url.toString()).getName();

                try {
                    logger.info("opening: " + url);
                    showHUDMessage("Opening " + fileName);
                    pdfDialog.setDocumentURL(docURL.toString());

                    // attempt to load the document
                    Date then = new Date();
                    loadingFile = new PDFFile(getDocumentData(url));
                    Date now = new Date();

                    logger.info("PDF loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
                } catch (Exception e) {
                    logger.warning("failed to open: " + url + ": " + e);
                    showHUDMessage("Failed to open " + fileName, 5000);
                }
                if (loadingFile != null) {
                    // document was loaded successfully
                    currentFile = loadingFile;
                    showPage(page, false);
                    setViewPosition(position, false);
                    // notify other clients
                    PDFCellMessage msg = new PDFCellMessage(getCell().getCellID(),
                            PDFCellMessage.Action.DOCUMENT_OPENED,
                            docURL.toString(),
                            page,
                            position);
                    msg.setPageCount(currentFile.getNumPages());
                    logger.fine("sending message: " + msg);
                    ChannelController.getController().sendMessage(msg);
                } else {
                    // document failed to load, update the view
                    pageDirty = true;
                    repaint();
                }
            }
        }
    }

    /**
     * Open a PDF document
     * @param doc the URL of the PDF document to open
     */
    public void openDocument(String doc) {
        openDocument(doc, 1, new Point(), false);
    }

    /**
     * Open a PDF document
     * @param doc the URL of the PDF document to open
     * @param page the page to display initially
     * @param position the initial scroll position of the page
     * @param notify whether to notify other clients
     */
    public void openDocument(String doc, int page, Point position, boolean notify) {
        if ((doc == null) || (doc.length() == 0)) {
            return;
        }

        // close the currently open document if there is one
        if (docURL != null) {
            closeDocument(docURL.toString(), notify);
        }

        try {
            // load document in a new thread
            docURL = new URL(doc);
            new DocumentLoader(docURL, page, position).start();

            // while it's loading, notify other clients so they can load the
            // document in parallel
            if (notify == true) {
                PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                        PDFCellMessage.Action.OPEN_DOCUMENT,
                        docURL.toString(),
                        page,
                        position);

                logger.info("sending message: " + msg);
                ChannelController.getController().sendMessage(msg);
            }
        } catch (Exception e) {
            logger.warning("failed to open: " + docURL + ": " + e);
            showHUDMessage("Failed to open " + doc, 5000);
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

            docURL = null;
            currentPage = null;
            currentFile = null;
            mousePos.setLocation(0, 0);
        }
    }

    /**
     * Get the PDF document data from a URL
     * @param docURL the URL of the PDF document to open
     * @return the PDF document data
     */
    public ByteBuffer getDocumentData(URL docURL) throws IOException {
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

            buf = ByteBuffer.wrap((byte[]) data, 0, ((byte[]) data).length);
        }

        return buf;
    }

    /**
     * Get the PDF document data from a File
     * @param file the file name of the PDF document to open
     * @return the PDF document data
     */
    public ByteBuffer getDocumentData(File file) throws IOException {
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
    public BufferedImage getPageImage(int p) {
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
        showPage(getPageNumber(), false);
    }

    /**
     * Display the specified page
     * @param p the page to display
     */
    public void showPage(int p) {
        showPage(p, false);
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

            showHUDMessage("Page " + p, 3000);

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

            // pre-cache the next page
            if (isValidPage(p + 1)) {
                logger.fine("pre-caching page: " + (p + 1));
                currentFile.getPage(p + 1);
            }
        }
    }

    /**
     * Display the next page after the currently selected page
     */
    public void nextPage() {
        int next = getPageNumber() + 1;
        next = (isValidPage(next)) ? next : 1;

        showPage(next, true);
    }

    /**
     * Display the previous page to the currently selected page
     */
    public void previousPage() {
        int prev = getPageNumber() - 1;
        prev = isValidPage(prev) ? prev : 1;

        showPage(prev, true);
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
        yScroll = (int) position.getY();

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

    public Point getViewPosition() {
        return mousePos;
    }

    public void pause(boolean toPause) {
        pause(toPause, false);
    }

    /**
     * Pause/resume the slide show
     * @param toPause if true, pause the slide show else resume
     * @param whether to notify other clients
     */
    public void pause(boolean toPause, boolean notify) {
        paused = toPause;
        if (notify == true) {
            // notify other clients that the page moved
            PDFCellMessage msg = new PDFCellMessage(this.getCell().getCellID(),
                    Action.PAUSE,
                    docURL.toString(),
                    getPageNumber(),
                    getViewPosition());

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

        // size of app which page will be scaled to fit
        double appWidth = (double) this.getWidth();
        double appHeight = (double) this.getHeight();

        if (pageDirty == true) {
            pageImage = getPageImage();
            xScroll = 0;
            yScroll = 0;
            isDragging = false;
            pageDirty = false;
        }

        if (pageImage != null) {
            // might have to scale due to page size changing
            double scale = (double) this.getWidth() / (double) pageImage.getWidth();

            // size of page image scaled to fit app width
            double scaledPageWidth = scale * pageImage.getWidth();
            double scaledPageHeight = scale * pageImage.getHeight();

            // calculate the visible portion of the page
            double visibleHeight = Math.min(scaledPageHeight, appHeight);

            // prevent scrolling off end of scaled page
            yScroll = (yScroll + appHeight > scaledPageHeight) ? (int) (scaledPageHeight - appHeight) : yScroll;

            // prevent scrolling off top of page
            yScroll = (yScroll < 0) ? 0 : yScroll;

            logger.finest("app dimensions: " + getWidth() + "x" + getHeight());
            logger.finest("page dimentions: " + scaledPageWidth + "x" + scaledPageHeight);
            logger.finest("yScroll: " + yScroll);
            logger.finest("page width (page units): " + pageImage.getWidth());
            logger.finest("page height (page units): " + visibleHeight / scale);

            BufferedImage visibleImage = pageImage.getSubimage(xScroll, (int) (int)(yScroll/scale), pageImage.getWidth(), (int) (visibleHeight / scale));

            g.drawImage(visibleImage, 0, 0, (int) appWidth, (int) visibleHeight, null);
        } else {
            g.clearRect(0, 0, (int) appWidth, (int) appHeight);
        }
    }

    /**
     * Process a mouse motion event
     * @param evt the mouse motion event
     */
    public void mouseMoved(MouseEvent evt) {
        logger.finest("mouseMoved: " + evt);
        isDragging = false;
        mousePos.setLocation(evt.getX(), evt.getY());
    }

    /**
     * Process a mouse drag event
     * @param evt the mouse drag event
     */
    public void mouseDragged(MouseEvent evt) {
        logger.finest("mouseDragged: " + evt);

        if (pageImage != null) {
            if (isDragging == false) {
                // drag started
                isDragging = true;
            } else {
                // drag in progress
                // calculate distance moved in x and y
                double xDelta = mousePos.getX() - evt.getX();
                double yDelta = mousePos.getY() - evt.getY();

                double scale = this.getWidth() / pageImage.getWidth();
                double pageHeight = scale * pageImage.getHeight();

                setViewPosition(new Point(xScroll, (int) (yScroll + yDelta)), true);
            }
            mousePos.setLocation(evt.getX(), evt.getY());
        }
    }

    /**
     * Process a mouse wheel event
     * @param evt the mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent evt) {
        logger.finest("mouseWheelMoved: " + evt);

        if (evt.getWheelRotation() < 0) {
            previousPage();
        } else {
            nextPage();
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
                previousPage();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                nextPage();
                break;
            case KeyEvent.VK_P:
                pause(!paused, true);
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
