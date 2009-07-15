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

package org.jdesktop.wonderland.modules.pdfspreader.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.pdfspreader.client.PDFSpreaderCell;

public class PDFSpreaderCellRenderer extends BasicRenderer {
    private Node node = null;

    private PDFSpreaderCell pdfCell;

    private static final Logger logger =
            Logger.getLogger(PDFSpreaderCellRenderer.class.getName());

    private PDFFile pdf;

    private float spacing = 4.0f;
    private float scale = 1.0f;


    public PDFSpreaderCellRenderer(Cell cell) {
        super(cell);
        this.pdfCell = (PDFSpreaderCell) cell;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {

        node = new Node();

        Thread t = new PageLoadingThread(node);
        t.start();

//        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        node.setLocalRotation(new Quaternion().fromAngleNormalAxis((float) (Math.PI / 2), new Vector3f(0,1,0)));
        node.setLocalScale(scale);

        node.setModelBound(new BoundingBox());
        return node;
    }

    /**
     * Get an image of a specific page
     *
     * Taken from PDFViewerApp.java, with some modifications.
     *
     * @param p the page number
     * @return the image of the specified page
     */
    public BufferedImage getPageImage(int p) {
        BufferedImage image = null;

        PDFPage currentPage;
        try {
            if (isValidPage(p)) {
                currentPage = pdf.getPage(p, true);
                double ph = currentPage.getWidth()*2;
                double pw = currentPage.getHeight()*2;
//                double aw = (double) this.getWidth();
//                double ah = (double) this.getHeight();

                // request a page that fits the width of the viewer and has
                // the correct aspect ratio
//                int rw = (int) aw;
//                int rh = (int) (ph * (aw / pw));

                Image img = pdf.getPage(p).getImage((int)pw, (int)ph, null, null, true, true);

                logger.warning("img from pdfpage: " + img);

                if (img != null) {
                    // convert the page image into a buffered image
                    image = new BufferedImage((int)pw, (int)ph, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = image.createGraphics();
                    g2.drawImage(img, 0, 0, (int)pw, (int)ph, null);
                } else {
                    logger.warning("PDF viewer failed to get image for page: " + p);
                }

            }
        } catch (Exception e) {
            logger.severe("PDF viewer failed to get page image: " + e);
        }

        return image;
    }

    /**
     * Get the validity of the specified page in the currently open document
     * @return true if the page is within the range of pages of the current
     * document, false otherwise
     */
    public boolean isValidPage(int p) {
        return ((pdf != null) && (p > 0) && (p <= pdf.getNumPages()));
    }


    /**
     * Get the PDF document data from a URL
     *
     * (Taken verbatim from PDFViewerApp.java)
     *
     * @param docURL the URL of the PDF document to open
     * @return the PDF document data
     */
    public ByteBuffer getDocumentData(String docURL) throws IOException {
        ByteBuffer buf = null;

        if (docURL != null) {
            // connect to the URL
            logger.warning("About to open connection to PDF URI");
            URL url = AssetUtils.getAssetURL(docURL, cell);
            URLConnection conn = url.openConnection();
            conn.connect();

            logger.warning("Connection open, size: " + conn.getContentLength());
            // create a buffer to load the document into
            int docSize = conn.getContentLength();

            // Just try reading from it, first.

//            byte[] data = new byte[docSize];

            // create a buffered stream for reading the document
            DataInputStream is = new DataInputStream(new BufferedInputStream(conn.getInputStream()));


            logger.warning("reading document");
            // read the document into the buffer
//            is.readFully(data, 0, docSize);

            Vector<Byte> bytesVec = new Vector<Byte>();
            byte b;
            int numBytes = 0;
            while(true) {
                try {
                    b = is.readByte();

                    bytesVec.add(b);
                    numBytes++;
                } catch (EOFException e) {
                    break;
                } catch (Exception e) {
                    logger.warning("Error reading PDF in loop: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

            }

            logger.warning("bytes read: " + numBytes);
            logger.warning("document read, converting to byte[]");

            byte[] bytes = new byte[bytesVec.size()];
            int i = 0;
            for(Byte curB : bytesVec) {
                bytes[i] = bytesVec.get(i);
                i++;
            }

            buf = ByteBuffer.wrap((byte[]) bytes, 0, ((byte[]) bytes).length);
        }

        logger.warning("returning buffer: " + buf + " with length: " + buf.array().length);

        return buf;
    }

    private class PageLoadingThread extends Thread {

        private Node node;

        public PageLoadingThread(Node n) {
            node = n;
        }

        public void run() {

            logger.info("Creating scene graph for entity: " + entity);

            String name = cell.getCellID().toString();

            String pdfURI = ((PDFSpreaderCell) cell).getSourceURI();
            try {
                logger.warning("PDF loading: " + pdfURI);

                Date then = new Date();
                logger.warning("really about to load, date: " + then.getTime());
                pdf = new PDFFile(getDocumentData(pdfURI));
                Date now = new Date();

                logger.warning("PDF loaded in: " + (now.getTime() - then.getTime()) + "ms");
            } catch (Exception e) {
                logger.warning("PDF failed to load: " + e.getMessage());
                e.printStackTrace();
            }
//        TriMesh mesh = new Box(cell.getCellID().toString(), new Vector3f(), 0.1f, 2, 2f);

            // loop through the number of pages we have to render.
            //

//            float totalArc = (float) Math.PI;
//            float arcPerStep = totalArc / pdf.getNumPages();
//            float radius = 10;

            Vector3f currentCenter = new Vector3f();

            // centered around 0,0, calculate starting position.
            
            for (int i = 1; i <= pdf.getNumPages(); i++) {
                logger.warning("currentCenter: " + currentCenter + " (page " + i + ")");
                // for each page, we need to:
                //   1. make a new Box
                //   2. get the image for this page and make it into a texture
                //   3. apply the texture to the box
                //   4. move the position pointer for the next box

                BufferedImage pageTexture = getPageImage(i);

                logger.warning("pageTexture: " + pageTexture);

                // Dispatch the JME specific stuff to a thread that we can run inside
                // the RenderingThread.
                ClientContextJME.getWorldManager().addRenderUpdater((RenderUpdater)new NewSlideUpdater(node, pageTexture, currentCenter, i + "_"), null);

                // move the pointer for the next position
                currentCenter = currentCenter.add(0, 0, 4);
            }
        }
    }


    private class NewSlideUpdater implements RenderUpdater {

        private Node parent;
        private BufferedImage page;
        private Vector3f center;
        private String name;

        public NewSlideUpdater(Node p, BufferedImage texture, Vector3f c, String n) {
            parent = p;
            page = texture;
            center = c;
            name = n;
        }

        public void update(Object arg0) {


                logger.warning("In NEW SLIDE UPDATER about to make a new slide object and attach it.");
                Texture texture = TextureManager.loadTexture(page, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

//              Texture texture = TextureManager.loadTexture(TextureManager.loadImage(), Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, true)
                texture.setWrap(Texture.WrapMode.BorderClamp);
                texture.setTranslation(new Vector3f());

                // Figure out what the size of the texture is, scale it down to something
                // reasonable.
                float width = texture.getImage().getWidth() * 0.00125f;
                float height = texture.getImage().getHeight() * 0.00125f;

                TriMesh currentSlide = new Box(cell.getCellID().toString() + "_" + name, center.clone(), 0.1f, width, height);
                node.attachChild(currentSlide);
                logger.warning("Just attached slide to node, with (" + width + "," + height + ")");

                RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

                TextureState ts = (TextureState) rm.createRendererState(RenderState.StateType.Texture);
                ts.setTexture(texture);
                ts.setEnabled(true);

                MaterialState ms = (MaterialState) rm.createRendererState(RenderState.StateType.Material);
                ms.setDiffuse(ColorRGBA.white);
                ms.setColorMaterial(MaterialState.ColorMaterial.Diffuse);
                currentSlide.setRenderState(ms);

                currentSlide.setRenderState(ts);

                currentSlide.setSolidColor(ColorRGBA.white);

                // This is not the right place to be doing this - it really only
                // needs to get done after the last slide. But it's easier than
                // making sure we run an update only at the end, and it keeps
                // the bounds constantly up to date as slides get added.
                node.updateModelBound();
                node.updateRenderState();
                ClientContextJME.getWorldManager().addToUpdateList(node);
        }
    }
}