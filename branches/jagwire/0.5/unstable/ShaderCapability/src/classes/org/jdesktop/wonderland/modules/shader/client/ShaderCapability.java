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

package org.jdesktop.wonderland.modules.shader.client;

import com.jme.scene.Node;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState.StateType;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * Client-side sample cell component
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ShaderCapability extends CellComponent implements RenderUpdater {

    private static Logger logger = Logger.getLogger(ShaderCapability.class.getName());
    private String info = null;
    private GLSLShaderObjectsState shaderState;
    private boolean armed = false;
    
    
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli shaderMap;
    private SharedMapCli uniformsMap;
    private MapListener listener;
    
    private String vertexShader = "";
    private String fragmentShader = "";
    
    public ShaderCapability(Cell cell) {
        super(cell);
        listener = new MapListener();
        shaderState = (GLSLShaderObjectsState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.GLSLShaderObjects);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        switch(status) {
            case ACTIVE:
                if (increasing) {
                    new Thread(new Runnable() {

                        public void run() {
                            shaderMap = ssc.get("shaders");
                            uniformsMap = ssc.get("uniforms");

                            shaderMap.addSharedMapListener(listener);
                            uniformsMap.addSharedMapListener(listener);

                            handleShaders(shaderMap);
                            handleUniforms(uniformsMap);

                        }
                    }).start();
                }
                break;
                
            case DISK:
                break;
        }
    }
    
    public SharedMapCli getShaderMap() {
        return shaderMap;
    }
    
    public SharedMapCli getUniformsMap() {
        return uniformsMap;
    }
    
    private void handleShaders(SharedMapCli map) {
        if(map.isEmpty())
            return; //nothing to do
        
        //otherwise...
        
        SharedString v = (SharedString)map.get("vertext");
        SharedString f = (SharedString)map.get("fragment");
        
        vertexShader = v.getValue();
        fragmentShader = f.getValue();
        
        activateShaders();
    }
    
    private void handleUniforms(SharedMapCli map) {
        if(map.isEmpty())
            return; //nothing to do
    }
    
    
    private void activateShaders() {
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                synchronized (shaderState) {
                    //shaderState = (GLSLShaderObjectsState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.GLSLShaderObjects);

                    //set uniforms
                    initializeUniforms();

                    //acquire cell's renderer to get the RenderComponent.
                    CellRendererJME r = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    //acquire the RenderComponent to get the scene root.
                    RenderComponent rc = r.getEntity().getComponent(RenderComponent.class);
                    //acquire the scene root to set the shader state;
                    Node n = rc.getSceneRoot();

                    //set the shader state
                    n.setRenderState(shaderState);
                    armRenderUpdater();
                    logger.warning("activated shaders");
                }
            }
        });

    }
    
    private void armRenderUpdater() {
        
            ClientContextJME.getWorldManager().addRenderUpdater(this, shaderState);
            
            armed = true;
            logger.warning("arming render updater");
        
    }
    private void initializeUniforms() {
        //TODO: iterate through uniform variables
    }
    

    public void update(Object o) {
        synchronized(shaderState) {
            GLSLShaderObjectsState shader = (GLSLShaderObjectsState)o;
            shader.load(vertexShader, fragmentShader);
            logger.warning("update!");
        }
    }
    
    public void setVertexShader(String shader) {
        synchronized(shaderState) {
            this.vertexShader = shader;
        }
    }
    
    public void setFragmentShader(String shader) {
        synchronized(shaderState) {
            this.fragmentShader = shader;
        }
    }
    
    public void start() {
        
        activateShaders();
    }
    
    private class MapListener implements SharedMapListenerCli {

        public void propertyChanged(SharedMapEventCli event) {
            Map  m = event.getMap();
            String mapName = event.getMap().getName();
            synchronized (shaderState) {
                if (mapName.equals("shaders")) {
                    logger.warning("Shader map update!");
                    vertexShader = ((SharedString)m.get("vertex")).getValue();
                    fragmentShader = ((SharedString)m.get("fragment")).getValue();
                } else if (mapName.equals("uniforms")) {
                    //TODO: handle uniform change
                }
                
                
                    logger.warning("starting shaders!");
                    start();
                
            }
        }
        
    }
    
    
}
