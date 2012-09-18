
package org.jdesktop.wonderland.modules.ezscript.client.pdfviewer;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.pdfviewer.client.PDFViewerApp;
import org.jdesktop.wonderland.modules.pdfviewer.client.cell.PDFViewerCell;

/**
 *
 * @author JagWire
 */
@ScriptMethod 
public class LastPDFPage  implements ScriptMethodSPI {
    
    private PDFViewerCell cell = null;

    public String getFunctionName() {
        return "LastPDFPage";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cell = (PDFViewerCell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Jumps slide-show to last page.\n"
                + "-- usage: LastPDFPage(cell);";
                
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "pdf";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        PDFViewerApp app = (PDFViewerApp)cell.getApp();
        app.getWindow().lastPage();
    }
    
}
