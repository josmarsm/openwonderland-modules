/**
 * Project Looking Glass
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */

package org.jdesktop.wonderland.artupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.ModelCellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SimpleTerrainCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.lg3d.wonderland.wfs.WFS;
import org.jdesktop.lg3d.wonderland.wfs.WFSCell;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellNotLoadedException;

/**
 *
 * @author jkaplan (jbarratt)
 */
public class FileUploadServlet extends UploadServlet {
    
    
    private static final Logger logger =
            Logger.getLogger(FileUploadServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        try {
            File fileDir = Util.getShareDir(config.getServletContext());
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            
            logger.info("File directory is " + fileDir.getCanonicalPath());
        } catch (IOException ioe) {
            throw new ServletException(ioe);
        }
    }
    
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Wonderland File Upload Servlet";
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        try {
            List<FileItem> items = (List<FileItem>) upload.parseRequest(request);
            
            // check for errors
            List<String> errors = checkRequired(items);
            if (!errors.isEmpty()) {
                throw new ServletException("Unable to load " +
                                           errors.toString());
            }
            
            // write file
            writeFiles(items);
        } catch (FileUploadException fue) {
            throw new ServletException(fue);
        }
    }

    /**
     * Check that all required items are present
     */
    protected List<String> checkRequired(List<FileItem> items) {
        Map<String, ItemValidator> validators = new HashMap<String, ItemValidator>();
        validators.put("user", new FieldValidator("user"));
        validators.put("file", new FileValidator("file"));
    
        List<String> out = new ArrayList<String>();
        for (FileItem item : items) {
            ItemValidator v = validators.remove(item.getFieldName());
            if (v == null) {
                out.add("Unknown field " + item.getFieldName());
            } else {
                String res = v.validate(item);
                if (res != null) {
                    out.add(res);
                }
            }
        }
        
        // any validators left are missing
        for (ItemValidator v : validators.values()) {
            out.add("Missing value for field " + v.getName());
        }
        
        return out;
    }
     
    /**
     * Write files to the art directory
     * @param items the list of items containing the files to write
     * @throws ServletException if there is an error writing the files
     */
    protected void writeFiles(List<FileItem> items) 
        throws IOException, ServletException
    {
        // get the value of the "name" field
        FileItem nameItem = findItem(items, "user");
        String name = nameItem.getString();
        
        // write the model file
        FileItem fileItem = findItem(items, "file");
        File fileDir = new File(Util.getShareDir(getServletContext()), name);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        File theFile = new File(fileDir, fileItem.getName());
        
        try {
            fileItem.write(theFile);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
