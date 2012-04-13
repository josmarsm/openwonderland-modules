
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellServerState;

/**
 * We don't annotate this factory because we don't want users inserting this
 * cell from the insert object menu. Instead, we want them to invoke a script
 * function.
 * @author JagWire
 */
@CellFactory
public class CommonCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] { };
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        return (T)new CommonCellServerState();
    }

    public String getDisplayName() {
       return "Empty Cell";
    }

    public Image getPreviewImage() {
        return null;
    }
}
