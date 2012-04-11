/**
 * iSocial Project http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights
 * Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as subject to the
 * "Classpath" exception as provided by the iSocial project in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.instructortools.client;

import com.jme.math.Vector3f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.softphone.MicrophoneInfoListener;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SpeakerInfoListener;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClientPlugin;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.instructortools.client.presenters.AdjustAudioPresenter;
import org.jdesktop.wonderland.modules.instructortools.client.views.AdjustAudioWindow;
import org.jdesktop.wonderland.modules.instructortools.client.views.ImageViewerWindow;
import org.jdesktop.wonderland.modules.instructortools.common.*;

/**
 *
 * @author Ryan
 */
public class InstructorClientConnection extends BaseConnection
        implements MicrophoneInfoListener, SpeakerInfoListener {

    private static final Logger logger = Logger.getLogger(InstructorClientConnection.class.getName());
    private static InstructorClientConnection INSTANCE;
    private static final String SPEAKER_VOLUME_COMMAND = "speakerVolume=";
    private static final String MIC_VOLUME_COMMAND = "microphoneVolume=";
    private int myMicrophoneVolume = 50;
    private int mySpeakerVolume = 50;
    private VolumeConverter volumeConverter = new VolumeConverter(100);

    private InstructorClientConnection() {
        
        SoftphoneControlImpl.getInstance().addMicrophoneInfoListener(this);
        SoftphoneControlImpl.getInstance().addSpeakerInfoListener(this);
    }

    public static InstructorClientConnection getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new InstructorClientConnection();
        }

        return INSTANCE;
    }

    public ConnectionType getConnectionType() {
        return InstructorToolsConnectionType.CLIENT_TYPE;
    }

    public void handleMessage(Message message) {
        //do nothing for now
        if (message instanceof PullToMeMessage) {
            handlePullToMeMessage((PullToMeMessage) message);
        } else if (message instanceof ReconnectSoftphoneMessage) {
            handleReconnectSoftphoneMessage();
        } else if (message instanceof RequestScreenShotMessage) {
            BigInteger source = ((RequestScreenShotMessage) message).getSourceID();
            handleRequestSnapshotMessage(source);
        } else if (message instanceof ScreenShotResponseMessage) {
            handleScreenShotResponseMessage((ScreenShotResponseMessage) message);
        } else if (message instanceof AudioRequestMessage) { //sent by guide
            handleAudioRequestMessage((AudioRequestMessage) message);
        } else if (message instanceof AudioResponseMessage) { //sent by student
            handleAudioResponseMessage((AudioResponseMessage) message);
        } else if (message instanceof AudioChangeMessage) {//sent by guide
            handleAudioChangeMessage((AudioChangeMessage) message);
        }
    }

    public void handlePullToMeMessage(PullToMeMessage message) {
        logger.warning("Received pull to me message!");
        try {
            ViewCell viewCell = ViewManager.getViewManager().getPrimaryViewCell();
            CellTransform transform = viewCell.getWorldTransform();
            Vector3f newPosition = new Vector3f(message.getX(),
                    message.getY(),
                    message.getZ());
            ClientContextJME.getClientMain().gotoLocation(null,
                    newPosition,
                    transform.getRotation(null));
        } catch (IOException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleReconnectSoftphoneMessage() {
        logger.warning("Reconnecting Softphone!");
        AudioManagerClient client = AudioManagerClientPlugin.getClient();
        client.connectSoftphone();
    }

    public void handleToggleSecurityMessage() {
    }

    public void handleRequestStudentAudioMessage() {
    }

    public void handleRequestSnapshotMessage(BigInteger sourceID) {
        logger.warning("Handling screenshot request message!");
        Robot robot;
        try {
            //set up our robot to take the screenshot 
            JFrame main = JmeClientMain.getFrame().getFrame();
            robot = new Robot(main.getGraphicsConfiguration().getDevice());

            Rectangle deviceBounds = main.getGraphicsConfiguration().getBounds();
            Rectangle bounds = main.getBounds();
            bounds.setLocation(bounds.x - deviceBounds.x,
                    bounds.y - deviceBounds.y);


            //create the image data. i.e. "Take the picture"
            BufferedImage bImg = robot.createScreenCapture(bounds);

            //send a response message
            sendScreenShotResponseMessage(sourceID, uploadPicture(bImg));


        } catch (IOException ex) {
//            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            logger.warning("Error creating image to send as a response!");
            ex.printStackTrace();

        } catch (AWTException ex) {
            logger.warning("Error creating robot for device!");


            try {
                JFrame main = JmeClientMain.getFrame().getFrame();
                robot = new Robot();
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle deviceBounds = env.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
                Rectangle bounds = main.getBounds();
                bounds.setLocation(bounds.x - deviceBounds.x,
                        bounds.y - deviceBounds.y);


                //create the image data. i.e. "Take the picture"
                BufferedImage bImg = robot.createScreenCapture(bounds);

                String url = uploadPicture(bImg);

                sendScreenShotResponseMessage(sourceID, url);
            } catch (IOException ex1) {
                Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex1);
                logger.warning("Error creating image to send as a response!");
                ex1.printStackTrace();

            } catch (AWTException ex1) {
                logger.warning("Error creating robot!");
                ex1.printStackTrace();
            }

        }
    }

    public void handleScreenShotResponseMessage(ScreenShotResponseMessage message) {
        logger.warning("received screenshot!");
        try {

            BufferedImage bImg = ImageIO.read(new URL(message.getUrl()));


            showScreenShotInWindow(bImg);

        } catch (IOException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleAudioRequestMessage(AudioRequestMessage message) {
        //somehow we need to get the current volume of the softphone.
        logger.warning("HANDLING AUDIO REQUEST MESSAGE:");
        logger.warning("SENDING AUDIO RESPONSE MESSAGE!");
        this.send(new AudioResponseMessage(getSession().getID(), //source
                message.getSourceID(), //target from sourceID
                myMicrophoneVolume,
                mySpeakerVolume));
    }

    public void handleAudioResponseMessage(AudioResponseMessage message) {
        logger.warning("HANDLING AUDIO RESPONSE MESSAGE:");
        logger.warning("SHOWING ADJUST AUDIO WINDOW!");

        /**
         * We need to be careful here. This method will create a new
         * HUDComponent for every message it receives, even if that message is
         * from a client that already has a control window open for them.
         * Possible ideas include:
         *
         * 1) Create a map of HUDComponents and reference them here. This map
         * would only ever exist on an admin's client.
         *
         * 2) Continue creating new HUDComponents and hope they get garbage
         * collected when they're closed.
         */
        //create view and hudComponent
        AdjustAudioWindow view = new AdjustAudioWindow();
        HUDComponent hudComponent = null;
        HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
        hudComponent = main.createComponent(view);
        hudComponent.setDecoratable(true);
        hudComponent.setPreferredLocation(CompassLayout.Layout.EAST);


        main.addComponent(hudComponent);



        //create presenter
        AdjustAudioPresenter presenter = new AdjustAudioPresenter(message.getSource(),
                view, hudComponent);
        //show window
        logger.warning("SETTING MICROPHONE SLIDER FROM REMOTE: "+message.getMicVolume());
        logger.warning("SETTING SPEAKER SLIDER FROM REMOTE: "+message.getSpeakerVolume());
        presenter.setMicrophoneSliderValue(message.getMicVolume());
        presenter.setSpeakerSliderValue(message.getSpeakerVolume());
        presenter.setVisible(true);
    }

    public void handleAudioChangeMessage(AudioChangeMessage message) {
        logger.warning("HANDLING AUDIO CHANGED MESSAGE!");
        setSoftphoneSpeakerVolume(message.getSpeakerVolume());
        setSoftphoneMicrophoneVolume(message.getMicVolume());
    }

    public void sendPullToMeMessage(Set<BigInteger> ids, float x, float y, float z) {
        PullToMeMessage msg = new PullToMeMessage(ids, x, y, z);
        this.send(msg);
    }

    public void sendReconnectSoftphoneMessage(Set<BigInteger> ids) {
        logger.warning("Sending reconnect softphone message!");

//        AConnection audioConnection = (BaseConnection)getSession().getConnection(AudioManagerConnectionType.CONNECTION_TYPE);
        this.send(new ReconnectSoftphoneMessage(ids));


    }

    public void sendScreenShotRequestMessage(Set<BigInteger> ids) {
        logger.warning("Sending a request for a screenshot.");
        BigInteger sourceID = LoginManager.getPrimary().getPrimarySession().getID();
        this.send(new RequestScreenShotMessage(ids, sourceID));
    }

    public void sendAudioChangeMessage(BigInteger target, float micVolume, float speakerVolume) {
        logger.warning("SENDING AUDIO CHANGE MESSAGE:\n"
                + "target: " + target.toString() + "\n"
                + "micVolume:" + micVolume + "\n"
                + "speakerVolume: " + speakerVolume);
        this.send(new AudioChangeMessage(target, micVolume, speakerVolume));
    }

    private void sendScreenShotResponseMessage(BigInteger sourceID, String url) throws IOException {
        logger.warning("Sending screenshot response message!");


        //send the 1s and 0s to requester of the screenshot
        this.send(new ScreenShotResponseMessage(sourceID, url));
//        handleScreenShotResponseMessage(new ScreenShotResponseMessage(sourceID, imageData));
    }

    private void showScreenShotInWindow(final BufferedImage image) {
        /**
         * We don't use M-V-P as no model, or presenter are needed, just a view.
         */
        logger.warning("Showing screenshot!");
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ImageViewerWindow window = new ImageViewerWindow(image);

                window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                window.pack();

                window.setVisible(true);
            }
        });


    }

    public void sendAudioRequestMessage(Set<BigInteger> IDs) {
        logger.warning("SENDING AUDIO REQUEST MESSAGE!");
        this.send(new AudioRequestMessage(IDs, getSession().getID()));
    }

