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

package org.jdesktop.wonderland.modules.pdfspreader.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage.LayoutType;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellServerState;

@CellFactory
public class PDFSpreaderCellFactory implements CellFactorySPI{
    public String[] getExtensions() {
        return new String[] {"pdf"};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        PDFSpreaderCellServerState state = new PDFSpreaderCellServerState();

        // set reasonable defaults here. 
        state.setLayout(LayoutType.LINEAR);
        state.setScale(1.0f);
        state.setSpacing(4.0f);
        state.setCreatorName(LoginManager.getPrimary().getUsername());

        // XXX HACK XXX
        // Provide a hint so that the slides appear above the floor
        // XXX HACK XXX
        // Give the hint for the bounding volume for initial Cell placement
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1, 2, 1);
        BoundingVolumeHint hint = new BoundingVolumeHint(true, box);
        state.setBoundingVolumeHint(hint);

        if (props != null) {
           String uri = props.getProperty("content-uri");
           if (uri != null) {
               state.setSourceURI(uri);
           }
       }

        return (T)state;
    }

    public String getDisplayName() {
        // if null, won't show in the insert component dialog
        return null;
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
        return "PDF Slide Spreader";
    }
}