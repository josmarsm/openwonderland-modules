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
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.lg3d.wonderland.wfs.WFSCell;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellNotLoadedException;

/**
 *
 * @author jkaplan
 */
public class UploadServlet extends HttpServlet {
    
    
    private static final Logger logger =
            Logger.getLogger(UploadServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        try {
            File artDir = Util.getArtDir(config.getServletContext());
            if (!artDir.exists()) {
                artDir.mkdirs();
                new File(artDir, "models").mkdir();
                new File(artDir, "textures").mkdir();
            }
            
            logger.info("Art directory is " + artDir.getCanonicalPath());
        
            WFSCellDirectory dir = Util.getWFS(config.getServletContext());
            logger.info("WFS directory at " + dir.getPathName());
        } catch (IOException ioe) {
            throw new ServletException(ioe);
        }
    }
    
    /** 
     * Returns a short description of the servlet.
     * @return the description for this servlet
     */
    @Override
    public String getServletInfo() {
        return "Wonderland Art Upload Servlet";
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
            
            // write models and textures
            writeFiles(items);
            
            // create WFS file
            writeWFS(items);
        } catch (FileUploadException fue) {
            throw new ServletException(fue);
        }
    }

    /**
     * Check that all required items are present
     * @param items the items to check
     * @return the items that are present
     */
    private List<String> checkRequired(List<FileItem> items) {
        Map<String, ItemValidator> validators = new HashMap<String, ItemValidator>();
        validators.put("name", new FieldValidator("name"));
        validators.put("xbounds", new NumberFieldValidator("xbounds"));
        validators.put("ybounds", new NumberFieldValidator("ybounds"));
        validators.put("zbounds", new NumberFieldValidator("zbounds")); 
        validators.put("xloc", new NumberFieldValidator("xloc"));
        validators.put("yloc", new NumberFieldValidator("yloc"));
        validators.put("zloc", new NumberFieldValidator("zloc"));
        validators.put("xrot", new NumberFieldValidator("xrot"));
        validators.put("yrot", new NumberFieldValidator("yrot"));
        validators.put("zrot", new NumberFieldValidator("zrot"));
        validators.put("arot", new NumberFieldValidator("arot"));
        validators.put("model", new FileValidator("model"));
        validators.put("textures", new FileValidator("textures"));
    
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
     * Write files to the specified directory
     * @param items the list of items containing the files to write
     * @throws IOException if there is an error writing the files
     * @throws ServletException if there is an error writing the files
     */
    private void writeFiles(List<FileItem> items) 
        throws IOException, ServletException
    {
        // get the value of the "name" field
        FileItem nameItem = findItem(items, "name");
        String name = nameItem.getString();
        
        // write the model file
        FileItem modelItem = findItem(items, "model");
        File modelsDir = new File(Util.getArtDir(getServletContext()), "models");
        File modelFile = new File(modelsDir, name + ".j3s.gz");
        
        try {
            modelItem.write(modelFile);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        
        // unzip the textures
        FileItem texturesItem = findItem(items, "textures");
        ZipInputStream zin = new ZipInputStream(texturesItem.getInputStream());
        
        ZipEntry entry;
        File curDir = new File(Util.getArtDir(getServletContext()), "textures");
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                File dir = new File(curDir, entry.getName());
                dir.mkdirs();
            } else {
                // write the unzipped texture data
                File texture = new File(curDir, entry.getName());                
                FileOutputStream out = new FileOutputStream(texture);

                byte[] buffer;
                if (entry.getSize() > -1) {
                    buffer = new byte[(int) entry.getSize()];
                } else {
                    buffer = new byte[64 * 1024];
                }
                
                int read = 0;
                while ((read = zin.read(buffer, 0, buffer.length)) > -1) {
                    out.write(buffer, 0, read);
                }
                out.close();
            }
        }
    }
    
    /**
     * Write WFS file to the WFS directory
     * @param items the list of items containing the data to write
     * @throws ServletException if there is an error writing the files
     */
    private void writeWFS(List<FileItem> items) 
        throws IOException, ServletException
    {
        // get the value of the "name" field
        FileItem nameItem = findItem(items, "name");
        String name = nameItem.getString();
        
        // parse submitted values in a cell setup object
        BasicCellGLOSetup<ModelCellSetup> setup =
                new BasicCellGLOSetup<ModelCellSetup>();        
 
        // set the class of cellGLO to create
        setup.setCellGLOClassName("org.jdesktop.lg3d.wonderland.darkstar.server.cell.SimpleTerrainCellGLO");
        
        // parse the origin from xloc, yloc, zloc
        FileItem xLocItem = findItem(items, "xloc");
        FileItem yLocItem = findItem(items, "yloc");
        FileItem zLocItem = findItem(items, "zloc");
        
        double[] origin = new double[3];
        origin[0] = Double.parseDouble(xLocItem.getString());
        origin[1] = Double.parseDouble(yLocItem.getString());
        origin[2] = Double.parseDouble(zLocItem.getString());
        setup.setOrigin(origin);
        
        // parse the rotation from xrot, yrot and zrot
        FileItem xRotItem = findItem(items, "xrot");
        FileItem yRotItem = findItem(items, "yrot");
        FileItem zRotItem = findItem(items, "zrot");
        FileItem aRotItem = findItem(items, "arot");
        
        double[] rot = new double[4];
        rot[0] = Double.parseDouble(xRotItem.getString());
        rot[1] = Double.parseDouble(yRotItem.getString());
        rot[2] = Double.parseDouble(zRotItem.getString());
        rot[3] = Double.parseDouble(aRotItem.getString());
        setup.setRotation(rot);
        
        // read the bounds information
        FileItem xBoundsItem = findItem(items, "xbounds");
        FileItem yBoundsItem = findItem(items, "ybounds");
        FileItem zBoundsItem = findItem(items, "zbounds");
        
        // read the bounds
        double[] bounds = new double[3];
        bounds[0] = Double.parseDouble(xBoundsItem.getString());
        bounds[1] = Double.parseDouble(yBoundsItem.getString());
        bounds[2] = Double.parseDouble(zBoundsItem.getString());
        
        // the bounds radius for now is just the maximum of the three
        // values specified.
        setup.setBoundsType("BOX");
        Arrays.sort(bounds);
        setup.setBoundsRadius(bounds[2]);
        
        // add the model information
        ModelCellSetup mcs = new ModelCellSetup();
        mcs.setModelFile("models/" + name + ".j3s.gz");
        setup.setCellSetup(mcs);
        
        // create the wfs cell
        try {
            WFSCellDirectory dir = Util.getWFS(getServletContext());
            WFSCell cell = dir.addCell(name);
            cell.setCellSetup(setup);
        
            // write out the wfs file
            dir.write();
        } catch (InvalidWFSCellException iwe) {
            logger.log(Level.WARNING, "Error writing " + setup, iwe);
        } catch (WFSCellNotLoadedException wcnle) {
            logger.log(Level.WARNING, "Error writing " + setup, wcnle);
        }
    }
    
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