//    private byte[] compressJPG(BufferedImage in) {
//        try {
//            // set up reader/writer to wrap around files
//            ImageWriter writer = null;
//            Iterator iterator = ImageIO.getImageWritersByFormatName("jpg");
//            if(iterator.hasNext()) {
//                writer = (ImageWriter)iterator.next();
//            }
//            File file = new File("tmp");
//            ImageOutputStream ios = ImageIO.createImageOutputStream(file);
//            writer.setOutput(ios);
//            ByteArrayOutputStream s = new ByteArrayOutputStream();
//            ImageWriteParam parameter = new ImageWriteParam(Locale.getDefault());
//            parameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            parameter.setCompressionQuality(0);
//            
//            writer.write(null, new IIOImage(in, null, null), parameter);
//           
//        } catch (IOException ex) {
//            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        
//    }
    private ContentCollection getScreenshotsRoot() throws ContentRepositoryException {
        ServerSessionManager manager = getSession().getSessionManager();
        ContentRepository repository = ContentRepositoryRegistry.getInstance().getRepository(manager);
        ContentCollection root = repository.getRoot();

        ContentCollection groupsRoot = (ContentCollection) root.getChild("groups");
        ContentCollection mediaRoot = (ContentCollection) groupsRoot.getChild("media");
        ContentCollection screenshots = (ContentCollection) mediaRoot.getChild("screenshots");

        return screenshots;

    }

    private boolean filenameExists(ContentCollection root, String filename) throws ContentRepositoryException {
        if (root.getChild(filename) != null) {
            return true;
        }

        return false;
    }

    private String uploadPicture(BufferedImage image) {

        String url = "NOT-A-URL";
        try {
            ContentCollection screenshots = getScreenshotsRoot();

            String filename = new Long(System.currentTimeMillis()).toString() + ".jpg";
            while (filenameExists(screenshots, filename)) {
                filename = new Long(System.currentTimeMillis()).toString() + ".jpg";
            }
            File file = new File(filename);
            //filename should now be unique enough such that it doesn't already exist on
            //the server.

            ImageIO.write(image, "jpg", file);

            ContentResource screenshot =
                    (ContentResource) screenshots.createChild(file.getName(),
                    Type.RESOURCE);
            screenshot.put(file);

            url = screenshot.getURL().toString();


        } catch (IOException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            return url;
        }

    }

    private void setSoftphoneSpeakerVolume(float volume) {
        try {
            logger.warning("SETTING SOFTPHONE SPEAKER VOLUME: "+volume);
            SoftphoneControlImpl.getInstance().sendCommandToSoftphone(SPEAKER_VOLUME_COMMAND + volume);
        } catch (IOException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setSoftphoneMicrophoneVolume(float volume) {
        try {
            logger.warning("SETTING SOFTPHONE MICROPHONE VOLUME: "+volume);
            SoftphoneControlImpl.getInstance().sendCommandToSoftphone(MIC_VOLUME_COMMAND + volume);
        } catch (IOException ex) {
            Logger.getLogger(InstructorClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void microphoneVuMeterValue(String value) {
//        logger.warning("MICROPHONE METER VALUE CHANGED: " + value);
    }

    public void microphoneVolume(String volume) {
//        logger.warning("MY MICROPHONE VOLUME CHANGED: " + volume);

        VolumeConverter converter = new VolumeConverter(100);
        logger.warning("CONVERTING MICROPHONE VOLUME FROM: "+volume+""
                + " TO: "+converter.getVolume(Float.parseFloat(volume)));
        myMicrophoneVolume = converter.getVolume(Float.parseFloat(volume));
        

    }

    public void speakerVuMeterValue(String value) {
//        logger.warning("SPEAKER METER VALUE CHANGED: " + value);
    }

    public void speakerVolume(String volume) {
        VolumeConverter converter = new VolumeConverter(100);
        logger.warning("CONVERTING SPEAKER VOLUME FROM: "+volume+""
                + " TO: "+converter.getVolume(Float.parseFloat(volume)));
//        myMicrophoneVolume = converter.getVolume(Float.parseFloat(volume));
        mySpeakerVolume = converter.getVolume(Float.parseFloat(volume));
    }
}
