/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import org.jdesktop.wonderland.modules.pdfpresentation.common.PDFLayoutHelper;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * Cell factory to create a presentation Cell. This factory returns null for the
 * getDisplayName() method so that it does not appear in the Insert -> Object
 * dialog box: the only way to create a presentation Cell is to DnD a PDF file.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class PresentationCellFactory implements CellFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/pdfpresentation/client/" +
            "resources/Bundle");

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(PresentationCellFactory.class.getName());
    
    /**
     * {@inheritDoc}
     */
    public String[] getExtensions() {
        return new String[]{"pdf"};
    }

    /**
     * {@inheritDoc}
     */
    public <T extends CellServerState> T getDefaultCellServerState(
            Properties props) {

        LOGGER.warning("In PDFSpreaderCellFactory!");

        // Create a new server state for the presentation Cell, to be used to
        // create the Cell.
        PresentationCellServerState state = new PresentationCellServerState();
        state.setCreatorName(LoginManager.getPrimary().getUsername());

        // XXX HACK XXX
        // Provide a hint so that the slides appear above the floor
        // XXX HACK XXX
        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1, 2, 1);
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);

        // Using the URI specified in the "content-uri" attribute, fetch the
        // PDF and deploy it.
        DeployedPDF deployedPDF = null;
        if (props != null) {
            String uri = props.getProperty("content-uri");
            if (uri != null) {
                try {
                    LOGGER.warning("PDF URI is: " + uri);
                    deployedPDF = PDFDeployer.loadDeployedPDF(uri);
                    state.setSourceURI(uri);
                } catch (java.lang.Exception excp) {
                    LOGGER.log(Level.WARNING, "Unable to load PDF from " +
                            uri, excp);
                    return null;
                }
            }
        }

        // Create a new default layout based upon the parsed PDF File.
        PresentationLayout layout = new PresentationLayout(LayoutType.LINEAR);
        layout.setScale(PresentationLayout.DEFAULT_SCALE);
        layout.setSpacing(PresentationLayout.DEFAULT_SPACING);
        layout.setSlides(PDFLayoutHelper.generateLayoutMetadata(
                layout.getLayout(), deployedPDF.getNumberOfSlides(), layout.getSpacing()));

        // TODO Do some fallback handling here - what happens if we don't have
        // a proper PDF at this stage? there will be no layout information.
        state.setLayout(layout);
        return (T) state;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        // if null, won't show in the insert component dialog
        return null;
    }

    /**
     * {@inheritDoc}
     */
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
        return BUNDLE.getString("Presentation_Cell");
    }
}
