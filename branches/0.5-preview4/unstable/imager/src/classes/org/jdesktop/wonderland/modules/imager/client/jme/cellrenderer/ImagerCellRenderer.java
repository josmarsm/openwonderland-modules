/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.imager.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.imager.client.ImagerCell;

/**
 * @author jkaplan
 */
public class ImagerCellRenderer extends BasicRenderer
    {
    private final int totalImages = 100;
    private int loadedImages = 0;

    private Node node = null;
    private String[] textureURI = new String[totalImages];
    private URL[] url = new URL[totalImages];
    private Texture[] texture = new Texture[totalImages];
    private Image[] image = new Image[totalImages];
    private Box[] box = new Box[totalImages];
    private TextureState[] ts = new TextureState[totalImages];
    private String[] theImage = new String[totalImages];
    private int currentImage = 0;
    private float[] width = new float[totalImages];
    private float[] height = new float[totalImages];

    public ImagerCellRenderer(Cell cell)
        {
        super(cell);
        }

    public  void    exposeNext()
        {
        if(currentImage >= loadedImages - 1)
            {
            currentImage = 0;
            }
        else
            {
            currentImage++;
            }
        exposeImage(currentImage);
        }
    
    public  void    exposePrevious()
        {
        if(currentImage == 0)
            {
            currentImage = loadedImages - 1;
            }
        else
            {
            currentImage--;
            }
        exposeImage(currentImage);
        }

    public  void    exposeImage(int im)
        {
        System.out.println("Enter exposeImage with image = " + im);
        node.detachAllChildren();

        node.attachChild(box[im]);
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();
        box[im].setRenderState(ts[im]);
        ClientContextJME.getWorldManager().addToUpdateList(node);
        }

    protected Node createSceneGraph(Entity entity)
        {
        System.out.println("Enter createSceneGraph");
        // Create a new root node
        node = new Node("Image Viewer Node");

        int thisImage = 0;

        String  baseURI = cell.getCellCache().getSession().getSessionManager().getServerURL() + ((ImagerCell)cell).getImageURI() + "/" + cell.getName();
        try
            {
            URL urell = new URL(baseURI + "/content.txt");
            java.net.URLConnection con = urell.openConnection();
            con.connect();

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
            String line;
            for(; (line = in.readLine()) != null; )
                {
                System.out.println("File = " + line);
                theImage[thisImage] = line;
                thisImage++;
                if(thisImage >= totalImages)
                    {
                    break;
                    }
                }
            }
        catch (Exception ex)
            {
            Logger.getLogger(ImagerCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }

        loadedImages = thisImage - 1;

        // First load the texture to figure out its size
        for(int i = 0; i < loadedImages; i++)
            {
            System.out.println("i = " + i);
            textureURI[i] = baseURI + "/" + theImage[i];
            System.out.println("Loaded image " + textureURI[i]);

        // Convert the uri given to a proper url to download
            url[i] = null;
            try
                {
                url[i] = getAssetURL(textureURI[i]);
                }
            catch (MalformedURLException ex)
                {
                Logger.getLogger(ImagerCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }

        // Load the texture first to get the image size
            texture[i] = TextureManager.loadTexture(url[i]);
            texture[i].setWrap(Texture.WrapMode.BorderClamp);
            texture[i].setTranslation(new Vector3f());

        // Figure out what the size of the texture is, scale it down to something
        // reasonable.
            image[i] = texture[i].getImage();
            width[i] = image[i].getWidth() * 0.002f;
            height[i] = image[i].getHeight() * 0.002f;

        // Create a box of suitable dimensions
            box[i] = new Box("Box", new Vector3f(0, 5, 0), width[i], height[i], 0.1f);

        // Set the texture on the node
            ts[i] = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_TEXTURE);
            ts[i].setTexture(texture[i]);
            ts[i].setEnabled(true);

            }
        exposeImage(0);
        currentImage = 0;

        return node;

    }
}
