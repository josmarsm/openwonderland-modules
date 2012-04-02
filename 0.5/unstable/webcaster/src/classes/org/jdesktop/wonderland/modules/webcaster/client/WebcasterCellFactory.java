/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.webcaster.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellServerState;
import org.jdesktop.wonderland.modules.phone.common.PhoneInfo;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
@CellFactory
public class WebcasterCellFactory implements CellFactorySPI {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcaster/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    @SuppressWarnings("unchecked")
    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        WebcasterCellServerState state = new WebcasterCellServerState();
        state.setName(bundle.getString("WEBCASTER"));
        PhoneInfo phoneInfo = new PhoneInfo(false, String.valueOf(state.getStreamID()), "foo","Webcaster Phone", .2, .1, true, true);
        PhoneCellServerState phoneCellState = new PhoneCellServerState(phoneInfo);
        phoneCellState.setName("Webcaster phone: " + state.getStreamID());
        state.setPhoneCellState(phoneCellState);
        return (T)state;
    }

    public String getDisplayName() {
        return bundle.getString("WEBCASTER");
    }

    public Image getPreviewImage() {        
        URL url = WebcasterCellFactory.class.getResource("resources/webcaster_preview.gif");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
