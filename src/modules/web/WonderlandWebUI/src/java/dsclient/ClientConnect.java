/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dsclient;

/**
 *
 * @author antonio
 */
import java.net.PasswordAuthentication;
import javax.vecmath.Vector3f;

import java.util.logging.Level;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Properties;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;
//import java.util.logging.Level;
import java.nio.channels.UnresolvedAddressException;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.SessionId;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;

import java.util.LinkedList;
import javax.vecmath.Point3f;
import org.jdesktop.lg3d.wonderland.appshare.ServerMasterClient;

import org.jdesktop.lg3d.wonderland.darkstar.common.AvatarInfo;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarSetupMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ErrorMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.LoginMessage;

import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellHierarchyMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ProtocolVersion;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.ServerManagerMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.VoiceManagerTuningMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.UserChangedMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AlsSupportedMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarP2PMessage;

import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.PingMessage;

import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfig;

import org.jdesktop.lg3d.wonderland.config.client.AvatarClientConfig;

import org.directwebremoting.WebContext;

import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;

import org.jdesktop.lg3d.wonderland.config.client.PlacemarkConfig;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.AvatarCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.ChannelInfo;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.FindUserMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.SoftphoneMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.AvatarCellSetup;
import org.jdesktop.lg3d.wonderland.scenemanager.AvatarControlBehavior;
import org.jdesktop.lg3d.wonderland.scenemanager.avatar.Avatar;

public class ClientConnect {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private ArrayList<ServerInfo> serverDataList = new ArrayList<ServerInfo>();
  private SimpleClient client = new SimpleClient(new ClientListener());
  private ManagerClientChannel managerChannel = null;
  private ClientChannel avatarCellChannel = null;
  private ClientChannel avatarP2PChannel = null;
  private Semaphore clientConnection = new Semaphore(1);
  private String errorMessage;
  private int usercount;
  private WebContext wc;
  private ClientConnectUp connection;
  private List<String> usersOnline;
  private Map userNameMap; //this will store the userID -> userName
  private Map userIdMap; //this will store the userName -> userId
  
  private LinkedList msgRxQueue;
  
  private String userName;
  private String password;
  private String serverName;
  private String portNum;
  
  private CellID cellId;
  private Point3f currentPosition;
  private PlacemarkConfig sysPMC;
  private PlacemarkConfig userPMC;
  private int sequenceNum=0;

  boolean initialWait = false; //wait 5 seconds before initially processing messages
  
  /** Constructor.
   * 
   * @param userName
   * @param password
   * @param serverName
   * @param portNum
   */
  public ClientConnect(String userName, String password,
                       String serverName, String portNum) {

    logger.log(Level.INFO, "ClientConnect::constructor ... ");

    String SERVERNAME = System.getProperty("sgs.server");
    String SERVERPORT = System.getProperty("sgs.port");

    if (serverDataList.size() == 0) {
      serverDataList.add(new ServerInfo(SERVERNAME, SERVERPORT));
    }

    this.userName = userName;
    this.password = password;
    this.serverName = serverName;
    this.portNum = portNum;

    usersOnline = new ArrayList<String>();
    userNameMap = new HashMap();
    userIdMap = new HashMap();
    
    msgRxQueue = new LinkedList();
    currentPosition = new Point3f(0,0,0);
  }

  /**
   * Mutator for webContext.
   *
   * @param inWebContext The WebContext to store.
   */
  public void setWebContext(final WebContext inWebContext) {
    wc = inWebContext;
  } // And setWebContext().

  /* 
   * use this method to get the server connected and listening
   */
  public int connect() { 
    connection = new ClientConnectUp();
    connection.start();
    
    MessageData newMsgRx = new MessageData(MessageData.MsgType.ChatMessage);     
    newMsgRx.setUserName("SERVER");
    newMsgRx.setChatMessage("Connecting ...");
    msgRxQueue.add(newMsgRx);

    return 1;
  }

  /* 
   * This should close the thread that was used to setup the connection
   */
  public void disconnect() {
    MessageData newMsgRx = new MessageData(MessageData.MsgType.ChatMessage);     
    newMsgRx.setUserName("SERVER");
    newMsgRx.setChatMessage("You have been disconnected!!!");
    msgRxQueue.add(newMsgRx);
      
//    System.out.println(" .... Disconnecting ");
    connection.quit();

    if (client.isConnected()) {
      disconnectFromServer();
    }
  }

  /** Method to disconnect the client.
   * 
   */
  private void disconnectFromServer() {
    client.logout(true);
    connection.quit();
  }

