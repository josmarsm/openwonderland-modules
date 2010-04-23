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
package org.jdesktop.wonderland.modules.pdf.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;

/**
 * The PDFDeployer class loads a deployed PDF file.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PDFDeployer {

    /**
     * Given a URI to a PDF file, returns a DeployedPDF object that contains
     * meta-information about the PDF. If the XML deployment file does not
     * exist, then return null.
     *
     * @param pdfURI The URI to the PDF file
     * @return A DeployedPDF object associated with the PDF file
     * @throw MalformedURLException If the given PDF URI is invalid
     * @throw IOException Upon error reading the meta data information
     * @throw JAXBException Upon error parsing the meta data information
     */
    public static DeployedPDF loadDeployedPDF(String pdfURI)
            throws MalformedURLException, IOException, JAXBException {

        // Append a .xml extension to the PDF URI and load using the current
        // primary session.
        String xmlURI = pdfURI + ".xml";
        URL url = AssetUtils.getAssetURL(xmlURI);
        InputStream is = url.openStream();
        return DeployedPDF.decode(is);
    }

    /**
     * Given the URI to a PDF file, and a slide number, returns a URL to the
     * image.
     *
     * @param pdfURI The URI to the PDF file
     * @param page The page number
     */
    public static URL loadPDFPage(String pdfURI, int page)
            throws MalformedURLException {

        String pngURI = pdfURI + "." + page + ".png";
        return AssetUtils.getAssetURL(pngURI);
    }
}
