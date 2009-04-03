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
import org.jdesktop.wonderland.common.AssetURI;
import org.w3c.dom.Document;

/**
 *
 * @author jordanslott
 */
public class WhiteboardClientUtils {

    public static SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
    private static final Logger logger = Logger.getLogger(WhiteboardClientUtils.class.getName());

    public static Document openDocument(String uri) {
        logger.fine("opening SVG document with URI: " + uri);
        Document doc = null;

        // Use the WL asset manager to fetch the asset
        AssetURI assetURI = AssetURI.uriFactory(uri);
        Asset asset = AssetManager.getAssetManager().getAsset(assetURI);
        if (asset == null || AssetManager.getAssetManager().waitForAsset(asset) == false) {
            return null;
        }

        try {
            doc = factory.createDocument(null, new FileReader(asset.getLocalCacheFile()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        logger.fine("SVG doc: " + doc);
        return doc;
    }
}
