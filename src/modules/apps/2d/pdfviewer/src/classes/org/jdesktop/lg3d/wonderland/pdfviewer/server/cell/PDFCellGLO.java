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
package org.jdesktop.lg3d.wonderland.pdfviewer.server.cell;

import com.sun.sgs.app.ClientSession;
import java.util.HashSet;
import java.util.Set;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellMessage;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFCellSetup;
import org.jdesktop.lg3d.wonderland.pdfviewer.common.PDFMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.ChecksumManagerGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;

/**
 * A server cell that demonstrates simple networking
 * @author jkaplan
 */
public class PDFCellGLO extends StationaryCellGLO
    implements CellMessageListener
{    
    private String fileName;

    public PDFCellGLO(Bounds bounds, Matrix4d center, String fileName) {
        super (bounds, center);

        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
   
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.pdfviewer.client.cell.PDFCell";
    }
    
    public CellSetup getSetupData() {
        String checksum = ChecksumManagerGLO.getChecksum(fileName);
        return new PDFCellSetup(baseUrl, fileName, checksum);
    }

    public void receivedMessage(ClientSession client, CellMessage message) {
    }
}