  /* 
   * ***************************************************************************
   * The following section will include all the methods that will access various
   * pieces of information from the darkstar server, and forward them onto the
   * dwr servlet.
   */
  /** Chat method from browser to wonderland server.
   * This method get the data from the user that was entered on the browser, and then
   * forwards it to the wonderland server, and waits for the response to display it
   * back to the browser.
   * @param msg - incoming chat string
   */
  public void sendChat(String msg) {
    //logger.log(Level.INFO,"Sending Chat Message");
    AvatarP2PMessage message = new AvatarP2PMessage(msg);
    try {
      //here i forward the chat information to the darkstar server
      managerChannel.send(message.getBytes());
      
      MessageData newMsgRx = new MessageData(MessageData.MsgType.ChatMessage);     
      newMsgRx.setUserName(userName);
      newMsgRx.setChatMessage(msg);
      msgRxQueue.add(newMsgRx);

//    sysPMC = PlacemarkConfig.getSystemPlacemarks();
//    userPMC = PlacemarkConfig.getUserPlacemarks(null);
//    System.out.println("system pm: " + sysPMC.getPlacemarks());
//    System.out.println("user pm: " + userPMC.getPlacemarks());
    
    } catch (IOException ex) {
      Logger.getLogger(ClientConnect.class.getName()).log(Level.SEVERE, null, ex);
    }
    //logger.log(Level.INFO,"Sending Chat Message");
  }
  
  /** Check which users are online.
   * This method requests the users that are online ( stored locally ) and return them
   * back to the browser.
   * @return String[]
   */
  public String[] usersOnline() {
    int i=0;
    String [] users = new String [usersOnline.size()];
    
    for (Iterator it = usersOnline.iterator(); it.hasNext();) {
      users[i] = (String)it.next();
      i++;
    }
      
    return users;
  }

  /** Return array of server stats.
   * 
   * @return String[] returns array of version and uptime
   */
  public String[] serverStats() {
    //here i forward the chat information to the darkstar server
    //return new String[]{dwrServerVer, dwrProtoVer, dwrUptime};
    return new String[]{"","",""};
  }
  
  /**
   * 
   * @param username
   * @return
   */
  public int goToUser(String username) {
    // send a request to find the given user.  The response will come to
    // the UserChannelListener and be forward to the handleGotoUserResponse()
    // method above.
    logger.log(Level.INFO,"username: " + username );
    String userId = userIdMap.get(username).toString();
    logger.log(Level.INFO,"username: " + username + " ID: " + userId);
    
//    FindUserMessage find = new FindUserMessage(FindUserMessage.ActionType.REQUEST, userId.getBytes());
//    try {
//        UserChannelListener.getUserChannel().send(userId, find.getBytes());
//    } catch (IOException ioe) {                                         
//        logger.log(Level.WARNING, "Error sending goto response " + find, ioe);
//    }
    return 1;
  }
  
