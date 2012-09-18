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
package org.jdesktop.wonderland.modules.modeloptimizer.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.utils.RunUtil;
import org.jdesktop.wonderland.utils.SystemPropertyUtil;

/**
 *
 * @author jkaplan
 */
public class Optimizer implements Runnable {
    private static final Logger LOGGER =
            Logger.getLogger(Optimizer.class.getName());
    
    public enum Status { NOT_STARTED, PREPARING, WORKING, COMPLETE, ERROR };
    
    private static final String[] JARS = {
        "contentrepo-spi.jar",
        "jme-awt.jar",
        "jme-collada.jar",
        "jme-xml.jar",
        "jme.jar",
        "jmecolladaloader-client.jar",
        "kmzloader-client.jar",
        "mtgame.jar",
        "optimizations.jar",
        "webdav-spi.jar",
        "wonderland-client.jar",
        "wonderland-common.jar"
    };
    
    private static final String MAIN_CLASS =
            Optimizer.class.getPackage().getName() + ".optimizations.Main";
    
    private final ServletContext ctx;
    private final ExternalProcessOptimizer opt;
    
    private Status status = Status.NOT_STARTED;
    private float progress = 0f;

    private int totalFiles;
    private int processedFiles;
    
    public Optimizer(ServletContext ctx) {
        this.ctx = ctx;
        this.opt = new ExternalProcessOptimizer(RunUtil.getContentDir());
    }
    
    public synchronized Status getStatus() {
        return status;
    }

    private synchronized void setStatus(Status status) {
        this.status = status;
    }

    public synchronized float getProgress() {
        return progress;
    }

    private synchronized void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    private synchronized void processedFile() {
        processedFiles++;
        progress = (float) processedFiles / (float) totalFiles;
    }

    public void run() {
        try {
            optimize();
            setStatus(Status.COMPLETE);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error optimizing", t);
            setStatus(Status.ERROR);
        } finally {
            opt.stop();
        }
    }
    
    public void stop() {
        opt.stop();
    }
    
    private void optimize() throws ContentRepositoryException, IOException,
                                   InterruptedException
    {
        setStatus(Status.PREPARING);
        ContentCollection root = (ContentCollection) getRepo().getRoot().getChild("users");        
        setTotalFiles(countFiles(root));
        
        setStatus(Status.WORKING);
        optimize(root);
    }

    private int countFiles(ContentCollection root) 
            throws ContentRepositoryException 
    {   
        int count = 1;
        
        for (ContentNode cn : root.getChildren()) {
            if (cn instanceof ContentResource) {
                count++;
            } else if (cn instanceof ContentCollection) {
                count += countFiles((ContentCollection) cn);
            }
        }
        
        return count;
    }

    private int optimize(ContentNode node) 
            throws InterruptedException, IOException 
    {
        int retries = 1;
        boolean optimized = false;

        while (!optimized && retries >= 0) {
            optimized = opt.optimize(node.getPath(), 30000);
            retries--;
        }
        
        int changed = (optimized ? 1 : 0);
        processedFile();

        if (node instanceof ContentCollection) {
            try {
                for (ContentNode child : ((ContentCollection) node).getChildren()) {
                    changed += optimize(child);
                }
            } catch (ContentRepositoryException ce) {
                LOGGER.log(Level.WARNING, "Unable to get children for "
                        + node.getPath(), ce);
            }
        }

        return changed;
    }

    private WebContentRepository getRepo() {
        return WebContentRepositoryRegistry.getInstance().getRepository(ctx);
    }
    
    private static class ExternalProcessOptimizer {
        private static final String RESULT_LINE = "Optimization Complete: ";
        private static final String PREFIX = "modelloaderjars/";
        
        private final String rootDir;
        private final Semaphore lock;

        private Process process;
        private Thread outputReader;
        
        public ExternalProcessOptimizer(File rootDir) {
            this.rootDir = rootDir.getPath();
            this.lock = new Semaphore(1);
            lock.drainPermits();
        }
        
        public boolean optimize(String file, long waitTime)
                throws IOException, InterruptedException
        {
            LOGGER.warning("Optimize " + file);
            
            if (process == null) {
                LOGGER.warning("Process not running -- launch");
                launch();
            }
            
            long startTime = System.currentTimeMillis();
            
            // send message
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    process.getOutputStream()));
            pw.println("Optimize: " + file);
            pw.flush();
            
            // wait for the semaphore to be set
            boolean result = false;
            try {
                result = lock.tryAcquire(waitTime, TimeUnit.MILLISECONDS);
                return result;
            } finally {
                if (!result) {
                    stop();
                }
                
                lock.drainPermits();
                long time = System.currentTimeMillis() - startTime;
                LOGGER.warning("Optimization finished in " + time + " ms. " +
                               "Result: " + result);
            }
        }
        
        public synchronized void stop() {
            if (process != null) {
                process.destroy();
                process = null;
            }
            
            if (outputReader != null) {
                outputReader.interrupt();
                outputReader = null;
            }
            
            lock.release();
        }
        
        private void launch() throws IOException {
            // extract jars
            File jarDir = RunUtil.createTempDir("optimizer", "jardir");
            StringBuilder classPath = new StringBuilder();
            for (String jar : JARS) {
                File jarFile = RunUtil.extract(ExternalProcessOptimizer.class,
                                               PREFIX + jar, jarDir);
                classPath.append(jarFile.getPath()).append(File.pathSeparator);
            }
            
            // extract log config file
            File logConf = RunUtil.extract(ExternalProcessOptimizer.class, 
                                           "conf/logging.properties", 
                                           jarDir);
            // rewrite file to replace logDir
            Properties props = new Properties();
            props.load(new FileReader(logConf));
            String logPattern = props.getProperty("java.util.logging.FileHandler.pattern");
            if (logPattern != null) {
                logPattern = logPattern.replace("%logDir%", getLogDir());
            }
            props.setProperty("java.util.logging.FileHandler.pattern", logPattern);
            props.store(new FileWriter(logConf), "Updated log directory");
            
            List<String> command = new ArrayList<String>();
            command.add(System.getProperty("java.home") + File.separator + 
                        "bin" + File.separator + "java");
            command.add("-cp");
            command.add(classPath.toString());
            command.add("-Djava.util.logging.config.file=" + logConf.getPath());
            command.add(MAIN_CLASS);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(rootDir));
            pb.redirectErrorStream(true);
            process = pb.start();
            
            outputReader = new Thread(new Runnable() {
                public void run() {
                    try {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (LOGGER.isLoggable(Level.INFO)) {
                                LOGGER.info("[Optimizer] " + line);
                            }
                            
                            if (line.startsWith(RESULT_LINE)) {
                                //value = Boolean.parseBoolean(line.substring(RESULT_LINE.length()));
                                
                                // a single optimization finished -- release
                                // the lock so a new one can be submitted
                                LOGGER.info("Optimization complete");
                                lock.release();
                            }
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error reading input stream",
                                   ex);
                    }
                    
                    // at this point, the stream is closed and the process 
                    // should stop if it hasn't already
                    if (outputReader == Thread.currentThread()) {
                        stop();
                    }
                }
            });
            outputReader.start();
        }
        
        protected String getLogDir() {
            String dir = SystemPropertyUtil.getProperty("wonderland.log.dir");
            if (dir == null) {
                dir = new File(RunUtil.getRunDir(), "log").getPath();
            }

            return dir;
        }
    }
}
