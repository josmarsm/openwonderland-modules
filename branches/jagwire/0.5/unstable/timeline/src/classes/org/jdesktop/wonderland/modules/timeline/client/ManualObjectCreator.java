/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client;

import com.jme.bounding.BoundingVolume;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.modules.contentrepo.client.importer.ContentRepositoryImporter;
import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.ModelDndContentImporter;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderClientPlugin;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedAudio;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedModel;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 * A class for generating objects from a file
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ManualObjectCreator {
    private static final Logger logger =
            Logger.getLogger(ManualObjectCreator.class.getName());

    private static final Map<String, ObjectCreator> creators =
            new LinkedHashMap<String, ObjectCreator>();

    private TimelineDate date;
    private String file;
    private String text;

    static {
        AudioCreator ac = new AudioCreator();
        for (String extension : ac.getExtensions()) {
            creators.put(extension, ac);
        }

        ImageCreator ic = new ImageCreator();
        for (String extension : ic.getExtensions()) {
            creators.put(extension, ic);
        }

        ModelCreator mc = new ModelCreator();
        for (String extension : mc.getExtensions()) {
            creators.put(extension, mc);
        }
    }

    public ManualObjectCreator(TimelineDate date, String file, String text) {
        this.date = date;
        this.file = file;
        this.text = text;
    }

    public static Set<String> getExtensions() {
        return creators.keySet();
    }

    public DatedObject create() {
        int idx = file.lastIndexOf(".");
        if (idx == -1) {
            logger.warning("Can't determine extension");
            return null;
        }
        String extension = file.substring(idx + 1);
        
        ObjectCreator creator = creators.get(extension);
        if (creator == null) {
            logger.warning("No creator for " + extension);
            return null;
        }
        
        return creator.create(date, file, extension, text);
    }

    public interface ObjectCreator {
        public Set<String> getExtensions();
        public DatedObject create(TimelineDate date, String file, String extension,
                                  String text);
    }

    static class AudioCreator implements ObjectCreator {
        private static final Set<String> EXTENSIONS = new LinkedHashSet<String>();
        static {
            EXTENSIONS.add("au");
            EXTENSIONS.add("mp3");
        }

        public Set<String> getExtensions() {
            return EXTENSIONS;
        }

        public DatedObject create(TimelineDate date, String file, String extension,
                                  String text)
        {
            ContentRepositoryImporter importer = new ContentRepositoryImporter() {
                @Override
                public void createCell(String uri) {
                    // don't create the cell
                }
            };
            importer.initialize(TimelineProviderClientPlugin.getServerSessionManager());
            String uri = importer.importFile(new File(file), extension);

            return new DatedAudio(date, uri);
        }
    }

    static class ImageCreator implements ObjectCreator {
        private static final Set<String> EXTENSIONS = new LinkedHashSet<String>();
        static {
            EXTENSIONS.add("jpg");
            EXTENSIONS.add("gif");
            EXTENSIONS.add("png");
        }

        public Set<String> getExtensions() {
            return EXTENSIONS;
        }

        public DatedObject create(TimelineDate date, String file, String extension,
                                  String text)
        {
            ContentRepositoryImporter importer = new ContentRepositoryImporter() {
                @Override
                public void createCell(String uri) {
                    // don't create the cell
                }
            };
            importer.initialize(TimelineProviderClientPlugin.getServerSessionManager());
            String uri = importer.importFile(new File(file), extension);

            // create the image
            DatedImage di = new DatedImage(date, uri);

            // Figure out a good sizing hint for the image. We first fetch the image
            // (it's ok to do it here, it'll get cached so that later it won't take
            // so long) and figure out its size. From that, we generate the bounding
            // hint.
            try {
                URL url = AssetUtils.getAssetURL(uri);
                Texture texture = TextureManager.loadTexture(url);
                com.jme.image.Image image = texture.getImage();
                float width = image.getWidth() * ImageViewerCell.WIDTH_SCALE_FACTOR;
                float height = image.getHeight() * ImageViewerCell.HEIGHT_SCALE_FACTOR;

                di.setWidth((int) width);
                di.setHeight((int) height);
            } catch (MalformedURLException excp) {
                logger.log(Level.WARNING, "Unable to form url from " + uri, excp);
            }

            return di;
        }
    }

    static class ModelCreator implements ObjectCreator {
        private static final Set<String> EXTENSIONS = new LinkedHashSet<String>();
        static {
            EXTENSIONS.add("kmz");
        }

        private DeployedModel dm;

        public Set<String> getExtensions() {
            return EXTENSIONS;
        }

        public DatedObject create(TimelineDate date, String file, String extension,
                                  String text)
        {
            ModelDndContentImporter importer =
                    new ModelDndContentImporter(TimelineProviderClientPlugin.getServerSessionManager(),
                                                new String[] { "kmz" })
            {
                //@Override
                public void createCell(DeployedModel deployedModel) {
                    // don't creat the cell, just rememer the model
                    ModelCreator.this.dm = deployedModel;
                }
            };

            String uri = importer.importFile(new File(file), extension);

            // get the various configurations for the model
            return new DatedModel(date, uri, dm.getModelTranslation(),
                                  dm.getModelScale(), dm.getModelRotation(),
                                  dm.getModelLoaderClassname());
        }
    }
}
