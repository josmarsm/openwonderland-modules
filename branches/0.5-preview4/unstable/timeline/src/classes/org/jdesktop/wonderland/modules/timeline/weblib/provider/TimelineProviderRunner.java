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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.runner.BaseRunner;
import org.jdesktop.wonderland.runner.RunnerChecksum;
import org.jdesktop.wonderland.runner.RunnerChecksums;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 * An extension of <code>BaseRunner</code> to launch the timeline provider.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderRunner extends BaseRunner {
    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "Timeline Provider";

    /** the URL for listing available files */
    private static final String CHECKSUM_URL =
                                  "timeline/timelineprovider/services/checksums";

    /** the logger */
    private static final Logger logger =
            Logger.getLogger(TimelineProviderRunner.class.getName());

    /** the webserver URL to link back to */
    private String webserverURL;

    /**
     * The current list of modules, implemented as a thread-local variable
     * that is only valid during a single call to start()
     */
    private static ThreadLocal<RunnerChecksums> moduleChecksums =
        new ThreadLocal<RunnerChecksums>();

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

        // and the Darkstar server jar
        files.add("wonderland-client-dist.zip");
        files.add("timeline-provider-dist.zip");

        // now add each module
        try {
            for (String module : getModuleChecksums().getChecksums().keySet()) {
                files.add(module);
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error reading module checksums", ioe);
        }

        return files;
    }

    /** Default Darkstar properties */
    @Override
    public Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("wonderland.web.server.url", webserverURL);
        return props;
    }

    @Override
    public synchronized void start(Properties props) throws RunnerException {
        try {
            super.start(props);
        } finally {
            // reset the module checksums
            moduleChecksums.remove();
        }
    }
   
    /**
     * Deploy zip files as normal using the superclass.  Copy any
     * .jar files (which are assumed to be modules) into the modules
     * directory
     * @param deploy the file to deploy
     * @throws IOException
     */
    @Override
    protected void deployFile(File deploy) throws IOException {
        if (deploy.getName().endsWith(".jar")) {
            File out = new File(getModuleDir(), deploy.getName());
            RunUtil.writeToFile(new FileInputStream(deploy), out);
        } else {
            super.deployFile(deploy);
        }
    }

    @Override
    protected RunnerChecksums getServerChecksums() throws IOException {
        // get the server checksums
        RunnerChecksums serverChecksums = super.getServerChecksums();

        // now add in the checksums for the modules
        Map<String, RunnerChecksum> checksums = serverChecksums.getChecksums();
        checksums.putAll(getModuleChecksums().getChecksums());
        serverChecksums.setChecksums(checksums);
        return serverChecksums;
    }

    /**
     * Get the module checksums from the thread-local variable.  This is only
     * valid during the method calls within a single invocation of start()
     */
    protected synchronized RunnerChecksums getModuleChecksums()
        throws IOException
    {
        RunnerChecksums out = moduleChecksums.get();
        if (out == null) {
            // read in the new checksums from the server
            URL checksumURL = new URL(webserverURL + CHECKSUM_URL);
            try {
                Reader in = new InputStreamReader(checksumURL.openStream());
                out = RunnerChecksums.decode(in);

                moduleChecksums.set(out);
            } catch (JAXBException je) {
                IOException ioe = new IOException("Error reading checksums " +
                                                  "from " + checksumURL);
                ioe.initCause(je);
                throw ioe;
            }
        }

        return out;
    }

    /**
     * Get the Darkstar server module directory
     * @return the server module directory
     */
    protected File getModuleDir() {
        File moduleDir = new File(getRunDir(), "modules");
        moduleDir.mkdirs();
        return moduleDir;
    }
}
