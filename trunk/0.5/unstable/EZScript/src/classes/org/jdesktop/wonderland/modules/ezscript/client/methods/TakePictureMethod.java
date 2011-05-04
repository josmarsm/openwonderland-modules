/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.test.CaptureSystemTest;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;


/**
 *
 * @author JagWire
 */



@ScriptMethod
public class TakePictureMethod implements ScriptMethodSPI {


    private CaptureStream stream;
    private CaptureSystem system;
    private Semaphore lock = new Semaphore(0);
    private int counter = 0;
    public String getFunctionName() {
        return "TakePicture";
    }

    public void setArguments(Object[] args) {
        try {
            CaptureSystemFactory factory = DefaultCaptureSystemFactorySingleton.instance();
            system = factory.createCaptureSystem();
            system.init();
            List list = system.getCaptureDeviceInfoList();
            CaptureDeviceInfo info = (CaptureDeviceInfo)list.toArray(new CaptureDeviceInfo[] { })[0];

            stream = system.openCaptureDeviceStream(info.getDeviceID());


        } catch (CaptureException ex) {
            Logger.getLogger(TakePictureMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getDescription() {
        return "Take a picture with the default webcam.";
    }

    public String getCategory() {
        return "media";
    }

    public void run() {
        try {
            stream.setObserver(new SimpleCaptureObserver());
            stream.start();
            //Thread.sleep(2000); //wait half a second

            
            lock.acquire();
            
            stream.stop();
            stream.dispose();
            system.dispose();

        } catch (CaptureException ex) {
            Logger.getLogger(TakePictureMethod.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ie) {
            Logger.getLogger(TakePictureMethod.class.getName()).log(Level.SEVERE, null, ie);
        }
    }


    class SimpleCaptureObserver implements CaptureObserver
    {

	public void onError(CaptureStream sender, CaptureException e)
	{	System.err.println("onError " + sender);
		e.printStackTrace();
	}


	public void onNewImage(CaptureStream sender, Image image)
	{
		final BufferedImage bimg;
                counter += 1;
                if(counter < 2)
                    return;
                
		try
		{
                    final VideoFormat format = image.getFormat();
                    System.out.println("onNewImage format=" + CaptureSystemTest.videoFormatToString(format) + " length=" + image.getBytes().length);
                    bimg = AWTImageConverter.toBufferedImage(image);



//		 Encode as a JPEG

//			FileOutputStream fos = new FileOutputStream("out.jpg");
//			JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(fos);
//			jpeg.encode(bimg);
//			fos.close();

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            JLabel label = new JLabel();
                            label.setText(null);
                            label.setIcon(new ImageIcon(bimg));
                            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                            HUDComponent component = mainHUD.createComponent(label);
                            component.setPreferredLocation(Layout.CENTER);
                            component.setDecoratable(true);

                            mainHUD.addComponent(component);
                            component.setVisible(true);
                        }
                    });
		} catch (Exception e) {
                    e.printStackTrace();
		
		} finally {
                    if(counter == 2)
                        lock.release();
                }
	}

    }

}
