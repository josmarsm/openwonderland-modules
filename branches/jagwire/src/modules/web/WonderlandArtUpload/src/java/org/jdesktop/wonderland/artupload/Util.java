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
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.jdesktop.lg3d.wonderland.servlet.WonderlandServletUtil;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellDirectory;

/**
 *
 * @author jkaplan
 * @author jbarratt
 */
public class Util {
    public static final String ART_DIR_PROP   = "wonderland.art.url.local";
    public static final String ART_URL_PROP   = "wonderland.art.url.base";
    public static final String ART_REDIR_PROP = "wonderland.art.url.redirect";
    public static final String WFS_ROOT_PROP  = "wonderland.wfs.root";
    public static final String WFS_DIR_PROP   = "wonderland.wfs.upload.dir";
    public static final String SHARE_DIR_PROP   = "wonderland.share.url.local";
    
    private static final String BASE_DIR = File.separator + ".wonderland"; 
    private static final String ART_UPLOAD_DIR = BASE_DIR + File.separator + "artUpload";
    private static final String ART_DIR = ART_UPLOAD_DIR + File.separator + "art";
    private static final String WFS_DIR = ART_UPLOAD_DIR + File.separator + "upload-wfs";
    private static final String SHARE_DIR = BASE_DIR + File.separator + "sharingUpload";

    private static final Logger logger = Logger.getLogger(Util.class.getName());
    
    /**
     * Get the art directory
     */
    public static File getArtDir(ServletContext context) throws IOException {
        String defaultArtDir = "file:" + System.getProperty("user.home") +
                               ART_DIR;
        
        String artDir = WonderlandServletUtil.getProperty(ART_DIR_PROP, context, 
                                                          defaultArtDir);
        
        return new File(new URL(artDir).getPath());
    }
    
    /**
     * Get the share directory
     */    
    public static File getShareDir(ServletContext context) throws IOException {
        String defaultShareDir = "file:" + System.getProperty("user.home") +
                                SHARE_DIR;
        
        String shareDir = WonderlandServletUtil.getProperty(SHARE_DIR_PROP, context, 
                                                          defaultShareDir);
        
        return new File(new URL(shareDir).getPath());
    }
    
    /**
     * Determine whether art URL is local or remote
     */
    public static boolean isLocalRedirect(ServletContext context) {
        return (getArtRedirectURL(context) != null);
    }
    
    /**
     * Get the remote art URL
     */
    public static String getArtURL(ServletContext context) {
        return WonderlandServletUtil.getProperty(ART_URL_PROP, context);
    }
    
    /**
     * Get the local art URL
     */
    public static String getArtRedirectURL(ServletContext context) {
        return WonderlandServletUtil.getProperty(ART_REDIR_PROP, context);
    }
    
    /**
     * Get the WFS directory
     */
    public static WFSCellDirectory getWFS(ServletContext context) 
            throws IOException 
    {
        // get the wfs root
        String wfsRootDefault = "file:" + System.getProperty("user.home") + 
                                WFS_DIR; 
        String wfsRoot = WonderlandServletUtil.getProperty(WFS_ROOT_PROP, 
                                context, wfsRootDefault);
        URL wfsRootURL = new URL(wfsRoot);
        
        // get the subdirectory, if any
        String wfsDir = WonderlandServletUtil.getProperty(WFS_DIR_PROP, 
                                context);
        
        // open or create the directory
        return WonderlandServletUtil.openWFS(wfsRootURL, wfsDir, true);
    }
}
