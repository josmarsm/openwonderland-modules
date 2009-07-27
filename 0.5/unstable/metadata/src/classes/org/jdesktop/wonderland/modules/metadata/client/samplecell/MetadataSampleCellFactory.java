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
package org.jdesktop.wonderland.modules.metadata.client.samplecell;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSampleCellServerState;

/**
 * The cell factory for the sample cell. Adjusted to test Metadata.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author mabonner
 */
@CellFactory
public class MetadataSampleCellFactory implements CellFactorySPI {
  private static Logger logger = Logger.getLogger(MetadataSampleCellFactory.class.getName());
  public String[] getExtensions() {
      return new String[] {};
  }

  public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
      CellServerState state = new MetadataSampleCellServerState();
      // state.setShapeType("BOX");

      // HACK!
      Map<String, String> metadata = new HashMap();
      metadata.put("sizing-hint", "2.0");
      state.setMetaData(metadata);

      return (T)state;
  }

  public String getDisplayName() {
      return "Metadata Sample Cell";
  }

  public Image getPreviewImage() {
      URL url = MetadataSampleCellFactory.class.getResource("resources/metadata_sample_preview.png");
      if(url == null){
        logger.severe("PATH TO PREVIEW IMAGE FOR METADATA SAMPLE CELL BAD - used abs path");
        url = MetadataSampleCellFactory.class.getResource("/Users/Matt/trunk/metadata/src/classes/org/jdesktop/wonderland/modules/metadata/client/samplecell/resources/metadata_sample_preview.png");
      }
      if(url == null){
        logger.severe("Path is STILL null!");
        return null;
      }
      return Toolkit.getDefaultToolkit().createImage(url);
  }
}
