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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jkaplan
 */
public class DownloadServlet extends HttpServlet {
    private static final Logger logger =
            Logger.getLogger(DownloadServlet.class.getName());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        ServletContext context = config.getServletContext();
        
        // check that the art url is set correctly
        if (!Util.isLocalRedirect(context) && Util.getArtURL(context) == null) {
            throw new ServletException("Art URL not specified.  Use " + 
                                       Util.ART_URL_PROP + " to set it.");
        }
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
        // first see if we have a copy of the file locally
        File file = new File(Util.getArtDir(getServletContext()), 
                             request.getPathInfo());
        if (file.exists()) {
            // if so, write it out
            response.setContentType(getServletContext().getMimeType(file.getPath()));
            response.setContentLength((int) file.length());
        
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            ServletOutputStream out = response.getOutputStream();
            
            int read;
            byte[] buffer = new byte[16384];
            while ((read = in.read(buffer)) > -1) {
                out.write(buffer, 0, read);
            }
            
            out.close();
        } else if (Util.isLocalRedirect(getServletContext())) {
            String url = Util.getArtRedirectURL(getServletContext()) + 
                         request.getPathInfo();
            RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
            rd.forward(request, response);
        } else {
            // no copy locally, redirect to the remote site
            String url = Util.getArtURL(getServletContext()) + 
                         request.getPathInfo();
            response.sendRedirect(url);
        }
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
        throw new ServletException("DownloadServlet only handles get.");
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
    public String getServletInfo() {
        return "Download artwork";
    }
}
