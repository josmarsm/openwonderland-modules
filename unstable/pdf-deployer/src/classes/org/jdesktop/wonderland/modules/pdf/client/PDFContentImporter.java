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
package org.jdesktop.wonderland.modules.pdf.client;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.jme.content.AbstractContentImporter;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 * A content importer for PDF files. This importer parses the PDF files and
 * generates meta-information to upload to WebDav. This importer also generates
 * images for each of the slides and uploads them individually to WebDav.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class PDFContentImporter extends AbstractContentImporter
        implements ClientPlugin {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(PDFContentImporter.class.getName());

    // The current session to the server, needed to fetch the content repo
    private ServerSessionManager loginInfo = null;

    // An internal BaseClientPlugin that handles activate and deactivate by
    // delegating to the superclass methods
    private BaseClientPlugin plugin = null;

    // Used to increase the resolution at which we render PDF pages. If this
    // is 1, then the rendered page is the same as the reported dimensions
    // of the page. For pages with text, this is often way too low resolution
    // to read it at any distance, hence the 2.0 default.
    private static final float RESOLUTION_RENDER_FACTOR = 2.0f;

    /**
     * @inheritDoc()
     */
    public void initialize(ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
        this.plugin = new BaseClientPlugin() {
            @Override
            protected void activate() {
                PDFContentImporter.this.register();
            }

            @Override
            protected void deactivate() {
                PDFContentImporter.this.deregister();
            }

        };

        // initialize our plugin
        plugin.initialize(loginInfo);
    }

    /**
     * @inheritDoc()
     */
    public void cleanup() {
        plugin.cleanup();
    }

    /**
     * Registers the content importer.
     */
    protected void register() {
        ContentImportManager m = ContentImportManager.getContentImportManager();
        m.registerContentImporter(this);
    }

    /**
     * Deregisters the content importer.
     */
    protected void deregister() {
        ContentImportManager m = ContentImportManager.getContentImportManager();
        m.unregisterContentImporter(this);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[] { "pdf" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String isContentExists(File file) {
        // Fetch the pdf/ directory in the user's WebDav area. Check to see if
        // a <File Name>.pdf/<File Name>pdf file exists. If so, return its URI
        // otherwise, return null.
        String pdfName = file.getName() + "/" + file.getName();
        try {
            ContentCollection pdfRoot = getPDFRoot();
            if (pdfRoot.getChild(pdfName) != null) {
                return "wlcontent://users/" + loginInfo.getUsername() +
                        "/pdf/" + pdfName;
            }
            return null;
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Error while try to find " + pdfName +
                    " in content repository", excp);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadContent(File file) throws IOException {
        // Load in the file and parse as a PDF file. Upon error log and error
        // and return null;
        String pdfFileName = file.getName();
        PDFFile pdfFile = null;
        try {
            pdfFile = getPDFFile(file);
        } catch (java.io.IOException excp) {
            LOGGER.log(Level.WARNING, "Failed to load as PDF: " +
                    file.getAbsolutePath(), excp);
            return null;
        }

        // Upload the PDF file to WebDav under the pdf/ directory. Name it
        // <File Name>.pdf/<File Name>.pdf, creating the directories if
        // necessary
        try {
            uploadPDFFile(file);
        } catch (IOException excp) {
            LOGGER.log(Level.WARNING, "Unable to upload PDF: " +
                    file.getAbsolutePath(), excp);
            return null;
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to upload PDF: " +
                    file.getAbsolutePath(), excp);
            return null;
        }

        // Using the information parsed from the PDF File, create a new
        // DeployedPDF object, to be written to WebDav
        DeployedPDF deployedPDF = new DeployedPDF();
        deployedPDF.setPdfFileName(pdfFileName);
        deployedPDF.setNumberOfSlides(pdfFile.getNumPages());

        // Keep track of the maximum width and height for all of the pages
        int maximumWidth = 0;
        int maximumHeight = 0;

        // Loop through each of the images. Convert each into a PNG and upload
        // to WebDav.
        for (int page = 1; page <= pdfFile.getNumPages(); page++) {
            // Fetch the buffered image for the page and write to a file in
            // WebDav.
            BufferedImage pageTexture = getPageImage(pdfFile, page);
            try {
                uploadPageImage(pdfFileName, pageTexture, page);
            } catch (IOException excp) {
                LOGGER.log(Level.WARNING, "Unable to upload page image from " +
                        file.getAbsolutePath() + ", page number " + page, excp);
                return null;
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to upload page image from " +
                        file.getAbsolutePath() + ", page number " + page, excp);
                return null;
            }

            // Update the running track of the maximum page width and height
            maximumWidth = Math.max(maximumWidth, pageTexture.getWidth());
            maximumHeight = Math.max(maximumHeight, pageTexture.getHeight());
        }

        // Set the maximum page width and height in the PDF meta data and upload
        // to WebDav
        deployedPDF.setMaximumSlideWidth(maximumWidth);
        deployedPDF.setMaximumSlideHeight(maximumHeight);
        try {
            uploadDeployedPDF(pdfFileName, deployedPDF);
        } catch (JAXBException excp) {
            LOGGER.log(Level.WARNING, "Unable to upload deployed module from " +
                    file.getAbsolutePath(), excp);
            return null;
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to upload deployed module from " +
                    file.getAbsolutePath(), excp);
            return null;
        }

        // Return a URI referring to the PDF file
        ServerSessionManager session = LoginManager.getPrimary();
        return "wlcontent://users/" + session.getUsername() + "/pdf/" +
                pdfFileName + "/" + pdfFileName;
    }

    /**
     * Given a File, returns a PDFFile representing the file.
     *
     * @param file The File of the data
     * @return The PDF representing the file
     * @throw IOException Upon error reading the file
     */
    private PDFFile getPDFFile(File file) throws IOException {
        // Load the File into a byte buffer, using the length of the file
        int length = (int)file.length();
        byte b[] = new byte[length];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(b);
        ByteBuffer bb = ByteBuffer.wrap(b);

        // Create a new PDFFile based upon the byte buffer and returnd
        return new PDFFile(bb);
    }

    /**
     * Takes the page number and a pdf file and returns a buffered image that
     * represents that page.
     *
     * @param pdfFile The PDFFile object
     * @param pageNumber The page number
     * @return The image of the specified page
     */
    private BufferedImage getPageImage(PDFFile pdfFile, int pageNumber) {

        // Fetch the current page and the width and height and image from that
        PDFPage page = pdfFile.getPage(pageNumber, true);
        int height = (int)(page.getHeight() *RESOLUTION_RENDER_FACTOR);
        int width = (int)(page.getWidth() *RESOLUTION_RENDER_FACTOR);
        Image image = page.getImage(width, height, null, null, true, true);

        // Convert into a buffered image and return
        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        return bi;
    }

    /**
     * Returns the root directory for all PDF files, pdf/ under the user's
     * WebDav directory.
     */
    private ContentCollection getPDFRoot() throws ContentRepositoryException {
        // Fetch the user's root using the current primary server. It should
        // be ok to use the primary server at this point
        ContentRepositoryRegistry r = ContentRepositoryRegistry.getInstance();
        ServerSessionManager session = LoginManager.getPrimary();

        // Try to find the pdf/ directory if it exists, otherwise, create it
        ContentCollection userRoot = r.getRepository(session).getUserRoot();
        ContentNode node = (ContentNode) userRoot.getChild("pdf");
        if (node == null) {
            node = (ContentNode) userRoot.createChild("pdf", Type.COLLECTION);
        }
        else if (!(node instanceof ContentCollection)) {
            node.getParent().removeChild("pdf");
            node = (ContentNode) userRoot.createChild("pdf", Type.COLLECTION);
        }
        return (ContentCollection) node;
    }

    /**
     * Uploads the given PDF file to the user's WebDav area. Write the file into
     * the pdf/ directory, and put it under at <File Name>.pdf/<File Name>.pdf,
     * creating the directories that are necessary.
     *
     * @param file The PDF File to upload to WebDav
     * @throw ContentRepositoryException Upon error uploading to WebDav
     * @throw IOException Upon error writing to WebDav
     */
    private void uploadPDFFile(File file)
            throws ContentRepositoryException, IOException {

        // Fetch the pdf/ directory in the user's WebDav area. Check to see if
        // a <File Name>.pdf/ directory exists, otherwise create it.
        String pdfName = file.getName();
        ContentCollection pdfRoot = getPDFRoot();
        ContentNode node = pdfRoot.getChild(pdfName);
        if (node == null) {
            // Create the directory if it does not exist.
            node = pdfRoot.createChild(pdfName, Type.COLLECTION);
        }
        else if (!(node instanceof ContentCollection)) {
            // If it does exist, but is not a directory, then delete it and
            // recreate the directory.
            node.getParent().removeChild(pdfName);
            node = pdfRoot.createChild(pdfName, Type.COLLECTION);
        }
        ContentCollection pdfDir = (ContentCollection)node;

        // Beneath the <File Name>.pdf/ directory, create a new resource for
        // the PDF File, removing it if it already exists.
        ContentNode resource = pdfDir.getChild(pdfName);
        if (resource == null) {
            // Create the resource if it does not exist.
            resource = pdfDir.createChild(pdfName, Type.RESOURCE);
        }
        else if (!(resource instanceof ContentResource)) {
            // If it does exist, but is not a resource, then delete it and
            // recreate the resource
            resource.getParent().removeChild(pdfName);
            resource = pdfDir.createChild(pdfName, Type.RESOURCE);
        }
        
        // Upload the file to WebDav
        ((ContentResource)resource).put(file);
    }

    /**
     * Takes the name of the PDF File name, the buffered image for a given page
     * and its page number, and saves a PDF file to WebDav for the page.
     *
     * @param fName The name of the PDF file
     * @param image The buffered image for the page
     * @param page The page number
     * @throw ContentRepositoryException Upon error uploading to WebDav
     * @throw IOException Upon error writing to WebDav
     */
    private void uploadPageImage(String fName, BufferedImage image, int page)
            throws ContentRepositoryException, IOException {

        // Fetch the directory pdf/<File Name>.pdf, assuming it exists
        ContentCollection pdfRoot = getPDFRoot();
        ContentCollection pdfDir = (ContentCollection)pdfRoot.getChild(fName);

        // Check to see if there is a child named <File Name>.pdf.<page>.png and
        // create it if it does not yet exist.
        String imageName = fName + "." + page + ".png";
        ContentNode resource = pdfDir.getChild(imageName);
        if (resource == null) {
            // Create the resource if it does not exist.
            resource = pdfDir.createChild(imageName, Type.RESOURCE);
        }
        else if (!(resource instanceof ContentResource)) {
            // If it does exist, but is not a resource, then delete it and
            // recreate the resource
            resource.getParent().removeChild(imageName);
            resource = pdfDir.createChild(imageName, Type.RESOURCE);
        }

        // Write the buffered image to a file and upload to webdav
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        ((ContentResource)resource).put(os.toByteArray());
    }

    /**
     * Takes the DeployedPDF object and writes it to WebDav, given the name of
     * the PDF file
     *
     * @param fName The name of the PDF file
     * @param deployedPDF The DeployedPDF object
     * @throw ContentRepositoryException Upon error uploading to WebDav
     */
    private void uploadDeployedPDF(String fName, DeployedPDF deployedPDF)
            throws ContentRepositoryException, JAXBException {
        
        // Fetch the directory pdf/<File Name>.pdf, assuming it exists
        ContentCollection pdfRoot = getPDFRoot();
        ContentCollection pdfDir = (ContentCollection)pdfRoot.getChild(fName);

        // Check to see if there is a child named <File Name>.pdf.xml and
        // create it if it does not yet exist.
        String xmlName = fName + ".xml";
        ContentNode resource = pdfDir.getChild(xmlName);
        if (resource == null) {
            // Create the resource if it does not exist.
            resource = pdfDir.createChild(xmlName, Type.RESOURCE);
        }
        else if (!(resource instanceof ContentResource)) {
            // If it does exist, but is not a resource, then delete it and
            // recreate the resource
            resource.getParent().removeChild(xmlName);
            resource = pdfDir.createChild(xmlName, Type.RESOURCE);
        }

        // Write the XML to a file and upload to webdav
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        deployedPDF.encode(os);
        ((ContentResource)resource).put(os.toByteArray());
    }
}
