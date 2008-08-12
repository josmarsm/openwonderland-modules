/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dsclient;



import java.util.logging.Logger;
import java.util.logging.Level;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

/**
 *
 * @author antonio
 */
@RemoteProxy
public class DSClientServlet implements HttpSessionListener{
  private Logger logger = Logger.getLogger(this.getClass().getName());
  private String sessionId = null;
  
  /**Constructor
   * 
   */
  public DSClientServlet() {
  
  }
  
    
  /**Called to create a connection, and set the Comet reverse ajax method.
   * 
   * @param username
   * @param password
   * @param servername
   * @param portnum
   * @param inRequest
   */
  @RemoteMethod
  public void startDSClient(String username, String password, 
                            String servername, String portnum,
                            final HttpServletRequest inRequest) {
    try {

      WebContext wc = WebContextFactory.get();

      ClientConnect dsclient = new ClientConnect(username,password,servername,portnum);
      HttpSession session = inRequest.getSession(true);
      
      sessionId = new String(session.getId());
      
      session.setAttribute("clientconnect", dsclient);
      session.setAttribute("webcontext", wc);
      
      System.out.println("dsclient: " + dsclient);
      System.out.println("webcontext: " + wc);
      
      System.out.println("id created: " + session.getId());
      System.out.println("id passed in: " + sessionId);
      
      /*
       * set the max timeout to be 2 minutes, DWR will always have connetion open, so there
       * is no need to send a 'pulse' to the servlet
       */
      //session.setMaxInactiveInterval(10);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 
  
  /** method used to set the web context for the http session.
   * This must be set for each page that the client moves to.
   * @param inSession
   */
  @RemoteMethod
  public void setWebContext(final HttpSession inSession) {
    WebContext wc = WebContextFactory.get();  
    inSession.setAttribute("webcontext", wc);
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
  }
    
  /** Connect the webclient to the wonderland server
   * 
   * @param inSession
   * @return
   */
  @RemoteMethod
  public int connect( final HttpSession inSession) {
    
    logger.log(Level.INFO,"... connecting ...");
    logger.log(Level.INFO,inSession.getId());
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Logger.getLogger(DSClientServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
    //if( inSession.getId().equals(sessionId) ) {
      System.out.println("inSession: " + inSession);
      System.out.println("clientconnect: " + inSession.getAttribute("clientconnect"));
      ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
      ((ClientConnect)inSession.getAttribute("clientconnect")).connect();
//    } else {
//      System.out.println("different session ids");
//      System.out.println("stored id: " + sessionId);
//      System.out.println("passed in id: " + inSession.getId());
//      //inSession.invalidate();
//      
//    }
    
    //upon the first connection the servlet will build the images to use for the map moving
    
    return 1;
  }

  /** Browser to disconnect the web session from the wonderland server
   * 
   * @param inSession
   */
  @RemoteMethod
  public void disconnect( final HttpSession inSession) {
    //logger.log(Level.INFO,"... disconnecting ...");
    ((ClientConnect)inSession.getAttribute("clientconnect")).disconnect();
  }
  
  /** Send a chat message
   * 
   * @param msg
   * @param inSession
   */
  @RemoteMethod
  public void sendChat(String msg, final HttpSession inSession) {
    logger.log(Level.INFO,"... sending chat ...");
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    ((ClientConnect)inSession.getAttribute("clientconnect")).sendChat(msg);
    logger.log(Level.INFO,"... chat sent ...");
  }

  /**
   * 
   * @param inSession
   * @return
   */
  @RemoteMethod
  public String[] serverStats(final HttpSession inSession) {
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    return ((ClientConnect)inSession.getAttribute("clientconnect")).serverStats();
  }
  
  /**
   * 
   * @param inSession
   * @return
   */
  @RemoteMethod
  public String[] usersOnline(final HttpSession inSession) {
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    return ((ClientConnect)inSession.getAttribute("clientconnect")).usersOnline();
  }
    
  /**
   * 
   * @param username
   * @param inSession
   * @return
   */
  @RemoteMethod
  public int goToUser(String username, final HttpSession inSession) {
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    return ((ClientConnect)inSession.getAttribute("clientconnect")).goToUser(username);
  }
  
  @RemoteMethod
  public int goToLocation(float posX, float posY, float posZ, final HttpSession inSession) {
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    return ((ClientConnect)inSession.getAttribute("clientconnect")).goToLocation(posX,posY,posZ);
  }
  
  @RemoteMethod
  public int sendCallMe(String number, final HttpSession inSession) {
    ((ClientConnect)inSession.getAttribute("clientconnect")).setWebContext(((WebContext)inSession.getAttribute("webcontext")));
    return ((ClientConnect)inSession.getAttribute("clientconnect")).sendCallMe(number);
  }
 
  public void sessionCreated(HttpSessionEvent arg0) {
    System.out.println("new web session was created with ID: " + arg0.getSession().getId());
    
  }

  public void sessionDestroyed(HttpSessionEvent arg0) {
    System.out.println("*********************web session was destoryed *** calling disconnect ***************************");
    System.out.println("session with ID: " + arg0.getSession().getId());

    if( arg0 != null ) {
      if( arg0.getSession() != null ) {
        if(arg0.getSession().getAttribute("clientconnect") != null ) {
          ((ClientConnect)arg0.getSession().getAttribute("clientconnect")).disconnect();
        }
      }
    }
    arg0.getSession().invalidate();    
  }
}
