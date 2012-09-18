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
package org.jdesktop.wonderland.modules.telepointer.client.cell;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Line;
import com.jme.scene.Line.Mode;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.geom.BufferUtils;
import java.awt.event.MouseEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.modules.telepointer.common.cell.messages.TelePointerMessage;

/**
 * Manage the rendering of all SharedPointers.
 *
 * @author paulby
 */
class TelePointerRenderer {

    private Entity entity = null;
    private Node rootNode;

    private static TelePointerRenderer sharedPointerRenderer = null;

    TelePointerRenderer() {
    }

    public static final TelePointerRenderer getSharedPointerRenderer() {
        if (sharedPointerRenderer==null)
            sharedPointerRenderer = new TelePointerRenderer();

        return sharedPointerRenderer;
    }

    SharedPointer createSharedPointer(String username) {
        if (entity==null)
            initialize();

        return new SharedPointer(username);
    }

    private void initialize() {
        entity = new Entity("SharedPointerRenderer");
        rootNode = new Node("SharedPointerRenderer-root");
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        entity.addComponent(RenderComponent.class, rc);

        ZBufferState zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        rootNode.setRenderState(zbuf);

        ClientContextJME.getWorldManager().addEntity(entity);
    }

    class SharedPointer {
        private Node pointer;
        private boolean isVisible=false;

        SharedPointer(String username) {
            pointer = new Node("Pointer-"+username);
            Line cursor = new Line();
            float size = 0.2f;
            FloatBuffer vertBuf = BufferUtils.createFloatBuffer(
                    new Vector3f(size, size, size), new Vector3f(-size, -size, -size),
                    new Vector3f(-size, size, size), new Vector3f(size, -size, -size),
                    new Vector3f(size, -size, size), new Vector3f(-size, size, -size),
                    new Vector3f(size, size, -size), new Vector3f(-size, -size, size)
                    );
            cursor.setVertexBuffer(vertBuf);
            cursor.setVertexCount(8);
            cursor.setMode(Mode.Segments);
            IntBuffer indices = BufferUtils.createIntBuffer(0,1,2,3,4,5,6,7);
            cursor.setIndexBuffer(indices);
            cursor.setLineWidth(6f);
            cursor.setModelBound(new BoundingSphere(size, new Vector3f()));
            pointer.attachChild(cursor);

            TextLabel2D label = new TextLabel2D(username);
            label.setLocalTranslation(0f, 0.3f, 0f);
            Matrix3f rot = new Matrix3f();
            rot.fromAngleAxis((float) Math.PI, new Vector3f(0f,1f,0f));
            label.setLocalRotation(rot);
            pointer.attachChild(label);

            pointer.setLocalTranslation(new Vector3f());

        }

        private void setVisible(boolean visible) {
            if (visible==isVisible)
                return;

            if (visible) {
                SceneWorker.addWorker(new WorkCommit() {
                    public void commit() {
                        rootNode.attachChild(pointer);
                        ClientContextJME.getWorldManager().addToUpdateList(rootNode);
                    }
                });
            } else {
                SceneWorker.addWorker(new WorkCommit() {
                    public void commit() {
                        rootNode.detachChild(pointer);
                        ClientContextJME.getWorldManager().addToUpdateList(rootNode);
                    }
                });
            }
            isVisible = visible;
        }

        /**
         * Handle the message, scheduling any visual updates
         */
        public void handleMessage(final TelePointerMessage msg) {
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    switch(msg.getMouseEventID()) {
                        case MouseEvent.MOUSE_ENTERED :
                            setVisible(true);
//                            System.err.println("TelePointerRenderer - Mouse Enter");
                            break;
                        case MouseEvent.MOUSE_EXITED :
                            setVisible(false);
//                            System.err.println("TelePointerRenderer - Mouse Exit");
                            break;
                    }
                    if (isVisible && msg.getPointerPosition()!=null) {
                        pointer.setLocalTranslation(msg.getPointerPosition());
                        ClientContextJME.getWorldManager().addToUpdateList(pointer);
                    }
                }

            });
        }
    }


}
