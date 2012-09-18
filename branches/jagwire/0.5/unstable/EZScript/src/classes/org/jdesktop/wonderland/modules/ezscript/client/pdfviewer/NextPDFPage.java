
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
public class NextPDFPage  implements ScriptMethodSPI {
    
    private PDFViewerCell cell = null;

    public String getFunctionName() {
        return "NextPDFPage";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cell = (PDFViewerCell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Jumps a slide-show to the next page.\n"
                + "-- usage: NextPDFPage(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "pdf";
    }

    public void run() {
        PDFViewerApp app = (PDFViewerApp)cell.getApp();
        app.getWindow().nextPage();
    }
    
}
