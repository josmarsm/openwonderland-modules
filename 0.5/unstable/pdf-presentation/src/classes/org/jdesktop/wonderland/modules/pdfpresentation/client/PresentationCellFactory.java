/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.pdfpresentation.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdf.client.DeployedPDF;
import org.jdesktop.wonderland.modules.pdf.client.PDFDeployer;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationCellServerState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout;
import org.jdesktop.wonderland.modules.pdfpresentation.common.PresentationLayout.LayoutType;

@CellFactory
public class PresentationCellFactory implements CellFactorySPI{
    public String[] getExtensions() {
        return new String[] {"pdf"};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        Logger.getLogger(PresentationCellFactory.class.getName()).warning("In PDFSpreaderCellFactory!");

        PresentationCellServerState state = new PresentationCellServerState();

        state.setCreatorName(LoginManager.getPrimary().getUsername());

        // XXX HACK XXX
        // Provide a hint so that the slides appear above the floor
        // XXX HACK XXX
        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1, 2, 1);
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);

        DeployedPDF deployedPDF = null;
        if (props != null) {
           String uri = props.getProperty("content-uri");
           if (uri != null) {
                try {
                    Logger.getLogger(PresentationCellFactory.class.getName()).warning("PDF URI is: " + uri);
                    deployedPDF = PDFDeployer.loadDeployedPDF(uri);

                    state.setSourceURI(uri);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(PresentationCellFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PresentationCellFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JAXBException ex) {
                    Logger.getLogger(PresentationCellFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
           }
       }
        
        PresentationLayout layout = new PresentationLayout(LayoutType.LINEAR);
        layout.setScale(1.0f);
        layout.setSpacing(4.0f);
        layout.setSlides(PDFLayoutHelper.generateLayoutMetadata(layout.getLayout(), deployedPDF, layout.getSpacing()));

        // TODO Do some fallback handling here - what happens if we don't have
        // a proper PDF at this stage? there will be no layout information.
        state.setLayout(layout);



        return (T)state;
    }


    public String getDisplayName() {
        // if null, won't show in the insert component dialog
        return "Presentation Cell";
    }

    public Image getPreviewImage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        // XXX HACK
        // In order for this NOT to display in the Cell Palettes then the
        // getDisplayName() method must return null. However, this prevents it
        // from appearing in a list of Cells when more than one supports the
        // the PDF extension. So we return a good display name here
        // XXX
        return "Presentation Cell ";
    }
}