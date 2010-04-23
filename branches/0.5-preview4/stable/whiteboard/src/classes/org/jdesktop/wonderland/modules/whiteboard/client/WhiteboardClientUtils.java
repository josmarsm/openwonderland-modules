/*
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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.AssetURI;
import org.w3c.dom.Document;

/**
 *
 * @author jordanslott
 */
public class WhiteboardClientUtils {

    public static final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
    private static final Logger logger = Logger.getLogger(WhiteboardClientUtils.class.getName());

    public static Document openDocument(String uri) {
        logger.fine("opening SVG document with URI: " + uri);
        Document doc = null;

        // Use the WL asset manager to fetch the asset. Since we are doing this on our own
        // We need to make sure we "annotate" the asset uri with the host name and port
        // of the server.
        AssetURI assetURI = AssetURI.uriFactory(uri);
        WhiteboardClientUtils.annotateURI(assetURI);
        Asset asset = AssetManager.getAssetManager().getAsset(assetURI);

        if (asset == null) {
            logger.warning("Null AssetURI for " + uri);
        } else {
            // Fetch the asset and wait for it to download
            if (AssetManager.getAssetManager().waitForAsset(asset) == true) {
                try {
                    doc = factory.createDocument(null, new FileReader(asset.getLocalCacheFile()));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        logger.fine("SVG doc: " + doc);
        return doc;
    }

    /**
     * Given a URI, annotate it with the server host name and port of the primary
     * login.
     *
     * @param uri The asset URI
     */
    public static void annotateURI(AssetURI uri) {
        // Use the primary login session to determine the server host and port
        // name
        ServerSessionManager manager = LoginManager.getPrimary();
        if (manager == null) {
            logger.warning("No primary login session for " + uri);
        } else {
            String serverHostAndPort = manager.getServerNameAndPort();

            // Annotate the URI with the host name and port from the session and
            // return as a URL
            uri.setServerHostAndPort(serverHostAndPort);
        }
    }
}
