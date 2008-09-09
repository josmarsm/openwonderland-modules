/**
 * Project Looking Glass
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
 * $Author$
 */

package org.jdesktop.wonderland.artupload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author jkaplan
 * @author jbarratt
 */
public class FileUploadServlet extends UploadServlet {
    private Set<File> writtenFiles;
    
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
     * @return a string describing this servlet
     */
    @Override
    public String getServletInfo() {
        return "Wonderland File Upload Servlet";
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if there's an exception in the servlet
     * @throws IOException if there's an exception writing files, or out to the response
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
            
            // write files
            writeFiles(items);
            writeStatus(response);
        } catch (FileUploadException fue) {
            throw new ServletException(fue);
        }
    }

    /**
     * Check that all required items are present
     * @param items
     * @return 
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
     * @throws IOException if there is an error writing the files
     * @throws ServletException if there is an error writing the files
     */
    protected void writeFiles(List<FileItem> items) 
        throws IOException, ServletException
    {
        writtenFiles = new HashSet<File>();
        // get the value of the "name" field
        FileItem nameItem = findItem(items, "user");
        String name = nameItem.getString();
        
        // write the model file
        FileItem fileItem = findItem(items, "file");
        File fileDir = new File(Util.getShareDir(getServletContext()), name);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        String fileName = fileItem.getName();
        //Fix for users of IE that include the whole path
        if (fileName != null) {
            fileName = FilenameUtils.getName(fileName);
        }
        File theFile = new File(fileDir, fileName);
        
        try {
            fileItem.write(theFile);
            writtenFiles.add(theFile);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void writeStatus(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html ");
        out.println("PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html>");
        out.println("<head>");
        out.println("</head>");
        out.println("<h1>Success</h1>");
        out.println("<p>The following files have been written</p>");
        out.println("<ul>");
        for (Iterator<File> it = writtenFiles.iterator(); it.hasNext();) {
            File f = it.next();
            out.println("<li>");
            out.println(f.toString());
            out.println("</li>");
        }
        out.println("</ul>");
        out.println("<hr style=\"width: 100%; height: 2px;\">");
        out.println("<p>");
        out.println(new Date().toString());
        out.println("</p>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
    }
}
