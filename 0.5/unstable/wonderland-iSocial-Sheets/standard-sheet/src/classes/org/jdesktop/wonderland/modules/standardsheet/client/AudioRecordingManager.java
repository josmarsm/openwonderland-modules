/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

/**
 *
 * @author nilang
 */
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 * Manages all of the Text Chat tabs in the HUD for the client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class AudioRecordingManager implements AudioRecordingConnection.AudioRecordingListener {

    private static final Logger LOGGER =
            Logger.getLogger(AudioRecordingManager.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/textchat/client/resources/Bundle");
    // A mapping from user names to text chat TextChatPanel. NOTE: Accesses to
    // this Map must by synchronized, and only takes place on the AWT Event Thread.
   // private Map<String, WeakReference<TextChatPanel>> userPanelRefMap;
    private JCheckBoxMenuItem textChatMenuItem;
    public AudioRecordingConnection textChatConnection;
    private String localUserName;
  //  private TextChatPanelTabbedPane chatContainerPane = new TextChatPanelTabbedPane();
    private static final String TEXT_CHAT_ALL = "";
    private HUDComponent chatHud = null;

    /**
     * Singleton to hold instance of ChatManager. This holder class is loaded
     * on the first execution of ChatManager.getChatManager().
     */
    private static class AudioRecordingManagerHolder {

        private final static AudioRecordingManager manager = new AudioRecordingManager();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final AudioRecordingManager getAudioRecordingManager() {
        return AudioRecordingManagerHolder.manager;
    }

    /**
     * Private constructor, singelton pattern
     */
    private AudioRecordingManager() {
   //     userPanelRefMap = new HashMap<String, WeakReference<TextChatPanel>>();

        // Create the global text chat menu item. Listen for when it is
        // selected or de-selected and show/hide the frame as appropriate. This
        // menu item will get added/removed for each primary session.
      
    }

    /**
     * Registers the primary session
     * @param session the primary session
     */
    public void register(WonderlandSession session) {
        // Capture the local user name for later use
        localUserName = session.getUserID().getUsername();

        // Create a new custom connection to receive text chats. Register a
        // listener that handles new text messages. Will display them in the
        // window.
        textChatConnection = new AudioRecordingConnection();
        textChatConnection.addTextChatListener(this);

        // Open the text chat connection. If unsuccessful, then log an error
        // and return.
        try {
            textChatConnection.connect(session);
        } catch (ConnectionFailureException excp) {
            LOGGER.log(Level.WARNING, "Unable to establish a connection to " +
                    "the chat connection.", excp);
            return;
        }

        // Create the main HUD component and the default "All" panel, on the AWT Event Thread.
   
    }

    /**
     * Creates and returns a new text chat HUD Component.
     *
     * NOTE: This method assumes it is being called on the AWT Event Thread
     */
  

    /**
     * Creates a new tab in the chat HUD. The panel may exist already and in that
     * case we just create a tab to host the panel.
     * Finally puts the reference to the panel in the user-panel map.
     * @param existingTextChatPanel if null creates a new one, if not it had been
     * created before but the user has closed the tab.
     * @param textChatWith Text (from bundle)
     * @param user user to establish the chat with
     * @param message Message to append if receiving a message from another user
     * for the first time (If they have opened a direct chat to us). Will be empty
     * in the case that we are opening conversation (or text chat all).
     */
 

    /**
     * Unregister and menus we have created, etc.
     * NOTE: (by Design) this is only called on 'Logout' but never on 'Exit'
     * Potential TODO: Register a UserListener to do clean up on 'Exit'
     */
    public void unregister() {
        // First remove the listen for incoming text chat messages.
        textChatConnection.removeTextChatListener(this);

     
    }
}



    /**
     * Creates a new text chat tab, given the remote participants user name
     * and displays it.
     *
     * @param remoteUser The remote participants user name
     */
 
