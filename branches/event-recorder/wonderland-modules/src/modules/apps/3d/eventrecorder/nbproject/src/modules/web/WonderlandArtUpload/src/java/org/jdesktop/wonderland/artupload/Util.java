/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.artupload;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletContext;
import org.jdesktop.lg3d.wonderland.wfs.InvalidWFSException;
import org.jdesktop.lg3d.wonderland.wfs.WFS;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.lg3d.wonderland.wfs.WFSCellNotLoadedException;
import org.jdesktop.lg3d.wonderland.wfs.WFSFactory;

/**
 *
 * @author jkaplan
 */
public class Util {
    public static final String ART_DIR_PROP = "wonderland.art.url.local";
    public static final String ART_URL_PROP = "wonderland.art.url.base";
    public static final String WFS_DIR_PROP = "wonderland.wfs.root";
    
    private static final String BASE_DIR = File.separator + ".wonderland" + 
                                           File.separator + "artUpload";
    private static final String ART_DIR = BASE_DIR + File.separator + "art";
    private static final String WFS_DIR = BASE_DIR + File.separator + "upload-wfs";

    /**
     * Get the art directory
     */
    public static File getArtDir(ServletContext context) throws IOException {
        // first try a system property
        String artDir = System.getProperty(ART_DIR_PROP);
        
        // next try a servlet config parameter
        if (artDir == null) {
            artDir = (String) context.getInitParameter(ART_DIR_PROP);
        }
        
        // try the default
        if (artDir == null) {
            artDir = "file:" + System.getProperty("user.home") + ART_DIR;
        }
        
        try {
            return new File(new URI(artDir));
        } catch (URISyntaxException use) {
            IOException ioe = new IOException(use.getMessage());
            ioe.initCause(use);
            throw ioe;
        }
    }
    
    /**
     * Get the remote art URL
     */
    public static String getArtURL(ServletContext context) {
        // first try a system property
        String artURL = System.getProperty(ART_URL_PROP);
        
        // next try a servlet config parameter
        if (artURL == null) {
            artURL = context.getInitParameter(ART_URL_PROP);
        }
        
        return artURL;
    }
    
    /**
     * Get the WFS directory
     */
    public static WFS getWFS(ServletContext context) 
            throws IOException 
    {
         // first try a system property
        String wfsDir = System.getProperty(WFS_DIR_PROP);
        
        // next try a servlet config parameter
        if (wfsDir == null) {
            wfsDir = context.getInitParameter(ART_DIR_PROP);
        }
        
        // try the default
        if (wfsDir == null) {
            wfsDir = "file:" + System.getProperty("user.home") + WFS_DIR;
        } 
        
        // decide whether to create or open the WFS
        boolean create = false;
        WFS wfs;
        
        URL wfsRootURL = new URL(wfsDir);
        if (wfsRootURL.getProtocol().equals(WFS.FILE_PROTOCOL)) {
            // see if we need to create the WFS directory.  We need to
            // create a new directory if the directory doesn't exist or
            // is empty
            File wfsRootFile = new File(wfsRootURL.getPath());
            create = !wfsRootFile.exists();
                // || wfsRootFile.list().length == 0;
        }
           
        try {
            if (create) {
                // create a new WFS
                wfs = WFSFactory.create(wfsRootURL);
                wfs.write();
            } else {
                // open existing WFS
                wfs = WFSFactory.open(wfsRootURL);
            }
        } catch (InvalidWFSException iwe) {
            IOException ioe =new IOException(iwe.getMessage());
            ioe.initCause(iwe);
            throw ioe;
        }
        
        return wfs;
    }
}
