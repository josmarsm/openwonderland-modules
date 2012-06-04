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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.WonderlandURLStreamHandlerFactory;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.webdav.web.FileWebContentRepository;

/**
 *
 * @author jkaplan
 */
public class Main implements Runnable {
    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());
    
    private static final String HANDLER_PACKAGE =
            Main.class.getPackage().getName() + ".protocols";
    
    private static final String PREFIX = "Optimize: ";
    private static final String COMPLETE = "Optimization Complete: ";
    
    private static final Optimization[] OPTIMIZATIONS = new Optimization[] {
        new LoadModelOptimization(),
        new KMZLoaderDataOptimization(),
        new DAELoaderDataOptimization(),
    };
    
    private final BufferedReader in;
    private final FileWebContentRepository repo;
    
    public Main() {
        in = new BufferedReader(new InputStreamReader(System.in));
        repo = getRepo();
        
        for (Optimization opt : OPTIMIZATIONS) {
            opt.initialize();
        }
        
        
    }
    
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                boolean optimized = false;
                
                try {
                    optimized = processLine(line);
                } finally {
                    // notify the caller that we have completed
                    System.out.println(COMPLETE + optimized);
                }
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error optimizing", t);
        }
    }
    
    private boolean processLine(String line) 
            throws ContentRepositoryException, IOException 
    {
        LOGGER.log(Level.WARNING, "Received line: " + line);
        
        String path = line.substring(PREFIX.length()).trim();
        ContentNode node = repo.getRoot().getChild(path);

        LOGGER.log(Level.WARNING, "Optimized: " + node.getPath());
        
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        boolean optimized = false;
        
        for (Optimization opt : OPTIMIZATIONS) {
            optimized |= opt.optimize(node, context);
        }
        
        LOGGER.log(Level.WARNING, "Done optimizing: " + node.getPath());
        
        return optimized;
    }
    
    public static FileWebContentRepository getRepo() {
        try {
            File cwd = new File(System.getProperty("user.dir"));
            URL baseURL = new URL("http://localhost/");
            return new FileWebContentRepository(cwd, baseURL);
        } catch (MalformedURLException mue) {
            throw new RuntimeException(mue);
        }
    }
    
    public static void main(String[] args) {
        WonderlandURLStreamHandlerFactory.setHandlerPackage(HANDLER_PACKAGE);
        URL.setURLStreamHandlerFactory(new WonderlandURLStreamHandlerFactory());
        
        Main main = new Main();
        main.run();
    }
}
