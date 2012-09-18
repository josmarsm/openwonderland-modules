/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.officeconverter.weblib;

import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;

/**
 * Runner for starting OpenOffice in headless mode.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author Bernard Horan
 */
public class OpenOfficeRunner extends BaseRunner {

    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "Open Office Server";
    /** the logger */
    private static final Logger LOGGER =
            Logger.getLogger(OpenOfficeRunner.class.getName());

    /** the webserver URL to link back to */
    private String webserverURL;

    /**
     * Configure this runner.  This method sets values to the default for the
     * Darkstar server.
     *
     * @param props the properties to deploy with
     * @throws RunnerConfigurationException if there is an error configuring
     * the runner
     */
    @Override
    public void configure(Properties props) 
            throws RunnerConfigurationException
    {
        super.configure(props);

        // if the name wasn't configured, do that now
        if (!props.containsKey("runner.name")) {
            setName(DEFAULT_NAME);
        }

        // record the webserver URL
        webserverURL = props.getProperty("wonderland.web.server.url");
    }

    /**
     * Add the Darkstar distribution file
     * @return a list containing the core distribution file as well
     * as the Darkstar distribution file
     */
    @Override
    public Collection<String> getDeployFiles() {
        // add all the files from the superclass
        Collection<String> files = super.getDeployFiles();

        //files.add("wonderland-client-dist.zip");
        files.add("openoffice-dist.zip");
        return files;
    }

    /** Default Darkstar properties */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("wonderland.web.server.url", webserverURL);
        //props.setProperty("java.security.debug", "failure");
        //props.setProperty("catalina.useNaming", "true");
        
        return props;
    }
}
