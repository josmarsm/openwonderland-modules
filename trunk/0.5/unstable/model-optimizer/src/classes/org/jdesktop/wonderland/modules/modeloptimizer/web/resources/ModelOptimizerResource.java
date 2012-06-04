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
package org.jdesktop.wonderland.modules.modeloptimizer.web.resources;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.modeloptimizer.web.Optimizer;

/**
 *
 * @author jkaplan
 */
@Path("/")
public class ModelOptimizerResource implements ServletContextListener {
    enum CurrentOptimizer { 
        INSTANCE;
        
        private Optimizer current;
        private Thread currentThread;
        
        public synchronized Optimizer get() {
            return current;
        }
        
        public synchronized Optimizer start(ServletContext context) {
            if (current != null && 
                current.getStatus() != Optimizer.Status.COMPLETE &&
                current.getStatus() != Optimizer.Status.ERROR) 
            {
                // already optimization in progress
                return current;
            }
            
            current = new Optimizer(context);
            currentThread = new Thread(current);
            currentThread.start();
            return current;
        }
        
        public synchronized void stop() {
            if (currentThread != null) {
                currentThread.interrupt();
                currentThread = null;
            }
            
            if (current != null) {
                current.stop();
                current = null;
            }
        }
    }
    
    private static final CacheControl NO_CACHE = new CacheControl();
    static {
        NO_CACHE.setNoCache(true);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        Optimizer cur = CurrentOptimizer.INSTANCE.get();
        if (cur == null) {
            return Response.ok(new StatusWrapper()).cacheControl(NO_CACHE).build();
        }
        
        return Response.ok(new StatusWrapper(cur)).cacheControl(NO_CACHE).build();
    }
    
    @Path("/start")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@Context ServletContext context) {
        Optimizer cur = CurrentOptimizer.INSTANCE.start(context);
        return Response.ok(new StatusWrapper(cur)).cacheControl(NO_CACHE).build();
    }
    
    public void contextInitialized(ServletContextEvent sce) {
    }

    public void contextDestroyed(ServletContextEvent sce) {
        CurrentOptimizer.INSTANCE.stop();
    }
    
    @XmlRootElement(name="OptimizerStatus")
    private static class StatusWrapper {
        private final Optimizer.Status status;
        private final float progress;
        
        public StatusWrapper() {
            this.status = Optimizer.Status.NOT_STARTED;
            this.progress = 0f;
        }
        
        public StatusWrapper(Optimizer opt) {
            this.status = opt.getStatus();
            this.progress = opt.getProgress();
        }
        
        @XmlElement
        public Optimizer.Status getStatus() {
            return status;
        }
        
        @XmlElement
        public float getProgress() {
            return progress;
        }
    }
}
