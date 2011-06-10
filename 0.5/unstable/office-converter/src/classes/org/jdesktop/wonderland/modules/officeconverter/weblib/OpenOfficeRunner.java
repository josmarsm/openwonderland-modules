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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.jdesktop.wonderland.runner.Runner;
import org.jdesktop.wonderland.runner.RunnerConfigurationException;
import org.jdesktop.wonderland.runner.RunnerException;
import org.jdesktop.wonderland.runner.RunnerInfo;
import org.jdesktop.wonderland.utils.Constants;
import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;
import org.jvnet.winp.WinProcess;

/**
 * Runner to start and stop openoffice running in headless mode
 * Heavily plagiarised from BaseRunner
 * @author Bernard Horan
 * @author Jon Kaplan
 */
public class OpenOfficeRunner implements Runner {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(OpenOfficeRunner.class.getName());
    /** the default name if none is specified */
    private static final String DEFAULT_NAME = "OpenOffice Runner";

    /** the name of this runner */
    private String name = "unknown";    

    /** the location of this runner */
    private String location = "localhost";

    /** the path to the open office executable */
    private String openoffice_executable;

    /** the directory to run in */
    private File runDir;

    /** the process we started */
    private Process proc;

    /** the log file */
    private File logDir;
    private File logFile;
    private Logger logWriter;
    private Handler logFileHandler;

    /** the base URL to connect to */
    private String localURL;

    /** the current status */
    private Status status = Status.NOT_RUNNING;
    private final Object statusLock = new Object();

    /** status listeners */
    private Set<RunnerStatusListener> listeners =
            new CopyOnWriteArraySet<RunnerStatusListener>();

    /** status updater thread */
    private Thread statusUpdater;

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    /**
     * Configure the runner.  Curently parses the following properties:
     * <ul><li><code>runner.name</code> - the name to return in
     *         <code>getName()</code>
     *     <li><code>runner.location</code> - the location to return in
     *         <code>getLocation()</code>
     * </ul>
     * @param props the properties to configure with
     * @throws RunnerConfigurationException if there is an error
     */
    public void configure(Properties props) throws RunnerConfigurationException {
        //logger.warning("Properties: " + props);
        if (props.containsKey("runner.name")) {
            setName(props.getProperty("runner.name"));
        } else {
            setName(DEFAULT_NAME);
        }

        if (props.containsKey("runner.location")) {
            setLocation(props.getProperty("runner.location"));
        }
        if (props.containsKey("openoffice.executable")) {
            openoffice_executable = props.getProperty("openoffice.executable");
        } else {
            openoffice_executable = getDefaultExecutable();
        }
        //logger.warning("openoffice: " + openoffice_executable);
        localURL = "http://" + System.getProperty(Constants.WEBSERVER_HOST_INTERNAL_PROP).trim() +
                   ":" + System.getProperty(Constants.WEBSERVER_PORT_PROP).trim() + "/";


    }

