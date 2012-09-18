
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
public class PausePDFSlideShow  implements ScriptMethodSPI {
    
    private PDFViewerCell cell = null;

    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "PausePDFSlideShow";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cell = (PDFViewerCell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Pauses a playing pdf slide-show.\n"
                + "-- usage: PausePDFSlideShow(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "pdf";
    }

    public void run() {
        PDFViewerApp app = (PDFViewerApp)cell.getApp();
        if(app.getWindow().isPlaying()) {
            app.getWindow().togglePlay();
        }
    }
    
}
