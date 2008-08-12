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
public abstract class UploadServlet extends HttpServlet {
    
    private static final Logger logger =
            Logger.getLogger(UploadServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Wonderland Generic Upload Servlet";
    }
    
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        throw new ServletException("Upload servlet only handles post");
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected abstract void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;
    
    /**
     * Check that all required items are present
     */
    protected abstract List<String> checkRequired(List<FileItem> items);
     
    /**
     * Write files to the art directory
     * @param items the list of items containing the files to write
     * @throws ServletException if there is an error writing the files
     */
    protected abstract void writeFiles(List<FileItem> items) 
        throws IOException, ServletException;
    
    /**
     * Find an item in a list by field name
     * @return the item, or null if it is not found
     */
    protected FileItem findItem(List<FileItem> items, String name) {
        FileItem out = null;
        
        for (FileItem item : items) {
            if (item.getFieldName().equals(name)) {
                out = item;
                break;
            }
        }
        
        return out;
    }
    
    interface ItemValidator {
        public String getName();
        
        /**
         * Validate the given item.  Return null if the item is ok, or
         * an error string if not
         */
        public String validate(FileItem item);
    }
    
    class FieldValidator implements ItemValidator {
        private String name;
        
        public FieldValidator(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public String validate(FileItem item) {
            if (!item.isFormField()) {
                return getName() + " not form field.";
            }
            
            if (item.getString() == null || item.getString().length() == 0) {
                return getName() + " cannot be empty.";
            }
            
            return null;
        }
    }
    
    class NumberFieldValidator extends FieldValidator {
        public NumberFieldValidator(String name) {
            super (name);
        }
        
        @Override
        public String validate(FileItem item) {
            String out = super.validate(item);
            if (out != null) {
                return out;
            }
            
            try {
                Double.parseDouble(item.getString());
            } catch (NumberFormatException nfe) {
                return getName() + " must be a number";
            }
            
            return null;
        }
    }
    
    class FileValidator implements ItemValidator {
        private String name;
        
        public FileValidator(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String validate(FileItem item) {
            if (item.isFormField()) {
                return getName() + " must be a file.";
            }
            
            if (item.getSize() == 0) {
                return getName() + " must not be empty";
            }
            
            return null;
        }
    }
}
