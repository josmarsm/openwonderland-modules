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
package org.jdesktop.lg3d.wonderland.pdfviewer.common;

import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;

/**
 *
 * @author jkaplan
 */
public class PDFCellSetup implements CellSetup {
    private String baseURL;
    private String fileName;
    private String checksum;
    
    public PDFCellSetup(String baseURL, String fileName, String checksum) {
        this.baseURL = baseURL;
        this.fileName = fileName;
        this.checksum = checksum;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
