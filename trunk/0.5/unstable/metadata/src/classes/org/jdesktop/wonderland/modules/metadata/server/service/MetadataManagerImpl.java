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
package org.jdesktop.wonderland.modules.metadata.server.service;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;



/**
 * Implementation of the metadata manager interface
 * @author mabonner
 */
public class MetadataManagerImpl implements MetadataManager {
    private MetadataService service;

    public MetadataManagerImpl(MetadataService service) throws Exception {
        this.service = service;
        // service.test();
    }

    public void test() {
        service.test();
    }

    public void setCellMetadata(CellID id, ArrayList<Metadata> metadata){
      service.setCellMetadata(id, metadata);
    }
}
