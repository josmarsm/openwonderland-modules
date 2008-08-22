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

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author jkaplan
 * @author jbarratt
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
     * @return the description for this servlet
     */
    @Override
    public String getServletInfo() {
        return "Wonderland Generic Upload Servlet";
    }
    
    /** 
     * Handles the HTTP <code>GET</code> method.<br>
     * This is not used in this implementation.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException when called
     * @throws IOException is ignored
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        throw new ServletException("Upload servlet only handles post");
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.<br>
     * Overrides default implementation.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected abstract void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;
    
    /**
     * Check that all required items are present
     * @param items the items to check
     * @return the items that are present
     */
    protected abstract List<String> checkRequired(List<FileItem> items);
     
    /**
     * Write files to the specified directory
     * @param items the list of items containing the files to write
     * @throws IOException if there is an error writing the files
     * @throws ServletException if there is an error writing the files
     */
    protected abstract void writeFiles(List<FileItem> items) 
        throws IOException, ServletException;
    
    /**
     * Find an item in a list by field name
     * @param items the list of items
     * @param name the name of the item we are trying to find
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
