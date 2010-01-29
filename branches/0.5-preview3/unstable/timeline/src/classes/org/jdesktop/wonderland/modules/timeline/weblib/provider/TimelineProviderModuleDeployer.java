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
package org.jdesktop.wonderland.modules.timeline.weblib.provider;

import org.jdesktop.wonderland.web.asset.deployer.AssetDeployer;
import org.jdesktop.wonderland.web.checksums.deployer.ChecksumDeployer;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.runner.RunManager;
import org.jdesktop.wonderland.runner.Runner;

/**
 * Deploy timelineprovider jars
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderModuleDeployer extends ChecksumDeployer {
    /** the types of modules we deploy */
    private static final String[] TYPES = { "timelineprovider" };
   
    @Override
    public String getName() {
        return "Timeline Provider";
    }
    
    /**
     * The types of module we are interested in
     * @return common and server modules
     */
    @Override
    public String[] getTypes() {
        return TYPES; 
    }

    /**
     * Modules can only be deployed when the server is not running.
     * @param type The module part type
     * @param module The module associated with the module part
     * @param part The part of the module to be deployed
     * @return true if the module part can be deployed by the deployer
     */
    @Override
    public boolean isDeployable(String type, Module module, ModulePart part) {
        // get all darkstar servers, and make sure they are in the 
        // NOT_RUNNING state
        boolean running = false;
        
        for (Runner r : RunManager.getInstance().getAll(TimelineProviderRunner.class)) {
            if (r.getStatus() != Runner.Status.NOT_RUNNING) {
                running = true;
                break;
            }
        }
        
        return !running;
    }

    /**
     * Modules can only be undeployed when the server is not running
     * @param type The module part type
     * @param module The module associated with the module part
     * @param part The part of the module to be deployed
     * @return true if the module part can be undeployed by the deployer
     */
    @Override
    public boolean isUndeployable(String type, Module module, ModulePart part) {
         // get all darkstar servers, and make sure they are in the 
        // NOT_RUNNING state
        boolean running = false;
        
        for (Runner r : RunManager.getInstance().getAll(TimelineProviderRunner.class)) {
            if (r.getStatus() != Runner.Status.NOT_RUNNING) {
                running = true;
                break;
            }
        }
        
        return !running;
    }
}
