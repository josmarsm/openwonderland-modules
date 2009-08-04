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

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
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

}