  public int goToLocation(float posX, float posY, float posZ) {
//    System.out.println("current pos: " + currentPosition );
//    System.out.println("goto: X-> " + posX + " Y-> " + posY + " Z-> " + posZ);
    
    Vector3f velocity = new Vector3f();
    velocity.sub(currentPosition, new Point3f(posX,posY,posZ));
            
    currentPosition.set(posX, posY, posZ);
    
    AvatarP2PMessage message = new AvatarP2PMessage(currentPosition,                                                    
                                                    AvatarControlBehavior.DEFAULT_LOOK_DIRECTION,
                                                    velocity,
                                                    AvatarControlBehavior.DEFAULT_UP_VECTOR,
                                                    1,
                                                    System.currentTimeMillis());
    
    AvatarCellMessage msg = new AvatarCellMessage(cellId,
                                                  currentPosition, 
                                                  AvatarControlBehavior.DEFAULT_LOOK_DIRECTION);
    
    AvatarCellMessage audiomsg = new AvatarCellMessage(cellId,
                                                  currentPosition, 
                                                  AvatarControlBehavior.DEFAULT_LOOK_DIRECTION,
                                                  sequenceNum++);
    
    try {
      //here i forward the move message to the darkstar server
      avatarP2PChannel.send(message.getBytes());      
      avatarCellChannel.send(msg.getBytes());
      client.send(audiomsg.getBytes());
      
      
    } catch (IOException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    
    MessageData newMsgRx = new MessageData(MessageData.MsgType.AvatarMove);     
    newMsgRx.setUserName(userName);
    newMsgRx.setAvatarMove(posX, posZ);
    msgRxQueue.add(newMsgRx);
    
//    System.out.println("new pos: " + currentPosition );
    
    return 1;
  }
  
  /**
   * 
   * @param number
   * @return
   */
  public int sendCallMe(String number) {
//    System.out.println("Calling: " + number);
    
    Vector3f direction = new Vector3f(1,0,0);
//    System.out.println("Ask server to call softphone: " + number + " position " + currentPosition + " direction " + direction);
    
    SoftphoneMessage ssm1 = new SoftphoneMessage(true);
    SoftphoneMessage ssm2 = new SoftphoneMessage(number, currentPosition, direction, true);
    
    try {
      client.send(ssm1.getBytes());
      client.send(ssm2.getBytes());
    } catch (IOException ex) {
      Logger.getLogger(ClientConnect.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalStateException ex) {
      Logger.getLogger(ClientConnect.class.getName()).log(Level.SEVERE, null, ex);
    }
      
    return 1;
  }
  /* 
   * end DWR response methods 
   * ************************
   */

  /** Main method to test the class.
   * Use the main method to test this class.
   * @param args
   */
  public static void main(String args[]) {
//    System.out.println("In main ...");
    ClientConnect client = new ClientConnect("antonio", "", "localhost", "1139");
    client.connect();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
//      System.out.println("Interrupt Exception thrown!" + ex);
    }
    client.disconnect();
  }

  /** Class to run the thread for communication.
   * This thread will maintain the open communication channel.
   */
  class ClientConnectUp extends Thread {

    private boolean quit = false;
    private final int RUN_SLEEP = 100;
    /**
     * Default Constructor.
     */
    public ClientConnectUp() {
    }

    /**Run method.
     * This will run until killed. This will parse the messages that get sent to this servlet, 
     * and then relay the information back to the browser via DWR calls.
     */
    @Override
    public void run() {
      /*
       * counter for checking if connection dies off, I'm going to give it 1.5 minutes
       * to make a connection, then test to see if it fails, then drop the connection
       * to wonderland
       */
      int count=0;
      int COUNT_CHECK = (int)10000/RUN_SLEEP;
      
//      System.out.println("Inside run ...");

      try {
        //connect to the wonderland server, and stay alive to process messages
        Thread runner = (new Thread() {
          @Override
          public void run() {
            connectToServer();
          }
        });
        runner.start();

        ScriptSession scriptSession;
        MessageData msgData = null;
        
        while (!quit) {
          
          while( !msgRxQueue.isEmpty() && initialWait) {
            ScriptBuffer script = new ScriptBuffer();
            //get first message
            msgData = (MessageData)msgRxQueue.remove();
            
            if( msgData.getType().equals(MessageData.MsgType.ServerStatus) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> ServerStatus");

                script.appendScript("cbServerStats(")
                      .appendData(msgData.getServerVer())
                      .appendScript(",")
                      .appendData(msgData.getProtocolVer())
                      .appendScript(",")
                      .appendData("-1")
                      .appendScript(");");

                scriptSession = wc.getScriptSession();
                scriptSession.addScript(script);
            } else if( msgData.getType().equals(MessageData.MsgType.ChatMessage) ) {
              logger.log(Level.INFO,"DWR: ReverseAjax -> ChatMessage");
              logger.log(Level.INFO,"User: " + msgData.getUserName());
              logger.log(Level.INFO,"Message: " + msgData.getChatMessage());

              script.appendScript("cbChatUpdate(")
                    .appendData(msgData.getUserName())
                    .appendScript(",")
                    .appendData(msgData.getChatMessage())
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);
            } else if( msgData.getType().equals(MessageData.MsgType.LoginMessage) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> LoginMessage");

              script.appendScript("cbChatUpdate(")
                    .appendData("SERVER")
                    .appendScript(",")
                    .appendData(" logged in")
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);
              
            } else if( msgData.getType().equals(MessageData.MsgType.UserChange) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> UserChange");

              script.appendScript("cbUsersOnline(")
                    .appendData(usersOnline())
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);

            } else if( msgData.getType().equals(MessageData.MsgType.NewAvatar) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> Avatar created for " + msgData.getUserName());

              script.appendScript("cbCreateUserLocation(")
                    .appendData(msgData.getUserName())
                    .appendScript(",")
                    .appendData("0")
                    .appendScript(",")
                    .appendData("0")
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);
            } else if( msgData.getType().equals(MessageData.MsgType.AvatarMove) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> Avatar move for " + msgData.getUserName());
              
              script.appendScript("cbTrackUser(")
                    .appendData(msgData.getUserName())
                    .appendScript(",")
                    .appendData(msgData.getAvatarMoveX())
                    .appendScript(",")
                    .appendData(msgData.getAvatarMoveY())
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);
            } else if( msgData.getType().equals(MessageData.MsgType.Error) ) {
              //logger.log(Level.INFO,"DWR: ReverseAjax -> Error message " + msgData.getErrorMessage());

              script.appendScript("dsClient.cbErrorMessage(")
                    .appendData(msgData.getErrorMessage())
                    .appendScript(");");

              scriptSession = wc.getScriptSession();
              scriptSession.addScript(script);
            }
          }
          
          sleep(RUN_SLEEP);
          
