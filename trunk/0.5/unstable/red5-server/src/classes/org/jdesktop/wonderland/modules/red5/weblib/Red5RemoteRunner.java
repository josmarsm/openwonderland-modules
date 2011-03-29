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

package org.jdesktop.wonderland.modules.red5.weblib;

import java.util.Properties;
import org.jdesktop.wonderland.runner.BaseRemoteRunner;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;

/**
 * Remote runner for running RED5 on different servers.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class Red5RemoteRunner  extends BaseRemoteRunner {
    private String webserverURL;

    /**
     * Configure this runner.
     * @param props the properties to deploy with
     * @throws RunnerConfigurationException if there is an error configuring
     * the runner
     */
    @Override
    public void configure(Properties props)
            throws RunnerConfigurationException
    {
        super.configure(props);

        // record the webserver URL
        webserverURL = props.getProperty("wonderland.web.server.url");
    }

    /**
     * Get the default properties for the sas provider.
     * @return the default properties
     */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("wonderland.web.server.url", webserverURL);
        props.setProperty("java.security.debug", "failure");
        props.setProperty("logback.ContextSelector", "org.red5.logging.LoggingContextSelector");
        props.setProperty("catalina.useNaming", "true");

        return props;
    }

    public Class getRunnerClass() {
        return Red5Runner.class;
    }
}
