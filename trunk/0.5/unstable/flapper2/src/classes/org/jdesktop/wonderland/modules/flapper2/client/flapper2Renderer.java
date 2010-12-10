/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper2.client;


import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.io.IOException;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
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
public class flapper2Renderer extends BasicRenderer
    {
    private Node root = null;
    private flapper2Cell motherCell = null;
    private Entity mye = null;

    public flapper2Renderer(Cell cell)
        {
        super(cell);
        motherCell = (flapper2Cell)cell;
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
/*                if(spatial.getControllerCount() > 0)
                    {
                    ArrayList al = spatial.getControllers();
                    System.out.println("Controller = " + al.get(0).toString());
                    Controller co = spatial.getController(0);
                    float max = co.getMaxTime();
                    float min = co.getMinTime();
                    AnimationController ac = (AnimationController)co;
                    ArrayList bans = ac.getAnimations();
                    System.out.println(" bans 0 - " + bans.get(0));
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
                    }
*/
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


    protected Node createSceneGraph(Entity entity)
        {
        try
            {
            mye = entity;
            root = new Node("flapper2 Root");
            LoaderManager manager = LoaderManager.getLoaderManager();
            URL url = AssetUtils.getAssetURL(motherCell.modelURI, cell);
            DeployedModel dm = manager.getLoaderFromDeployment(url);

            Node theModel = dm.getModelLoader().loadDeployedModel(dm, entity);
            theModel.setLocalTranslation(0, 0, 0.3f);
            root.attachChild(theModel);
            root.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
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
    }


