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
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
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

    public PDFSpreaderCellRenderer(Cell cell) {
        super(cell);
        this.pdfCell = (PDFSpreaderCell) cell;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {

        logger.info("Creating scene graph for entity: " + entity);

        String name = cell.getCellID().toString();

        String pdfURI = ((PDFSpreaderCell)cell).getSourceURI();
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
        Vector3f currentCenter  = new Vector3f();

        node = new Node();

        for(int i=0; i<pdf.getNumPages(); i++) {
            logger.warning("currentCenter: " + currentCenter + " (page " + i + ")");
            // for each page, we need to:
            //   1. make a new Box
            //   2. get the image for this page and make it into a texture
            //   3. apply the texture to the box
            //   4. move the position pointer for the next box


            BufferedImage pageTexture = this.getPageImage(i);

            logger.warning("pageTexture: " + pageTexture);

            // Load the texture first to get the image size
            Texture texture = TextureManager.loadTexture(pageTexture, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

//            Texture texture = TextureManager.loadTexture(TextureManager.loadImage(), Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, true)
            texture.setWrap(Texture.WrapMode.BorderClamp);
            texture.setTranslation(new Vector3f());

            // Figure out what the size of the texture is, scale it down to something
            // reasonable.
            float width = texture.getImage().getWidth() * 0.0025f;
            float height = texture.getImage().getHeight() * 0.0025f;

            TriMesh currentSlide = new Box(cell.getCellID().toString() + "_" + i, currentCenter.clone(), 0.1f, width, height);
            node.attachChild(currentSlide);

            TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
            ts.setTexture(texture);
            ts.setEnabled(true);

            currentSlide.setSolidColor(ColorRGBA.white);
            
            currentSlide.setRenderState(ts);

            // move the pointer for the next position
            currentCenter = currentCenter.add(0, 0, 4);
        }

//        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

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
                double pw = currentPage.getWidth();
                double ph = currentPage.getHeight();
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
}