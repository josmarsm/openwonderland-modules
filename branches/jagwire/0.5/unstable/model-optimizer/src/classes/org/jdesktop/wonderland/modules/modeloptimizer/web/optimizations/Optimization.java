/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.modeloptimizer.web.optimizations;

import java.io.IOException;
import java.util.Map;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;

/**
 *
 * @author jkaplan
 */
public interface Optimization {
    /**
     * Perform any initialization needed by this optimization
     */
    public void initialize();
    
    /**
     * Optimize the given node in the content repository. The optimizer may
     * make whatever changes are needed to the nodes. Intermediate
     * work products (like loaded models) that are shared between 
     * optimizers can be stored in the context map -- the same map is
     * passed in to each optimizer.
     * 
     * @param node the content node to optimize
     * @param context a map for storing intermediate work products
     * @return true if the model was optimized, or false if not
     */
    public boolean optimize(ContentNode node, Map<String, Object> context)
            throws ContentRepositoryException, IOException;
}
