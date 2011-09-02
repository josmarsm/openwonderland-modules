/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;

/**
 *
 * @author xiuzhen
 */
@CellFactory
public class SectionStyledStickyNoteCellFactory implements CellFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/Bundle");

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        SectionStyledStickyNoteCellServerState state = new SectionStyledStickyNoteCellServerState();
        state.setNoteType(StickyNoteTypes.SECTION_STYLED);
        // HACK!
        Map<String, String> metadata = new HashMap();
        metadata.put("sizing-hint", "2.0");
        state.setMetaData(metadata);
        state.setName("SectionStyledStickyNote");

        return (T) state;
    }

    public String getDisplayName() {
        return BUNDLE.getString("Section_styled_sticky_note");
    }

    public Image getPreviewImage() {
        // TODO: i18n/L10n
        URL url = GenericStickyNoteCellFactory.class.getResource("resources/stickynote_preview.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
