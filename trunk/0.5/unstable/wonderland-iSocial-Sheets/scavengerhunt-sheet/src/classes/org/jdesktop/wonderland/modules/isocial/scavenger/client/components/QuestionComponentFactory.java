/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client.components;

import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionComponentServerState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;

/**
 *
 * @author Vladimir Djurovic
 */
@CellComponentFactory
public class QuestionComponentFactory implements CellComponentFactorySPI {
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ScavengerHuntConstants.BUNDLE_PATH);

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        QuestionComponentServerState state = new QuestionComponentServerState();
        return (T)state;
    }

    public String getDisplayName() {
        return BUNDLE.getString(ScavengerHuntConstants.PROP_QUESTION_NAME);
    }

    public String getDescription() {
        return BUNDLE.getString(ScavengerHuntConstants.PROP_QUESTION_DESC);
    }
    
}
