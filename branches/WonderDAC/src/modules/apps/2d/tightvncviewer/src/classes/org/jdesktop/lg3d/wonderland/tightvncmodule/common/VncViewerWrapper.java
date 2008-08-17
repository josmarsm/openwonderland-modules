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
package org.jdesktop.lg3d.wonderland.tightvncmodule.common;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCModuleApp;
import tightvnc.VncViewer;
import tightvnc.RfbProto;
import tightvnc.OptionsFrame;

public class VncViewerWrapper extends VncViewer {

    private final Logger logger =
            Logger.getLogger(VncViewerWrapper.class.getName());
    private TightVNCModuleApp app;

    public VncViewerWrapper(TightVNCModuleApp app) {
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
            logger.fine("*** framebuffer size is: " + fbSize);
            app.setSize(fbSize.width, fbSize.height);
            createCanvas(fbSize.width, fbSize.height);
            processNormalProtocol();
        } catch (Exception e) {
            logger.warning("failed to initialize VNC connection: " + e);
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
            vc.setImage(new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB));
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
            e.printStackTrace();
        }
    }

    /*
     * Connect to the RFB server and authenticate the user.
     */
    @Override
    protected void connectAndAuthenticate() throws Exception {
        logger.fine("connectAndAuthenticate");
        showConnectionStatus("initializing...");

        showConnectionStatus("connecting to " + host + ", port " + port + "...");

        rfb = new RfbProto(host, port, this);
        showConnectionStatus("connected to server");

        rfb.readVersionMsg();
        showConnectionStatus("RFB server supports protocol version " +
                rfb.serverMajor + "." + rfb.serverMinor);

        rfb.writeVersionMsg();
        showConnectionStatus("using RFB protocol version " +
                rfb.clientMajor + "." + rfb.clientMinor);

        int secType = rfb.negotiateSecurity();
        int authType;
        if (secType == RfbProto.SecTypeTight) {
            showConnectionStatus("unabling TightVNC protocol extensions");
            rfb.initCapabilities();
            rfb.setupTunneling();
            authType = rfb.negotiateAuthenticationTight();
        } else {
            authType = secType;
        }

        switch (authType) {
            case RfbProto.AuthNone:
                showConnectionStatus("no authentication needed");
                rfb.authenticateNone();
                break;
            case RfbProto.AuthVNC:
                showConnectionStatus("performing standard VNC authentication");
                if (passwordParam != null) {
                    rfb.authenticateVNC(passwordParam);
                } else {
                    String pw = askPassword();
                    rfb.authenticateVNC(pw);
                }
                break;
            default:
                throw new Exception("unknown authentication scheme " + authType);
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
        logger.fine("*** askPassword not implemented");
        return null;
    }

    /*
     * Do the rest of the protocol initialisation.
     */
    @Override
    protected void doProtocolInitialisation() throws IOException {
        logger.fine("doProtocolInitialisation: " + rfb);
        super.doProtocolInitialisation();
    }

    /*
     * setCutText() - send the given cut text to the RFB server.
     */
    @Override
    protected void setCutText(String text) {
        logger.fine("*** setCutText not implemented");
    }

    /*
     * Order change in session recording status. To stop recording, pass
     * null in place of the fname argument.
     */
    @Override
    protected void setRecordingStatus(String fname) {
        logger.fine("*** setRecordingStatus not implemented");
    //super.setRecordingStatus(fname);
    }

    public TightVNCModuleApp getApp() {
        return app;
    }

    @Override
    public String readParameter(String name, boolean required) {
        String value = super.readParameter(name, required);
        logger.fine("readParameter: name: " + name + ", required: " + required + " = " + value);
        return value;
    }

    @Override
    protected int readIntParameter(String name, int defaultValue) {
        int value = super.readIntParameter(name, defaultValue);
        logger.fine("readIntParameter: name: " + name + ", default: " + defaultValue + " = " + value);
        return value;
    }

    /*
     * moveFocusToDesktop() - move keyboard focus either to VncCanvas.
     */
    @Override
    protected void moveFocusToDesktop() {
        logger.fine("*** moveFocusToDesktop not implemented");
    }

    /*
     * disconnect() - close connection to server.
     */
    @Override
    synchronized public void disconnect() {
        if (rfb != null && !rfb.closed()) {
            rfb.close();
        }
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

    /*
     * Show message text and optionally "Relogin" and "Close" buttons.
     */
    @Override
    protected void showMessage(String msg) {
        logger.fine(msg);
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
        logger.fine("*** destroy not implemented");
    }

    /*
     * Start/stop receiving mouse events.
     */
    @Override
    public void enableInput(boolean enable) {
        logger.fine("*** enableInput not implemented");
    }

    /*
     * Close application properly on window close event.
     */
    @Override
    public void windowClosing(WindowEvent evt) {
        logger.fine("*** windowClosing not implemented");
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        if (vc != null) {
            vc.repaint(tm, x, y, width, height);
        }
    }

    @Override
    public void scheduleRepaint(long tm, int x, int y, int width, int height) {
        app.scheduleRepaint(tm, x, y, width, height);
    }
}

