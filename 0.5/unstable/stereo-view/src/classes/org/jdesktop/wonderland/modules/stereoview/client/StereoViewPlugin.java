package org.jdesktop.wonderland.modules.stereoview.client;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Plugin for stereo view
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Plugin
public class StereoViewPlugin extends BaseClientPlugin {

    // The error logger
    private static Logger LOGGER =
            Logger.getLogger(StereoViewPlugin.class.getName());

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/stereoview/client/resources/Bundle");

    // The menu item to add to the Windows menu
    private JCheckBoxMenuItem stereoMI = null;
    private JCheckBoxMenuItem configMI = null;
    
    
    // The entity for viewing
    private StereoViewEntity entity = null;
    
    // the view frame
    private StereoViewFrame frame = null;

    // the configure frame
    private ConfigureStereoViewFrame config = null;
    
    // the inter axial distance
    private float dist;
    
    // the angle
    private float angle;
    
    @Override
    protected void activate() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stereoMI = new JCheckBoxMenuItem(BUNDLE.getString("Stereo_View"));
                stereoMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setWindowVisible(stereoMI.isSelected());
                    }
                });
                
                configMI = new JCheckBoxMenuItem(BUNDLE.getString("Stereo_View_Config"));
                configMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setConfigVisible(configMI.isSelected());
                    }
                });
                
                JmeClientMain.getFrame().addToWindowMenu(stereoMI, 500);
                JmeClientMain.getFrame().addToWindowMenu(configMI, 501);
            } 
        });
    }
    
    @Override
    protected void deactivate() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setWindowVisible(false);
                setConfigVisible(false);
                
                if (stereoMI != null) {
                    JmeClientMain.getFrame().removeFromWindowMenu(stereoMI);
                    stereoMI = null;
                }
                
                if (configMI != null) {
                    JmeClientMain.getFrame().removeFromWindowMenu(configMI);
                    configMI = null;
                }
            }
        });
    }
        
    synchronized void setDistance(float dist) {
        this.dist = dist;
        
        if (entity != null) {
            entity.setDistance(dist);
        }
    }
    
    synchronized float getDistance() {
        return dist;
    }
    
    synchronized void setAngle(float angle) {
        this.angle = angle;
        
        if (entity != null) {
            entity.setAngle(angle);
        }
    }
    
    synchronized float getAngle() {
        return angle;
    }
    
    // must be called on AWT event thread
    private void setWindowVisible(boolean visible) {
        if (visible) {
            if (entity != null) {
                throw new IllegalStateException("Entity already created");
            }
            
            // create the image
            Canvas canvas = JmeClientMain.getFrame().getCanvas();
            
            synchronized (this) {
                entity = new StereoViewEntity(canvas.getWidth(), canvas.getHeight());
                frame = new StereoViewFrame(createImage(entity, 
                                                        canvas.getWidth(), canvas.getHeight()));
            }
            
            entity.setDistance(getDistance());
            entity.setAngle(getAngle());
            entity.setEnabled(true);
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    LOGGER.warning("Stereo window closing");
                    
                    // uncheck the checkbox, which should also dispose of
                    // the frame
                    if (stereoMI != null) {
                        stereoMI.setSelected(false);
                    }
                    
                    if (entity != null) {
                        setWindowVisible(false);
                    }
                }
            });
            
            frame.setSize(JmeClientMain.getFrame().getFrame().getSize());
            frame.setVisible(true);
        } else {
            if (entity == null) {
                // already disposed
                return;
            }
            
            frame.dispose();
            entity.dispose();
            
            synchronized (this) {
                frame = null;
                entity = null;
            }
        }
    }
    
    private Image createImage(ImageProducer producer, int width, int height) {
        AffineTransform tx = AffineTransform.getScaleInstance(1.0, -1.0);
        tx.translate(0, -height);
        AffineTransformOp txop = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR); 
        
        FilteredImageSource src = new FilteredImageSource(producer, 
                new BufferedImageFilter(txop));
        return Toolkit.getDefaultToolkit().createImage(src);
    }
    
    // must be called on awt event thread
    private void setConfigVisible(boolean visible) {
        if (visible) {
            if (config != null) {
                config.toFront();
            } else {
                config = new ConfigureStereoViewFrame(this);
                config.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        LOGGER.warning("Config window closing");
                        
                        if (configMI != null) {
                            configMI.setSelected(false);
                        }
                        
                        if (config != null) {
                            config = null;
                        }
                    }
                });
                
                config.pack();
                config.setVisible(true);
            }
        } else {
            if (config != null) {
                config.dispose();
                config = null;
            }
        }
    }
}
