/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.RoundEllie4.client;


import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;
import com.jme.animation.BoneTransform;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.io.IOException;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;

/**
 *
 * @author morris
 */
public class RoundEllie4Renderer extends BasicRenderer
    {
    private Node root = null;
    private RoundEllie4Cell motherCell = null;
    private Entity mye = null;
    private ArrayList vect = new ArrayList();

    public RoundEllie4Renderer(Cell cell)
        {
        super(cell);
        motherCell = (RoundEllie4Cell)cell;
        }

    private void printTree(Node root, int indent)
        {
        HashMap theHash = new HashMap<String, Object>();

        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < indent; i++)
            {
            buffer.append('\t');
            }

        System.out.println(buffer.toString() + "node: " + root);
        List<Spatial> children = root.getChildren();
        if(children == null)
            {
            return;
            }
        for(Spatial spatial : children)
            {
            if(spatial instanceof Node)
                {
                System.out.println("Node controller count = " + spatial.getControllerCount());
                if(spatial.getControllerCount() > 0)
                    {
                    System.out.println("Found a node with a controller " + spatial);
                    

                    ArrayList al = spatial.getControllers();

//                    System.out.println("Controller = " + al.get(0).toString() + " for node " + spatial);
                    Controller co = spatial.getController(0);
//                    float max = co.getMaxTime();
//                    float min = co.getMinTime();
//                    System.out.println("Time max = " + max + " - min = " + min + " for node " + spatial);
                    AnimationController ac = (AnimationController)co;
                    ArrayList bans = ac.getAnimations();
                    if(bans != null)
                        {
                        RoundEllie4ANode man = new RoundEllie4ANode();
                        man.nodeName = spatial.getName();
                        vect.add(man);
                        
/*                        System.out.println(" bans 0 - " + bans.get(0));
                        BoneAnimation ani = ac.getAnimation(bans.get(0).toString());
                        float kft[] = ani.getKeyFrameTimes();
                        for(int i = 0; i < kft.length; i++)
                            {
                            System.out.println("kft - " + kft[i]);
                            }
                        ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                        System.out.println("bt size = " + bt.size());
                        for(int j = 0; j < bt.size(); j++)
                            {
                            System.out.println("bt = " + bt.get(j).toString());
                        
                            }
                        Quaternion qt[] = bt.get(0).getRotations();
                        for(int i = 0; i < qt.length; i++)
                            {
                            System.out.println("rots = " + qt[i]);
                            }
                        System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                        Vector3f v3f[] = bt.get(0).getTranslations();
                        for(int i = 0; i < v3f.length; i++)
                            {
                            System.out.println("trans = " + v3f[i]);
                            }
 */
                        }
                    }

                printTree((Node)spatial, indent + 1);
                    
                }
            else
                {
                
                System.out.println("spatial: " + spatial.getName());
                System.out.println("Spatial Controller count = " + spatial.getControllerCount());

                if(spatial.getControllerCount() > 0)
                    {
                    spatial.getController(0).getControllerValues(theHash);
                    System.out.println("Hash - " + theHash);
                    }
//                System.out.println("spatial color = " + spatial.getGlowColor());
//                System.out.println("spatial rotation = " + spatial.getLocalRotation());
//                System.out.println("spatial translation = " + spatial.getLocalTranslation());
//                System.out.println("spatial scale = " + spatial.getLocalScale());
               }
            }
        }

    public void releaseNodeList()
        {
        vect.clear();
        }

    protected Node createSceneGraph(Entity entity)
        {
        try
            {
            mye = entity;
            root = new Node("RoundEllie4 Root");
            LoaderManager manager = LoaderManager.getLoaderManager();
            URL url = AssetUtils.getAssetURL(motherCell.modelURI, cell);
            DeployedModel dm = manager.getLoaderFromDeployment(url);

            Node theModel = dm.getModelLoader().loadDeployedModel(dm, entity);
            theModel.setLocalTranslation(0, 0, 0.3f);
            root.attachChild(theModel);
            root.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
            releaseNodeList();
            printTree(root, 2);
//            SceneMonitor.getMonitor().registerNode(root);
//            SceneMonitor.getMonitor().showViewer(true);
            }
        catch (MalformedURLException ex)
            {
            logger.log(Level.SEVERE, null, ex);
            }
        catch (IOException e)
            {
            logger.log(Level.SEVERE, null, e);
            }
        return root;
        }

    public Spatial getNode(String nodeName)
        {
        Spatial theNode = ScenegraphUtils.findNamedNode(root, nodeName);
        return theNode;
        }

    public Entity getTheEntity()
        {
        return mye;
        }

        public ArrayList getVect()
        {
        return vect;
        }

    }


