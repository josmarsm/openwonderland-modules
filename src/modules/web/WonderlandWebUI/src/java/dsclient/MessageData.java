/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dsclient;

/**
 *
 * @author antonio
 */
public class MessageData {
  public static enum MsgType {
    AvatarMove,
    ServerStatus,
    ChatMessage,
    LoginMessage,
    UserChange,
    NewAvatar,
    ServerStats, 
    Error
  };      
  
  private MsgType msgType;
  
  private String username; 
  private String chatMessage; 
  private float moveX;
  private float moveY;
  private String protocolVersion; 
  private String serverVersion; 
  private String errorMessage; 
  
  public MessageData(MsgType type) {
    msgType = type;
  }
  
  public MsgType getType() {
    return msgType;
  }

  public void setUserName(String in) {
    username = in;
  }

  public String getUserName() {
    return username;
  }

  //
  // Chat Message 
  //
  public void setChatMessage(String in) {
    chatMessage = in;
  }
  
  public String getChatMessage() {
    return chatMessage;
  }

  //
  // Avatar Move 
  //
  public void setAvatarMove(float x, float y) {
    moveX = x;
    moveY = y;
  }
  
  public float getAvatarMoveX() {
    return moveX;
  }
  
  public float getAvatarMoveY() {
    return moveY;
  }
  
  //
  // Server information 
  //
  
  /**
   * Server 
   * @param serverVer
   * @param protoVer 
   */
  public void setServerStats(String serverVer, String protoVer) {
    serverVersion = serverVer;
    protocolVersion = protoVer;
  }
  
  public String getServerVer() {
    return serverVersion;
  }
  
  public String getProtocolVer() {
    return protocolVersion;
  }  
  
  //
  // Error messages
  //
  
  public void setErrorMessage(String in) {
    errorMessage = in;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  
}
  