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
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.pdfviewer.client.cell.PDFCellMenu.Button;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.Action;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage.RequestStatus;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;

/**
 * A PDF Viewer Application
 *
 * @author nsimpson
 */
public class PDFViewerApp extends AppWindowGraphics2DApp
        implements KeyListener, MouseMotionListener, MouseWheelListener,
        PDFCellMenuListener {

    private static final Logger logger =
            Logger.getLogger(PDFViewerApp.class.getName());
    private static final int DEFAULT_WIDTH = 791;
    private static final int DEFAULT_HEIGHT = 1024;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private PDFDocumentDialog pdfDialog;
    private HUDButton msgButton;
    private URL docURL;
    private PDFFile currentFile;
    private PDFPage currentPage;
    private BufferedImage pageImage;
    private boolean pageDirty = true;
    private int xScroll = 0;
    private int yScroll = 0;
    private float zoom = 1.0f;
    private Point mousePos = new Point();
    private boolean isDragging = false;
    private boolean playing = false;
    private boolean synced = false;
    private boolean inControl = false;
    private PDFCellMenu cellMenu;
    protected Object actionLock = new Object();

    public PDFViewerApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public PDFViewerApp(SharedApp2DImageCell cell, int x, int y, int width, int height, boolean decorated) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        initPDFDialog();
        initHUDMenu();
        addEventListeners();
    }

    /**
     * Set up event listeners for keyboard and mouse events
     */
    private void addEventListeners() {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        cellMenu.addCellMenuListener(this);
    }

    private void initHUDMenu() {
        cellMenu = new PDFCellMenu();
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
                    if (isSynced()) {
                        // while it's loading, notify other clients so they can load the
                        // document in parallel
                        try {
                            URL url = new URL(pdfDialog.getDocumentURL());
                            sendDocumentRequest(PDFCellMessage.Action.OPEN_DOCUMENT,
                                    url,
                                    1,
                                    new Point());
                        } catch (Exception e) {
                            showHUDMessage("invalid PDF URL", 5000);
                            logger.warning("invalid PDF URL: " + pdfDialog.getDocumentURL());
                        }
                    } else {
                        openDocument(pdfDialog.getDocumentURL(), 1, new Point());
                    }
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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (pdfDialog != null) {
                    pdfDialog.setVisible(false);
                }
            }
        });
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
            PDFViewerApp.class.getResource("resources/pdf-document.png")
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

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /**
     * Set the size of of the Window (same as awt.Component.setSize)
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        repaint();
    }

    public boolean isSynced() {
        return synced;
    }

    /** 
     * Resynchronize the state of the cell.
     * 
     * A resync is necessary when the cell transitions from INACTIVE to 
     * ACTIVE cell state, where the cell may have missed state synchronization 
     * messages while in the INACTIVE state.
     * 
     * Resynchronization is only performed if the cell is currently synced.
     * To sync an unsynced cell, call sync(true) instead.
     */
    public void resync() {
        if (isSynced()) {
            synced = false;
            sync(true);
        }
    }

    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("PDF viewer unsynced");
            showHUDMessage("unsynced", 3000);
            updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("PDF viewer requesting sync with shared state");
            showHUDMessage("syncing...", 3000);
            sendDocumentRequest(Action.GET_STATE,
                    null,
                    0,
                    new Point());
        }
    }

    protected void sendDocumentRequest(Action action, URL url, int page, Point position) {
        PDFCellMessage msg = null;

        msg = new PDFCellMessage(this.getCell().getCellID(),
                ((PDFViewerCell) cell).getUID(),
                action,
                (url != null) ? url.toString() : null,
                page,
                position);

        if (currentFile != null) {
            msg.setPageCount(currentFile.getNumPages());
        }

        if (msg != null) {
            // send request to server
            logger.fine("PDF viewer sending document request: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    /**
     * Retries a PDF action request
     * @param action the action to retry
     * @param url the URL of the document that the action applies to
     * @param page the page in the document
     * @param position the page scroll position
     * @param position
     */
    protected void retryDocumentRequest(Action action, URL url, int page, Point position) {
        logger.fine("PDF Viewer creating retry thread for: " + action + ", " + url + ", " + page + ", " + position);
        new ActionScheduler(action, url, page, position).start();
    }

    protected class ActionScheduler extends Thread {

        private Action action;
        private URL url;
        private int page;
        private Point position;

        public ActionScheduler(Action action, URL url, int page, Point position) {
            this.action = action;
            this.url = url;
            this.page = page;
            this.position = position;
        }

        @Override
        public void run() {
            // wait for a retry window
            synchronized (actionLock) {
                try {
                    logger.fine("PDF viewer waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    logger.fine("PDF viewer exception waiting for retry: " + e);
                }
            }
            // retry this request
            logger.info("PDF viewer now retrying: " + action + ", " + url + ", " + page + ", " + position);
            sendDocumentRequest(action, url, page, position);
        }
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
                    logger.info("PDF viewer opening: " + url);
                    showHUDMessage("opening " + fileName, 5000);
                    pdfDialog.setDocumentURL(url.toString());

                    // attempt to load the document
                    Date then = new Date();
                    loadingFile = new PDFFile(getDocumentData(url));
                    Date now = new Date();

                    logger.info("PDF loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
                } catch (Exception e) {
                    logger.warning("PDF viewer failed to open: " + url + ": " + e);
                    showHUDMessage("failed to open " + fileName, 5000);
                }
                if (loadingFile != null) {
                    // document was loaded successfully
                    currentFile = loadingFile;
                    showPage(page);
                    setViewPosition(position);
                    // notify other clients
                    if (isSynced()) {
                        sendDocumentRequest(PDFCellMessage.Action.REQUEST_COMPLETE,
                                url,
                                page,
                                position);
                    }
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
        openDocument(doc, 1, new Point());
    }

    /**
     * Open a PDF document
     * @param doc the URL of the PDF document to open
     * @param page the page to display initially
     * @param position the initial scroll position of the page
     */
    public void openDocument(String doc, int page, Point position) {
        // To enable a simulation of PDF GLO request denial uncomment the 
        // following. Other clients will receive REQUEST_DENIED messages when 
        // attempting to control the document and will retry their requests 
        // when this client request completes. See bug #369.
//        try {
//            long sleep = (long) (Math.random() * 1000 * 30);
//            Thread.sleep(sleep);
//        } catch (InterruptedException e) {
//        }
        if ((doc == null) || (doc.length() == 0)) {
            // no document to open
            return;
        }

        if ((docURL != null) && (docURL.toString().equals(doc))) {
            // document is already open
            showPage(page);
            setViewPosition(position);
            return;
        }

        // opening a new document
        // close the currently open document if there is one
        if (docURL != null) {
            closeDocument(docURL.toString());
        }

        try {
            // load document in a new thread
            docURL = new URL(doc);
            new DocumentLoader(docURL, page, position).start();
        } catch (Exception e) {
            logger.warning("PDF viewer failed to open: " + doc + ": " + e);
            showHUDMessage("failed to open " + doc, 5000);
        }
    }

    /**
     * Close a PDF document
     * @param doc the document to close
     */
    public void closeDocument(String doc) {
        if (doc != null) {
            // REMIND: close the document input stream
            // and check that we're closing a document that's been opened
            docURL = null;
            currentPage = null;
            currentFile = null;
            mousePos.setLocation(0, 0);
            pageDirty = true;
            repaint();
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
                    logger.warning("PDF viewer failed to get image for page: " + p);
                }

            }
        } catch (Exception e) {
            logger.severe("PDF viewer failed to get page image: " + e);
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
        showPage(getPageNumber());
    }

    /**
     * Display the specified page
     * @param p the page to display
     */
    public void showPage(int p) {
        if (isValidPage(p)) {
            logger.info("PDF viewer showing page: " + p);
            mousePos.setLocation(0, 0);
            currentPage = currentFile.getPage(p);
            pageDirty = true;
            repaint();

            showHUDMessage("page " + p, 3000);

            // pre-cache the next page
            if (isValidPage(p + 1)) {
                logger.fine("PDF viewer pre-caching page: " + (p + 1));
                currentFile.getPage(p + 1);
            }
        } else {
            logger.warning("PDF page " + p + " is not a valid page");
        }
    }

    public int getNextPage() {
        int next = getPageNumber() + 1;
        next = (isValidPage(next)) ? next : 1;
        return next;
    }

    public int getPreviousPage() {
        int prev = getPageNumber() - 1;
        prev = isValidPage(prev) ? prev : 1;
        return prev;
    }

    /**
     * Display the specified page
     * @param page the page to display
     */
    public void gotoPage(int page) {
        if (isValidPage(page)) {
            if (isSynced()) {
                // notify other clients that the page changed
                sendDocumentRequest(PDFCellMessage.Action.SHOW_PAGE,
                        docURL,
                        page,
                        mousePos);
            } else {
                // show page independently of other clients
                showPage(page);
            }
        }
    }

    /**
     * Display the next page after the currently selected page
     */
    public void nextPage() {
        gotoPage(getNextPage());
    }

    /**
     * Display the previous page to the currently selected page
     */
    public void previousPage() {
        gotoPage(getPreviousPage());
    }

    /**
     * Set the view position
     * @param position the desired position
     */
    public void setViewPosition(Point position) {
        xScroll = (int) position.getX();
        yScroll = (int) position.getY();

        repaint();
    }

    public Point getViewPosition() {
        return mousePos;
    }

    /**
     * Pause/resume the slide show
     * @param toPause if true, play the slide show else resume
     */
    public void play(boolean toPlay) {
        playing = toPlay;
        showHUDMessage((playing == true) ? "play" : "pause", 3000);
        if (isSynced()) {
            // notify other clients that slide show state hase changed
            sendDocumentRequest(
                    (playing == true) ? PDFCellMessage.Action.PLAY : PDFCellMessage.Action.PAUSE,
                    docURL,
                    getPageNumber(),
                    getViewPosition());
        }
    }

    /**
     * PDFCellMenuListener methods
     */
    public void open() {
        showPDFDialog();
    }

    public void first() {
        gotoPage(1);
    }

    public void previous() {
        previousPage();
    }

    public void gotoPage() {
        logger.info("PDF viewer goto page not implemented");
    }

    public void next() {
        nextPage();
    }

    public void last() {
        if (currentFile != null) {
            gotoPage(currentFile.getNumPages());
        }
    }

    public void startSlideShow() {
        play(true);
    }

    public void pauseSlideShow() {
        play(false);
    }

    public void zoomIn() {
        zoom += 0.3f;
        repaint();
    }

    public void zoomOut() {
        zoom -= 0.3f;
        repaint();
    }

    public void sync() {
        sync(!isSynced());
    }

    public void unsync() {
        sync(!isSynced());
    }

    public void setInSlideShowMode(boolean inSlideShow) {
        playing = inSlideShow;
        
        updateMenu();
    }

    /**
     * Render the current page of the PDF document
     * @param g the surface on which to draw the page
     */
    @Override
    protected void paint(Graphics2D g) {
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

            logger.finest("PDF viewer dimensions: " + getWidth() + "x" + getHeight());
            logger.finest("PDF viewer page dimentions: " + scaledPageWidth + "x" + scaledPageHeight);
            logger.finest("PDF viewer yScroll: " + yScroll);
            logger.finest("PDF viewer page width (page units): " + pageImage.getWidth());
            logger.finest("PDF viewer page height (page units): " + visibleHeight / scale);

            BufferedImage visibleImage = pageImage.getSubimage(xScroll, (int) (int) (yScroll / scale), pageImage.getWidth(), (int) (visibleHeight / scale));

            g.clearRect(0, 0, (int) appWidth, (int) appHeight);
            g.scale(zoom, zoom);
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
        logger.finest("PDF viewer mouseMoved: " + evt);
        isDragging = false;
        mousePos.setLocation(evt.getX(), evt.getY());
    }

    /**
     * Process a mouse drag event
     * @param evt the mouse drag event
     */
    public void mouseDragged(MouseEvent evt) {
        logger.finest("PDF viewer mouseDragged: " + evt);

        if (pageImage != null) {
            if (isDragging == false) {
                // drag started
                isDragging = true;
            } else {
                // drag in progress
                // calculate distance moved in x and y
                double xDelta = mousePos.getX() - evt.getX();
                double yDelta = mousePos.getY() - evt.getY();
                Point position = new Point(xScroll, (int) (yScroll + yDelta));

                setViewPosition(position);

                if (isSynced()) {
                    // notify other clients that the page moved
                    sendDocumentRequest(PDFCellMessage.Action.SET_VIEW_POSITION,
                            docURL,
                            getPageNumber(),
                            position);
                }
            }
            mousePos.setLocation(evt.getX(), evt.getY());
        }
    }

    /**
     * Process a mouse wheel event
     * @param evt the mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent evt) {
        logger.finest("PDF viewer mouseWheelMoved: " + evt);

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
        logger.finest("PDF viewer keyPressed: " + evt);
    }

    /**
     * Process a key release event
     * @param evt the key release event
     */
    public void keyReleased(KeyEvent evt) {
        logger.finest("PDF viewer keyReleased: " + evt);

        switch (evt.getKeyCode()) {
            case KeyEvent.VK_O:
                // open a new document
                if (evt.isControlDown() == true) {
                    showPDFDialog();
                }
                break;
            case KeyEvent.VK_PAGE_UP:
                // show previous page
                previousPage();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                // show next page
                nextPage();
                break;
            case KeyEvent.VK_P:
                // play/resume slide show
                play(!playing);
                break;
            case KeyEvent.VK_S:
                // unsync/resync with shared state
                sync(!isSynced());
                break;
        }
    }

    /**
     * Process a key typed event
     * @param evt the key release event
     */
    public void keyTyped(KeyEvent evt) {
        logger.finest("PDF viewer keyTyped: " + evt);
    }

    public void handleResponse(PDFCellMessage msg) {
        String controlling = msg.getUID();
        String myUID = ((PDFViewerCell) cell).getUID();
        boolean forMe = (myUID.equals(controlling));
        PDFCellMessage pdfcm = null;

        if (isSynced()) {
            logger.fine("PDF viewer " + myUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // this request was denied, create a retry thread
                try {
                    logger.info("PDF viewer scheduling retry of request: " + msg);
                    retryDocumentRequest(msg.getAction(), (msg.getDocument() == null) ? null : new URL(msg.getDocument()),
                            msg.getPage(), msg.getPosition());
                } catch (Exception e) {
                    logger.warning("PDF viewer failed to create retry request for: " + msg);
                }
            } else {
                switch (msg.getAction()) {
                    case OPEN_DOCUMENT:
                        openDocument(msg.getDocument());
                        break;
                    case SHOW_PAGE:
                        showPage(msg.getPage());
                        break;
                    case SET_VIEW_POSITION:
                        setViewPosition(msg.getPosition());
                        break;
                    case SET_STATE:
                        if (forMe == true) {
                            if (isSynced()) {
                                openDocument(msg.getDocument(), msg.getPage(), msg.getPosition());
                                cellMenu.disableButton(Button.UNSYNC);
                                cellMenu.enableButton(Button.SYNC);
                                logger.info("PDF viewer synced");
                                showHUDMessage("synced", 3000);
                            }
                        }
                        break;
                    case PLAY:
                        showHUDMessage("slide show starting", 3000);
                        setInSlideShowMode(true);
                        break;
                    case PAUSE:
                        showHUDMessage("slide show stopped", 3000);
                        setInSlideShowMode(false);
                        break;
                    case REQUEST_COMPLETE:
                        // retry queued requests
                        synchronized (actionLock) {
                            try {
                                logger.fine("PDF viewer waking retry threads");
                                actionLock.notify();
                            } catch (Exception e) {
                                logger.warning("PDF viewer exception notifying retry threads: " + e);
                            }
                        }
                        break;
                }
                if ((forMe == true) && (msg.getAction() != Action.REQUEST_COMPLETE)) {
                    // notify everyone that the request has completed
                    pdfcm = new PDFCellMessage(msg);
                    pdfcm.setAction(Action.REQUEST_COMPLETE);
                }
            }
        }
        if (pdfcm != null) {
            logger.fine("PDF viewer sending message: " + pdfcm);
            ChannelController.getController().sendMessage(pdfcm);
        }
    }

    protected void updateMenu() {
        if (((PDFCellMenu) cellMenu).isActive()) {
            if (isSynced()) {
                ((PDFCellMenu) cellMenu).enableButton(Button.SYNC);
                ((PDFCellMenu) cellMenu).disableButton(Button.UNSYNC);
            } else {
                ((PDFCellMenu) cellMenu).enableButton(Button.UNSYNC);
                ((PDFCellMenu) cellMenu).disableButton(Button.SYNC);
            }

            if (playing == true) {
                ((PDFCellMenu) cellMenu).enableButton(Button.PAUSE_SLIDESHOW);
                ((PDFCellMenu) cellMenu).disableButton(Button.START_SLIDESHOW);
            } else {
                ((PDFCellMenu) cellMenu).enableButton(Button.START_SLIDESHOW);
                ((PDFCellMenu) cellMenu).disableButton(Button.PAUSE_SLIDESHOW);
            }
        }
    }

    public void setInControl(boolean inControl) {
        this.inControl = inControl;

        if (inControl == true) {
            CellMenuManager.getInstance().showMenu(this.getCell(), cellMenu, null);
            updateMenu();
        } else {
            CellMenuManager.getInstance().hideMenu();
        }
    }

    @Override
    public void takeControl(MouseEvent me) {
        logger.fine("PDF viewer has control");
        super.takeControl(me);
        setInControl(true);
    }

    @Override
    public void releaseControl(MouseEvent me) {
        logger.fine("PDF viewer lost control");
        super.releaseControl(me);
        setInControl(false);
    }
}