    /**
     * Get the default properties for this runner
     * @return an empty properties list
     */
    public Properties getDefaultProperties() {
        //logger.warning("OS NAME: " + System.getProperty("os.name"));
        Properties props = new Properties();
        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            props.setProperty("openoffice.executable","/Applications/OpenOffice.org.app/Contents/MacOS/soffice");
        } else {
            props.setProperty("openoffice.executable", "soffice");
        }
        return props;
    }

    /**
     * Determine whether or not this runner can be started and stopped by
     * the system.  Some runners, such as the web server, cannot be
     * started and stopped.
     * @return true if the runner can be started and stopped or false if not
     */
    public boolean isRunnable() {
        return true;
    }

    public void start(Properties props) throws RunnerException {
        // make sure we are in the correct state
        if (getStatus() != Status.NOT_RUNNING) {
            throw new IllegalStateException("Can't start runner in " +
                                            getStatus() + " state");
        }

        // read properties to update name and location
        if (props.containsKey("runner.name")) {
            setName(props.getProperty("runner.name"));
        }
        if (props.containsKey("runner.location")) {
            setLocation(props.getProperty("runner.location"));
        }
        if (props.containsKey("openoffice.executable")) {
            openoffice_executable = props.getProperty("openoffice.executable");
            //logger.warning("openoffice_executable:" + openoffice_executable);
        }

        // setup the logger.  First make sure that a new log will be
        // created.
        resetLogFile();
        try {
            logWriter = Logger.getLogger("wonderland.runner." + getLogName());
            logWriter.setLevel(Level.INFO);
            logWriter.setUseParentHandlers(false);
            logWriter.addHandler(getLogFileHandler());
        } catch (IOException ioe) {
            // no log file, abort
            logger.log(Level.WARNING, "Error creating log file " +
                       getLogFile() + " in runner " + getName());
            throw new RunnerException("Error creating log file", ioe);
        }

        try {
            // create the command to execute as a list of strings.  We will
            // convert this to a string array later on in order to execute it.
            // Command will be of approximately the form:
            //soffice -env:UserInstallation=file:///tmp/OfficeConverter -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
            //
            List<String> cmd = new ArrayList<String>();
            cmd.add(openoffice_executable);
            cmd.add("-env:UserInstallation=file://" + getRunDir().getCanonicalPath());
            cmd.add("-headless");
            cmd.add("-accept=\"socket,host=127.0.0.1,port=8100;urp;\"");
            cmd.add("-nofirststartwizard");

            // log the command
            logWriter.info("Executing: " + cmd.toString());

            // update status
            setStatus(Status.STARTING_UP);

            // execute
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(getRunDir());

            proc = pb.start();
            ProcessOutputReader reader = createOutputReader(
                                             proc.getInputStream(), logWriter);
            Thread outReader = new Thread(reader);
            outReader.setName(getName() + " output reader");
            outReader.start();

            // start a thread to wait for the process to die
            ProcessWaiter waiter = new ProcessWaiter(proc, outReader, logWriter);
            Thread waiterThread = new Thread(waiter);
            waiterThread.setName(getName() + " process waiter");
            waiterThread.start();
        } catch (IOException ioe) {
            logWriter.log(Level.WARNING, "Error starting process " + getName(),
                          ioe);
            setStatus(Status.ERROR);
            throw new RunnerException(ioe);
        }

        // everything started up OK, so set the status to RUNNING.  Note
        // that we hold the lock, so even if the process finishes before
        // this is called, the ProcessWaiter won't be able to the the status
        // until we are done, guaranteeing the correct ordering of
        // RUNNING and STOPPED.
        setStatus(Status.RUNNING);
        
    }

    public void stop() {
        // make sure we are in the correct state
        if (getStatus() != Status.RUNNING &&
                getStatus() != Status.STARTING_UP)
        {
            throw new IllegalStateException("Can't stop runner in " + 
                                            getStatus() + " state");
        }
        
        setStatus(Status.SHUTTING_DOWN);

        // workaround for Windows issue when stopping a process. Use
        // an external library that does a better job stopping
        // processes than Process.destroy()
        if (System.getProperty("os.name").startsWith("Windows")) {
            WinProcess wp = new WinProcess(proc);
            wp.killRecursively();
        } else {
            proc.destroy();
        }
    }

    /**
     * Get the current status of this runner.
     * @return the status of this runner.
     */
    public Status getStatus() {
        synchronized (statusLock) {
            return status;
        }
    }

    /**
     * Get information about this runner in a form that can be sent to
     * web services.
     * @return the information about this runner
     */
    public RunnerInfo getRunnerInfo() {
        return new RunnerInfo(localURL, this);
    }

    /**
     * Get the process log file.
     * @return the log file, created if it doesn't exist
     */
    public File getLogFile() {
        if (logFile == null) {
            if (Boolean.parseBoolean(
                    SystemPropertyUtil.getProperty("wonderland.log.preserve")))
            {
                // create a unqiuely named new log file
                do {
                    String useName = getLogName() +
                                     ((int) (Math.random() * 65536)) +
                                     ".log";
                    logFile = new File(getLogDir(), useName);
                } while (logFile.exists());
            } else {
                // reuse the name of the runner as the log file
                logFile = new File(getLogDir(), getLogName() + ".log");
            }
        }

        return logFile;
    }

    public Status addStatusListener(RunnerStatusListener rl) {
        listeners.add(rl);
        return getStatus();
    }

    public void removeStatusListener(RunnerStatusListener rl) {
        listeners.remove(rl);
    }

    /**
     * Set the name of this runner
     * @param name the name of this runner
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Set the location of this runner
     * @param location the location of this runner
     */
    protected void setLocation(String location) {
        this.location = location;
    }

    /**
     * Set the status and notify listeners.  Notifying listeners is done
     * in a separate thread, so we don't have to worry about listeners
     * creating deadlocks.
     * @param status the new status
     */
    protected void setStatus(final Status status) {
        synchronized (statusLock) {
            this.status = status;

            // make sure any notification that is in progress completes.  Otherwise
            // we could get out-of-order notifcations
            while (statusUpdater != null && statusUpdater.isAlive()) {
                try {
                   statusUpdater.join();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }

            // start a new thread to do the notifications
            statusUpdater = new Thread(new Runnable() {
                public void run() {
                    for (RunnerStatusListener l : listeners) {
                        try {
                            l.statusChanged(OpenOfficeRunner.this, status);
                        } catch (Error e) {
                            // log the exception, since it seems to get
                            // swallowed otherwise
                            logger.log(Level.WARNING, "Error notifying " + l, e);
                            throw e;
                        }
                    }
                }
            });

            statusUpdater.setName(getName() + " status update notifier");
            statusUpdater.start();
        }
    }

    /**
     * Reset the log file
     */
    protected synchronized void resetLogFile() {
        logFile = null;

        if (logFileHandler != null) {
            logFileHandler.close();
            logFileHandler = null;
        }

        if (logWriter != null) {
            logWriter = null;
        }
    }

    /**
     * Get the name of the runner, formatted for using as the name of a log
     * file.  Typically, this will do things like replace " " with "_" and
     * convert to lower case.
     * @return the log-formatted name of this runner
     */
    protected String getLogName() {
        return getName().replaceAll(" ", "_").toLowerCase();
    }

    /**
     * Get a log handler that should be used to write the log out to
     * the log file.
     * @return the handler to use
     */
    protected Handler getLogFileHandler() throws IOException {
        logFileHandler = new FileHandler(getLogFile().getCanonicalPath());
        logFileHandler.setLevel(Level.ALL);
        logFileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        });

        return logFileHandler;
    }

    /**
     * Get the log directory
     * @return the log directory
     */
    protected synchronized File getLogDir() {
        if (logDir == null) {
            // check a property
            String logDirProp = SystemPropertyUtil.getProperty("wonderland.log.dir");
            if (logDirProp != null) {
                logDir = new File(logDirProp);
            } else {
                logDir = new File(RunUtil.getRunDir(), "log");
            }

            logDir.mkdirs();
        }

        return logDir;
    }

    /**
     * Runners are identified by name.  Equals method is based on the name.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenOfficeRunner other = (OpenOfficeRunner) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    /**
     * Runners are identified by name.  Hashcode is based on the name.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Get the directory to install files in.
     * @return the run directory
     */
    protected synchronized File getRunDir() {
        if (runDir == null) {
            File baseDir = new File(RunUtil.getRunDir(), getLogName());
            runDir = new File(baseDir, "run");
            runDir.mkdirs();
        }

        return runDir;
    }
    
    /**
     * Create a new process output listener to handle the output from
     * a process.  This method is provided so subclasses can create their own
     * readers
     * @param in the data from the process
     * @param out the logger to log results to
     * @return a ProcessOutputReader that will be used to read output from
     * this process.
     */
    protected ProcessOutputReader createOutputReader(InputStream in,
                                                     Logger out) 
    {
        return new ProcessOutputReader(in, out);
    }
    
    /**
     * Create a new process waiter that waits for a process to end.  This
     * method is provided so subclasses can create their own waiters.
     * @param proc the process to wait for
     * @param outReader the output reader thread
     * @param logWriter the logger to write information to
     * @return the new ProcessWaiter
     */
    protected ProcessWaiter createWaiter(Process proc, Thread outReader,
                                         Logger logWriter) 
    {
        return new ProcessWaiter(proc, outReader, logWriter);
    }

    private static String getDefaultExecutable() {
        //logger.warning("OS NAME: " + System.getProperty("os.name"));
        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            return "/Applications/OpenOffice.org.app/Contents/MacOS/soffice";
        } else {
            return "soffice";
        }
    }

    /** read a stream from a process and write it to the given log file */
    protected static class ProcessOutputReader implements Runnable {
        private BufferedReader in;
        private Logger out;

        public ProcessOutputReader(InputStream is, Logger out) {
            this.in = new BufferedReader(new InputStreamReader(is));
            this.out = out;
        }

        public void run() {
            String line;

            try {
                while ((line = in.readLine()) != null) {
                    handleLine(line);
                }
            } catch (IOException ioe) {
                // oh well
                out.log(Level.WARNING, "Exception in process output reader",
                        ioe);

                logger.log(Level.WARNING, "Exception in process output reader",
                           ioe);
            }
        }

        protected void handleLine(String line) {
            out.info(line);
        }
    }

    /** wait for a process to end */
    protected class ProcessWaiter implements Runnable {
        private Process proc;
        private Thread outReader;
        private Logger logWriter;

        public ProcessWaiter(Process proc, Thread outReader,
                             Logger logWriter)
        {
            this.proc = proc;
            this.outReader = outReader;
            this.logWriter = logWriter;
        }

        public void run() {
            int exitValue = -1;

            // first wait for the process to end
            boolean running = true;
            while (running) {
                try {
                    exitValue = proc.waitFor();
                    running = false;
                } catch (InterruptedException ie) {
                    // ignore -- just start waiting again
                }
            }

            // now wait for the output
            try {
                outReader.join();
            } catch (InterruptedException ie) {
                // ignore
            }

            // wrte the exit value to the log and then close the log
            logWriter.info("Process exitted, return value: " + exitValue);

            // everything has terminated -- update the status
            setStatus(Status.NOT_RUNNING);
        }
    }

}