          if( ++count > COUNT_CHECK ) {
            initialWait = true;
          }
        }
//        logger.log(Level.INFO,"... clientconnect thread dieing ...");
      } catch (Exception e) {
//        logger.log(Level.SEVERE,"ClientConnect: Exception -> run");
        e.printStackTrace();
      }
    }

    /** Connect to the server method.
     */
    public void connectToServer() {
//      logger.log(Level.INFO,"In connectToServer ...");
      if (client.isConnected()) {
//        logger.log(Level.INFO,"client already connected ... disconnecting");
        /*
         * TODO: Need to create a method that will notify the browser to end and display that
         * the user is already logged in, and ask them to login as a different user
         */ 
        disconnectFromServer();
      }

      try {
        /*
         * Acquire the client connection semphore to ensure the previous
         * client disconnected
         */ 
        clientConnection.acquire();

      } catch (InterruptedException e) {
//        logger.log(Level.SEVERE,"ClientConnect: Exception -> connectToServer");
        e.printStackTrace();
      }

      ServerInfo serverInfo = new ServerInfo();

      serverInfo.setServerName(serverName);
      serverInfo.setPort(portNum);
      serverInfo.setUsername(userName);
      serverInfo.setPassword(password.toCharArray());


      try {
        Properties connectProperties = new Properties();
        connectProperties.setProperty("host", serverInfo.getServerName());
        connectProperties.setProperty("port", serverInfo.getPort());
        client.login(connectProperties);
      } catch (IOException e) {
//        logger.log(Level.SEVERE,"EXCEPTION: releasing connection -> " + e);
        clientConnection.release();
      } catch (UnresolvedAddressException e) {
//        logger.log(Level.SEVERE,"EXCEPTION: releasing connection -> " + e);
        clientConnection.release();
      } catch (Exception e) {
//        logger.log(Level.SEVERE,"EXCEPTION: -> " + e);
        clientConnection.release();
      }
      
    }// connectToServer
    
    public synchronized void quit() {
      this.quit = true;
    }
  }// end ClientConnectUp

  /** Client listener
   *
   */
  class ClientListener implements SimpleClientListener {

    /** Create password authentication.
     * @return PasswordAuthentication
     */
    public PasswordAuthentication getPasswordAuthentication() {
      ServerInfo server = new ServerInfo(serverName, portNum);
      server.setUsername(userName);
      server.setPassword(password.toCharArray());
      return new PasswordAuthentication(server.getUsername(), server.getPassword());
    }

    /** Method that is called when a channel is joined
     *
     * @param arg0 ClientChannel
     * @return ManagerClientChannel
     */
    public ClientChannelListener joinedChannel(ClientChannel channel) {
      //logger.log(Level.INFO, "Client Channel Listenter -> Joined channel -> " + channel.getName());
      
      if(channel.getName().equals(userName + ChannelInfo.AVATAR_CELL)) {
//        logger.info("Avatar cell channel connected");
        avatarCellChannel = channel;
      } else if (channel.getName().equals(userName + ChannelInfo.AVATAR_P2P)) {
//        logger.info("Avatar P2P channel connected");
        avatarP2PChannel = channel;
      } else {
        managerChannel = new ManagerClientChannel(channel);  
      }

      return managerChannel;
    }

    /** Received message method called by wonderland.
     *
     * @param data a byte[] of message received
     */
    public void receivedMessage(byte[] data) {
      //logger.log(Level.INFO,"ClientListener::receivedMessage");
      Message msg = Message.extractMessage(data);
      MessageData newMsgRx = new MessageData(MessageData.MsgType.ServerStats);
      
      if (msg instanceof ProtocolVersion) {
        //logger.log(Level.INFO,"Checking protocol versions");
        //logger.log(Level.INFO,"Server version " + ((ProtocolVersion) msg).toString());
        //logger.log(Level.INFO,"Local version " + ProtocolVersion.getLocalVersion().toString());
        
        // save the server version, protocol version
        newMsgRx.setServerStats(((ProtocolVersion) msg).toString(),ProtocolVersion.getLocalVersion().toString());

        msgRxQueue.add(newMsgRx);
        if (!ProtocolVersion.getLocalVersion().compatibleWithServerVersion(((ProtocolVersion) msg))) {
          logger.log(Level.SEVERE, "Protocol Version mismatch");
          System.exit(1);
        }
        
      } else if (msg instanceof ErrorMessage) {
        ErrorMessage m = (ErrorMessage) msg;
        errorMessage = m.getErrorMessage();
        logger.log(Level.SEVERE,"Error Message: " + m.getErrorMessage());
        //send message back to the user
        MessageData newMsgRxErr = new MessageData(MessageData.MsgType.Error);
        newMsgRxErr.setErrorMessage(errorMessage);
        msgRxQueue.add(newMsgRxErr);
        disconnectFromServer();
      } else if (msg instanceof LoginMessage) {
        processLoginMessage((LoginMessage) msg);
      } else if (msg instanceof PingMessage) {
        processPingMessage((PingMessage)msg);
      } else if (msg instanceof AvatarP2PMessage) {
        processAvatarP2PMessage((AvatarP2PMessage)msg);
      } else if (msg instanceof AlsSupportedMessage) {
        processAlsSupportedMessage((AlsSupportedMessage)msg);
      } else if (msg instanceof CellHierarchyMessage) {
        processCellHierarchyMessage((CellHierarchyMessage)msg);
      } else if (msg instanceof SoftphoneMessage) {
        processSoftphoneMessage((SoftphoneMessage)msg);
      } else {
//        logger.log(Level.INFO, "ClientListener::receivedMessage -> UnknownMsgRx -> " + msg);
      }
    }

    /**Process the LoginMessage.
     * processes the login message
     * @param msg
     */
    public void processLoginMessage(LoginMessage msg) {
      Message setup;
      Vector3f initialPosition = null;

      if (System.getProperty("lg.initialPosition") != null) {
        String coords[] = System.getProperty("lg.initialPosition").split(":");
        if (coords.length != 3) {
          System.err.println("Usage: Wonderland -goto x:y:z");
          System.exit(-1);
        }

        //FIX: This will have to change once the positions placemarks begin to work 
        //hardcoded, since for now I am just implementing the initial phase
        initialPosition = new Vector3f(new Float(50.0), new Float(0.7), new Float(50.0));
        currentPosition = new Point3f(50,0.7f,50);
      }

      // set the base URL in the channel controller
      if (msg.getBaseURL() != null) {
        WonderlandConfig.setBaseURL(msg.getBaseURL());
      }
      
      // initialize configurations
      AvatarInfo avatar = AvatarConfigurator.getDefaultAvatarInfo(msg.getBaseURL());
      setup = AvatarSetupMessage.createLoggedInMessage(
              avatar,
              AvatarClientConfig.getDefault().getUserColor(),
              initialPosition,
              ServerMasterClient.iAmTheServerMasterClient());

          
      // send our setup message
      try {
        client.send(setup.getBytes());
        //logger.log(Level.INFO," avatar setup sent");
      } catch (IOException ioe) {
        System.err.println("Unable to send setup message: " + ioe);
        ioe.printStackTrace();
      }
    }

    /**Process AvatarP2PMessage.
     * 
     * @param msg
     */
    private void processAvatarP2PMessage(AvatarP2PMessage msg) { 
      logger.log(Level.INFO,"ClientConnect::processAvatarP2PMessage -> " + msg.getActionType());
      MessageData newMsgRx;
      byte [] tmp = null;
      
      switch (msg.getActionType()) {
        case SETUP:
          //logger.log(Level.INFO,"ClientConnect::processAvatarP2PMessage -> SETUP");
           
          break;
        case MOVE:
          //logger.log(Level.INFO,"MOVE by " + userNameMap.get(tmp[0]).toString() + ": " + msg.getPosition(null) );

          Point3f move = msg.getPosition(null);

          newMsgRx = new MessageData(MessageData.MsgType.AvatarMove);
          newMsgRx.setUserName(userName);
          newMsgRx.setAvatarMove(move.x, move.z);
          msgRxQueue.add(newMsgRx);

        break;
        default:
          logger.log(Level.WARNING, "ClientListener -> processAvatarP2PMessage -> Unknown message: " + msg.getActionType() );
      }
    }

    /**Process PingMessage.
     * 
     * @param msg
     */
    private void processPingMessage(PingMessage msg) {
      logger.log(Level.INFO,"ClientListener::processPingMessage -");
      try {
        // echo back to client
        client.send(msg.getBytes());
      } catch (IOException ex) {
        logger.log(Level.SEVERE, "IOException: Ping message", ex);
      }
    }

    private void processAlsSupportedMessage(AlsSupportedMessage msg) {
      //Not Implemented - Antonio 
      
    }
    
    private void processCellHierarchyMessage(CellHierarchyMessage msg) {
      //dropping cell hierarchy messages for now
      
    }
    
    private void processSoftphoneMessage(SoftphoneMessage msg) {
     
    }
    
    /**Disconnect the Wonderland connection.
     * 
     * @param arg0
     * @param arg1
     */
    public void disconnected(boolean arg0, String arg1) {
      logger.log(Level.INFO, "...Disconnected: " + arg1);
      
      connection.quit();
      
      if (errorMessage != null) {
        clientConnection.release();
        return;
      }
      
      clientConnection.release();

      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
//        System.out.println("ClientListener: Exception -> disconnected");
      }
    }

    /**Not implemented
     * 
     */
    public void reconnecting() {
      logger.log(Level.INFO, "ClientListener::reconnecting ... ");
    }
    
    /**Not implemented
     * 
     */
    public void reconnected() {
      logger.log(Level.INFO, "ClientListener::reconnected ... ");
    }

    /**Not implemented
     * 
     */
    public void loggedIn() {
      logger.log(Level.INFO, "ClientListener::loggedIn ... ");
    }

    /**Not implemented
     * 
     */
    public void loginFailed(String arg0) {
      logger.log(Level.INFO, "ClientListener::loginFailed ... ");
    }
  }

  /**
   * 
   */
  class ManagerClientChannel implements ClientChannelListener {

    private ClientChannel chan;
    private AvatarCell avatarCell;
    private Avatar avatar;
    
    /**Constructor
     * 
     * @param chan
     */
    public ManagerClientChannel(ClientChannel chan) {
      //logger.log(Level.INFO,"Manager Client channel ...");
      this.chan = chan;
    }

    /**Method that get the recieved message from Wonderland
     * 
     * @param arg0
     * @param arg1
     * @param data
     */
    @SuppressWarnings("fallthrough")
    public void receivedMessage(ClientChannel arg0, SessionId arg1, byte[] data) {
      //logger.log(Level.INFO,"UserClientChannel::receivedMessage");
      Message msg = Message.extractMessage(data);
      //logger.log(Level.INFO,"UserClientChannel -> " + arg0.getName() + " : " + msg);
      
      if (msg instanceof ServerManagerMessage) {
        processServerManagerMessage(arg0, arg1, (ServerManagerMessage) msg);
      } else if (msg instanceof VoiceManagerTuningMessage) {
        processVoiceManagerTuningMessage(arg0, arg1, (VoiceManagerTuningMessage) msg);
      } else if (msg instanceof AvatarCellMessage) {
        processAvatarCellMessage((AvatarCellMessage) msg, arg1, arg0);
      } else if (msg instanceof CellHierarchyMessage) {
        processCellHierarchyMessage((CellHierarchyMessage) msg);
      } else if (msg instanceof AvatarP2PMessage) {
        processAvatarP2PMessage((AvatarP2PMessage)msg, arg1);
      } else if (msg instanceof UserChangedMessage) {
        processUserChangedMessage((UserChangedMessage)msg, arg1);
      } else if (msg instanceof AlsSupportedMessage) {
        processAlsSupportedMessage((AlsSupportedMessage)msg);
      } else {
        logger.log(Level.INFO, "UserClientChannel UnknownMsgRx: " + msg);
      }
    }
    /**Process AvatarP2PMessage.
     * 
     * @param msg
     * @param sessionId
     */
    private void processAvatarP2PMessage(AvatarP2PMessage msg, SessionId sessionId) {
      //logger.log(Level.INFO,"ManagerClient::processAvatarP2PMessage -> " + msg.getActionType());
      MessageData newMsgRx;
      byte [] tmp = null;
      
      if( sessionId != null )
        tmp = sessionId.toBytes();
      
      switch (msg.getActionType()) {
        case SETUP:
          logger.log(Level.INFO,"UserClientChannel::processAvatarP2PMessage -> SETUP");
          
           
          break;
        case MOVE:
//          logger.log(Level.INFO,"ManagerClient::processAvatarP2PMessage -> MOVE");
//          logger.log(Level.INFO,"MOVE by " + userNameMap.get(tmp[0]).toString() + ": " + msg.getPosition(null) );
//          logger.log(Level.INFO,"MOVE by " + tmp[0] + ": " + msg.getPosition(null) );
          
          newMsgRx = new MessageData(MessageData.MsgType.AvatarMove);
          newMsgRx.setUserName((String)userNameMap.get(tmp[0]));
          newMsgRx.setAvatarMove(msg.getPosition(null).x, msg.getPosition(null).z);
          msgRxQueue.add(newMsgRx);

          break;
//        case GESTURE:
//                      
////          if (avatar != null) {
////          avatar.remoteUserGestured(Avatar.Gesture.values()[msg.getGesture()], 
////          null);
////          }
//          
//          break;
//        case GESTURE_POINT:
////                if (avatar != null) {
////                    avatar.remoteUserGestured(Avatar.Gesture.POINT,
////                                              msg.getGesturePoint());
////                }
//          break;
        case CHAT:
           newMsgRx = new MessageData(MessageData.MsgType.ChatMessage);
//          System.out.println("*** ID: " + sessionId.toBytes());
//          System.out.println("*** ID: " + sessionId.toBytes().toString());
//          System.out.println("*** ID: " + userNameMap.get(sessionId.toBytes()) + " userID: " + msg.getUserID());
//      
//          logger.log(Level.INFO,"UserClientChannel::processAvatarP2PMessage -> CHAT -> " + msg.getChatMessage());
//          logger.log(Level.INFO,"UserClientChannel::processAvatarP2PMessage -> CHAT -> userID -> " + (String)userNameMap.get(msg.getUserID()));
//          logger.log(Level.INFO,"UserClientChannel::processAvatarP2PMessage -> CHAT -> userID -> " + (String)userNameMap.get(sessionId.toBytes()));
//          
          
//          for(int i=0; i < sessionId.toBytes().length; i++ ) {
//            System.out.println("sessionId[ " + i + "] = " + tmp[i]);
//          } 
          
          //dwrChatMessage[0] = (String)userNameMap.get(sessionId.toBytes());
          
          newMsgRx.setUserName((String)userNameMap.get(tmp[0]));
          newMsgRx.setChatMessage(msg.getChatMessage());
          msgRxQueue.add(newMsgRx);
          
          //dwrChatMessage[0] = (String)userNameMap.get(tmp[0]);
          //dwrChatMessage[1] = msg.getChatMessage();
          break;
        case SPEAKING:
//                if(logger.isLoggable(Level.FINEST)) {
//                    logger.log(Level.FINEST, "{0} received {1}", new Object[]{this, msg});
//                }
//                if (avatar!=null) {
//                    avatar.setSpeakingStatus(msg.getSpeakingStatus());
//                } else {
//                    logger.log(Level.WARNING, 
//                        "Listener {0} has a null avatar while listening to {1}", 
//                        new Object[]{this, clientChannel});
//                }
          break;
        case CLIENT_JOIN:
          //logger.log(Level.INFO,"UserClientChannel::processAvatarP2PMessage -> CLIENT_JOIN -> " + ChannelController.getController().getUser(SessionId.fromBytes(msg.getUserID())) );
          
          // A new client has joined our p2p channel, send it our current location
//          System.out.println("****** CLIENT_JOIN sending our position to " + ChannelController.getController().getUser(SessionId.fromBytes(msg.getUserID())) + "  null avatar=" + avatar) ;
//          if (avatar!=null) {
//              avatar.clientJoined(SessionId.fromBytes(msg.getUserID()));
//          } else {
//              logger.severe("CLIENT_JOIN but avatar is null");
//          }
          break;
//        case CAMERA_MODE:
//          // Update the AvatarNameTag of this class' Avatar
////                avatar.showCameraModeChange(msg.getCameraMode());
//          break;
        case WHISPER:
          //Update the avatar's whispering status
//          avatar.setWhisperingStatus(msg.getIsWhispering());
          break;
//        case SHOW:
//          //Update the cell's visibility
////                RecorderCell rCell = (RecorderCell) avatarCell;
////                rCell.showAvatar(msg.getIsVisible());
//          break;
        default:
          logger.log(Level.WARNING, "UserClientChannel -> Unknown Message: " + msg.getActionType() );
      }
    }
    
    /**Process AvatarCellMessage.
     * 
     * @param msg
     * @param channel
     */
    private void processAvatarCellMessage(AvatarCellMessage msg, SessionId sessionId, ClientChannel chan) {
      //dropping avatar cell messages for now
      //logger.log(Level.INFO,"UserClientChannel::AvatarCellMessage -> " + msg.getActionType());
      
      MessageData newMsgRx;
      byte [] tmp = null;
      
      if( sessionId != null )
        tmp = sessionId.toBytes();
      
      switch (msg.getActionType()) {
        case SETUP:
          //logger.log(Level.INFO,"UserClientChannel::processAvatarCellMessage -> SETUP");
          
          try {
            chan.send(new AvatarCellMessage(AvatarCellMessage.ActionType.JOIN_P2P).getBytes());
            } catch (IOException ex) {
            Logger.getLogger(ClientConnect.class.getName()).log(Level.SEVERE, null, ex);
          }
          break;
        case CELL_MOVE:
//          logger.log(Level.INFO,"UserClientChannel::processAvatarCellMessage -> CELL_MOVE ");
//          logger.log(Level.INFO,"CELL_MOVE: " + msg.getPosition(null));
//          logger.log(Level.INFO,"user: " + (String)userNameMap.get(tmp[0]));
          
          newMsgRx = new MessageData(MessageData.MsgType.AvatarMove);
          newMsgRx.setUserName((String)userNameMap.get(tmp[0]));
          newMsgRx.setAvatarMove(msg.getPosition(null).getX(), msg.getPosition(null).getZ());
          msgRxQueue.add(newMsgRx);
          break;
        default: break;
      }
    }
    
    /**Process CellHierarchyMessage.
     * 
     * @param msg
     */
    private void processCellHierarchyMessage(CellHierarchyMessage msg) {
      //dropping cell hierarchy messages for now
      //logger.log(Level.INFO,"UserClientChannel::CellHierarchyMessage -> " + msg);
      //logger.log(Level.INFO,"CellID: " + msg.getCellID());
      //cellId = msg.getCellID();
      CellSetup setup = msg.getSetupData();

      if (setup instanceof AvatarCellSetup) {
        //System.out.println("msg.getUserName : " + ((AvatarCellSetup) setup).getUserName());
        
        String userId = ((AvatarCellSetup) setup).getUserName();        
        if (userId.equals(userName)) {
          //logger.info("Found user's cell: " + msg.getCellID());
          cellId = msg.getCellID();
        }
      }
    }
    
    /**Process UserChangedMessage.
     * 
     * @param msg
     */
    private void processUserChangedMessage(UserChangedMessage msg, SessionId sessionId) {
      MessageData newMsgRx = new MessageData(MessageData.MsgType.UserChange);     
      //logger.log(Level.INFO,"UserClientChannel::processUserChangedMessage -> " + msg.getUserName());
      
      if( msg.getUserName().compareToIgnoreCase("ServerMasterClient") == 0 ) {
        return;
      }

      byte [] temp = msg.getUserID();

      // if user in list, delete them ( they just logged off )
      if( usersOnline.contains((String)msg.getUserName()) ) {
        usersOnline.remove((String)msg.getUserName());
  
        //remove the user from the mappings
        //userNameMap.remove(msg.getUserID());
        userNameMap.remove(temp[0]);
        userIdMap.remove(msg.getUserName());
        
        newMsgRx.setUserName(msg.getUserName());
        msgRxQueue.add(newMsgRx);
        
        MessageData newMsgRx1 = new MessageData(MessageData.MsgType.ChatMessage);     
        newMsgRx1.setUserName("SERVER");
        newMsgRx1.setChatMessage(msg.getUserName() + " has logged off");
        msgRxQueue.add(newMsgRx1);
        
      } else {
        //add the user that joined the wonderland server
        usersOnline.add(msg.getUserName());
        
        //save the userID and the userName to a mapping
        //userNameMap.put(msg.getUserID(), msg.getUserName());
        userNameMap.put(temp[0], msg.getUserName());
        //userIdMap.put(msg.getUserName(), msg.getUserID());
        userIdMap.put(msg.getUserName(), temp[0]);
        
        newMsgRx.setUserName(msg.getUserName());
        msgRxQueue.add(newMsgRx);
        
        MessageData newMsgRx1 = new MessageData(MessageData.MsgType.ChatMessage);     
        newMsgRx1.setUserName("SERVER");
        newMsgRx1.setChatMessage(msg.getUserName() + " is online");
        msgRxQueue.add(newMsgRx1);
        
        MessageData newMsgRx2 = new MessageData(MessageData.MsgType.NewAvatar);     
        newMsgRx2.setUserName(msg.getUserName());
        msgRxQueue.add(newMsgRx2);
      }
//      logger.log(Level.INFO,"UserClientChannel::processUserChangedMessage -> userID -> " + msg.getUserID());
//      logger.log(Level.INFO,"UserClientChannel::processUserChangedMessage -> userName -> " + msg.getUserName());
//      
//      System.out.println("*** get Name: " + userNameMap.get(temp[0]));
//      System.out.println("*** Size: " + userNameMap.size());
//      
    }
    
    /**Process ServerManagerMessage.
     * 
     * @param arg0
     * @param arg1
     * @param msg
     */
    private void processServerManagerMessage(ClientChannel arg0, SessionId arg1, ServerManagerMessage msg) {
      switch (msg.getActionType()) {
        case FULL_STATUS:
          int userLimit = msg.getUserLimit();
          String serverName = msg.getServerName();

        // No break here, fall through to STATUS case
        case STATUS:
          float t = msg.getUpTime() / 1000.0f / 60.0f / 60.0f;
          String hrs = Float.toString(t).substring(0, 4);

          break;
      }
    }

    /**Not Implemented.
     * 
     * @param msg
     */
    private void processAlsSupportedMessage(AlsSupportedMessage msg) {

    }
       
    /**Not Implemented.
     * 
     * @param msg
     */
    private void processVoiceManagerTuningMessage(ClientChannel arg0, SessionId arg1, VoiceManagerTuningMessage msg) {
    }
    
    /**Not Implemented.
     * 
     * @param msg
     */
    public void leftChannel(ClientChannel arg0) {
    }

    /** Send data on this channel
     * 
     * @param msg
     * @throws java.io.IOException
     * @throws java.lang.IllegalStateException
     */
    public void send(byte[] msg) throws IOException, IllegalStateException {
      chan.send(msg);
    }
  }// class ManagerClientChannel
   
}//class ClientConnect
    
