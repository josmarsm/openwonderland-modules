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
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellSetup;
import org.jdesktop.lg3d.wonderland.scenemanager.AssetManager;
import org.jdesktop.lg3d.wonderland.scenemanager.AssetManager.FileCompression;

/**
 *
 * @author jkaplan
 */
public class PDFCell extends Cell 
        implements ExtendedClientChannelListener 
{
    private static final Logger logger =
            Logger.getLogger(PDFCell.class.getName());
     
    private PDFFile pdf;

    public PDFCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    public void setup(CellSetup setup) {
        PDFCellSetup pdfSetup = (PDFCellSetup) setup;

        pdf = loadPDFFile(pdfSetup);
        if (pdf != null) {
            logger.info("Loaded PDF: " + pdf.getNumPages() + " pages.");
        }

        
    }

    /**
     * Load a PDF file based on a PDF cell setup message
     * @param pdfSetup the pdf cell setup message containing filename, etc
     * @return a PDFFile with the given content, or null if the given file
     * could not be found
     */
    protected PDFFile loadPDFFile(PDFCellSetup pdfSetup) {

        AssetManager am = AssetManager.getAssetManager();
        File file = am.loadFile(pdfSetup.getBaseURL(), 
                                pdfSetup.getFileName(),
                                pdfSetup.getChecksum(), 
                                FileCompression.NONE);

        try {
            // load the file using the asset manager
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            ByteBuffer fileBuf = fc.map(MapMode.READ_ONLY, 0, file.length());
        
            // parse the PDF file
            return new PDFFile(fileBuf);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to read PDF File: " + file, ioe); 
            return null;
        }
    }

    public void receivedMessage(ClientChannel channel, SessionId session, 
                                byte[] data) 
    {
    }

    public void leftChannel(ClientChannel channel) {
    }

   
}
