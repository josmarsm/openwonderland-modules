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
//
//  Copyright (C) 2001-2004 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//
//
// VncViewer.java - the VNC viewer applet.  This class mainly just sets up the
// user interface, leaving it to the VncCanvas to do the actual rendering of
// a VNC desktop.
//
package org.jdesktop.wonderland.modules.tightvncviewer.client;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Logger;
import tightvnc.OptionsFrame;
import tightvnc.VncViewer;

public class TightVNCViewerWrapper extends VncViewer {

    private final Logger logger =
            Logger.getLogger(TightVNCViewerWrapper.class.getName());
    private TightVNCViewerApp app;

    public TightVNCViewerWrapper(TightVNCViewerApp app) {
        this.app = app;
    }

    /*
     * init
     */
    @Override
    public void init() {
        recordingSync = new Object();
        readParameters();
        options = new OptionsFrame(this);
        rfbThread = new Thread(this);
        rfbThread.start();
    }

    /*
     * run
     */
    @Override
    public void run() {
        logger.fine("run");
        try {
            connectAndAuthenticate();
            doProtocolInitialisation();
            Dimension fbSize = getFramebufferSize();
            // TODO: can we scale this any higher?
            //double width = app.getWindow().getWidth(); //fbSize.width; //2048;
            double width = (fbSize.getWidth() > 1024) ? 1024 : fbSize.getWidth();
            double height = (fbSize.getHeight() / fbSize.getWidth()) * width;
            logger.fine("resizing window to: " + width + "x" + height);
            app.getWindow().setSize((int) width, (int) height);
            createCanvas((int) width, (int) height);
            processNormalProtocol();
        } catch (Exception e) {
            logger.warning("failed to initialize VNC connection: " + e);
            e.printStackTrace();
        }
    }

    /*
     * Create a VncCanvas instance
     */
    @Override
    protected void createCanvas(int maxWidth, int maxHeight) throws IOException {
        logger.fine("creating canvas: " + maxWidth + "x" + maxHeight);
        try {
            super.createCanvas(maxWidth, maxHeight);

            TightVNCViewerWindow window = app.getWindow();
            TightVNCViewerPanel panel = (TightVNCViewerPanel) window.getComponent();
            panel.setCanvas(vc);
        } catch (Exception e) {
            logger.warning("failed to create canvas: " + e);
            e.printStackTrace();
        }
    }

    /*
     * Process RFB socket messages.
     * If the rfbThread is being stopped, ignore any exceptions,
     * otherwise rethrow the exception so it can be handled.
     */
    @Override
    protected void processNormalProtocol() throws Exception {
        logger.fine("starting protocol processing");
        try {
            super.processNormalProtocol();
        } catch (Exception e) {
            logger.warning("exception while processing VNC protocol: " + e);
        }
    }

    /*
     * Show a message describing the connection status.
     * To hide the connection status label, use (msg == null).
     */
    @Override
    protected void showConnectionStatus(String msg) {
        logger.info(msg);
    }

    /*
     * Show an authentication panel.
     */
    @Override
    protected String askPassword() throws Exception {
        logger.warning("askPassword not implemented");
        return null;
    }

    /*
     * setCutText() - send the given cut text to the RFB server.
     */
    @Override
    protected void setCutText(String text) {
        logger.warning("setCutText not implemented");
    }

    /*
     * Order change in session recording status. To stop recording, pass
     * null in place of the fname argument.
     */
    @Override
    protected void setRecordingStatus(String fname) {
        logger.warning("setRecordingStatus not implemented");
        //super.setRecordingStatus(fname);
    }

    public TightVNCViewerApp getApp() {
        return app;
    }

    @Override
    public String readParameter(String name, boolean required) {
        String value = super.readParameter(name, required);
        logger.fine("read parameter: " + name + (required ? "" : "(optional)") + " = " + value);
        return value;
    }

    /*
     * fatalError() - print out a fatal error message.
     * FIXME: Do we really need two versions of the fatalError() method?
     */
    @Override
    synchronized public void fatalError(String str) {
        logger.severe("fatal error: " + str);
    }

    @Override
    synchronized public void fatalError(String str, Exception e) {
        logger.severe("fatal error: " + str + ": " + e);
    }

    @Override
    protected void showMessage(String msg) {
        logger.info(msg);
        app.getWindow().showHUDMessage(msg, 5000);
    }

    /*
     * Stop the applet.
     * Main applet thread will terminate on first exception
     * after seeing that rfbThread has been set to null.
     */
    @Override
    public void stop() {
        super.stop();
    }

    /*
     * This method is called before the applet is destroyed.
     */
    @Override
    public void destroy() {
        logger.warning("destroy not implemented");
    }

    /*
     * Start/stop receiving mouse events.
     */
    @Override
    public void enableInput(boolean enable) {
        logger.warning("enableInput not implemented");
    }

    @Override
    protected void moveFocusToDesktop() {
    }

    /*
     * Close application properly on window close event.
     */
    @Override
    public void windowClosing(WindowEvent evt) {
        logger.warning("windowClosing not implemented");
    }

    public void sendCtrlAltDel() {
        try {
            final int modifiers = InputEvent.CTRL_MASK | InputEvent.ALT_MASK;

            KeyEvent ctrlAltDelEvent =
                    new KeyEvent(this, KeyEvent.KEY_PRESSED, 0, modifiers, 127);
            this.rfb.writeKeyEvent(ctrlAltDelEvent);

            ctrlAltDelEvent =
                    new KeyEvent(this, KeyEvent.KEY_RELEASED, 0, modifiers, 127);
            this.rfb.writeKeyEvent(ctrlAltDelEvent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scheduleRepaint(long tm, int x, int y, int width, int height) {
        app.getWindow().scheduleRepaint(tm, x, y, width, height);
    }
}

