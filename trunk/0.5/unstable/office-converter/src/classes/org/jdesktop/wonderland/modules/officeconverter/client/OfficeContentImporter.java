/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.officeconverter.client;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.content.ContentImportManager;
import org.jdesktop.wonderland.client.jme.content.AbstractContentImporter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * ContentImporter that doesn't actually import, but converts from
 * a format understood by openoffice to PDF.
 * @author Bernard Horan
 */
@Plugin
public class OfficeContentImporter extends AbstractContentImporter
        implements ClientPlugin {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(OfficeContentImporter.class.getName());

    // The current session to the server, needed to fetch the content repo
    private ServerSessionManager loginInfo = null;

    // An internal BaseClientPlugin that handles activate and deactivate by
    // delegating to the superclass methods
    private BaseClientPlugin plugin = null;

    /**
     * @inheritDoc()
     */
    public void initialize(ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
        this.plugin = new BaseClientPlugin() {
            @Override
            protected void activate() {
                OfficeContentImporter.this.register();
            }

            @Override
            protected void deactivate() {
                OfficeContentImporter.this.deregister();
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
        String[] extensions = new String[19];
        extensions[0] = "doc";
        extensions[1] = "docx";
        extensions[2] = "ppt";
        extensions[3] = "odt";
        extensions[4] = "swf";//StarWriter
        extensions[5] = "rtf";
        extensions[6] = "wpd";//Word Perfect
        extensions[7] = "txt";
        extensions[8] = "ods";
        extensions[9] = "sxc";//StarOffice 1.0 spreadsheet
        extensions[10] = "xls";
        extensions[11] = "xlsx";
        extensions[12] = "csv";
        extensions[13] = "tsv";//Tab separated values
        extensions[14] = "odp";
        extensions[15] = "sxi";//Star Impress
        extensions[16] = "pptx";
        extensions[17] = "odg";
        extensions[18] = "svg";
        return extensions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String isContentExists(File file) {
        //We're converting, not really uploading, so it can't exist
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String uploadContent(File inputFile) throws IOException {
        //Convert the file
        File convertedFile = convertFile(inputFile);
        LOGGER.warning("Converted file: " + convertedFile);
        // Ask the content manager for whom handles PDFs
        // and dispatch there.
        ContentImportManager cim = ContentImportManager.getContentImportManager();
        final AbstractContentImporter importer = (AbstractContentImporter) cim.getContentImporter("pdf", true);
        if (importer == null) {
            LOGGER.severe("No importer found for " + convertedFile.getName());
            throw new IOException("No importer found for " + convertedFile.getName());
        }
        return importer.uploadContent(convertedFile);
    }

    private File convertFile(File inputFile) throws IOException {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String serverURL = loginInfo.getServerURL();
            HttpPost post = new HttpPost(serverURL + "/office-converter/converter/service");
            //LOGGER.warning("URI: " + post.getURI());
            post.addHeader("Accept", "application/pdf");

            String extension = FilenameUtils.getExtension(inputFile.getName());
            //LOGGER.warning("Extension: " + extension);
            DocumentFormat format = new DefaultDocumentFormatRegistry().getFormatByFileExtension(extension);
            //LOGGER.warning("Format: " + format.getMimeType());

            HttpEntity postEntity = new FileEntity(inputFile, format.getMimeType());
            post.setEntity(postEntity);


            HttpResponse response = httpClient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                LOGGER.warning("Status Code: " + statusLine.getStatusCode());
                LOGGER.warning("Status: " + statusLine.getReasonPhrase());
                throw new IOException(statusLine.getReasonPhrase());
            }

            File outputFile = File.createTempFile(inputFile.getName(), ".pdf");
            outputFile.deleteOnExit();
            LOGGER.warning("Output filename: " + outputFile.getName());
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            HttpEntity responseEntity = response.getEntity();
            responseEntity.writeTo(outputStream);
            outputStream.close();
            return outputFile;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "failed to convert file", e);
            throw new IOException("Failed to convert file", e);
        }
    }

    
}
