/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package uk.ac.essex.wonderland.modules.webcamcontrol.web.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSessionImpl;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionCreator;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarRunner;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLogin.DarkstarServerListener;
import org.jdesktop.wonderland.modules.darkstar.api.weblib.DarkstarWebLoginFactory;
import uk.ac.essex.wonderland.modules.webcamcontrol.web.WebcamConnection;
import uk.ac.essex.wonderland.modules.webcamcontrol.common.WebcamRecord;

/**
 *
 * @author Bernard Horan
 */
public class WebcamServlet extends HttpServlet implements ServletContextListener, DarkstarServerListener {

    private static final Logger logger = Logger.getLogger(WebcamServlet.class.getName());
    private AdminRegistration ar = null;
    private ServletContext context = null;

    /** the key to identify the connection in the servlet context */
    public static final String WEBCAM_CONTROL_CONN_ATTR = "__webcamcontrolConfigConnection";

    /** the key to identify the darkstar session in the servlet context */
    public static final String SESSION_ATTR = "__webcamcontrolConfigSession";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        Collection<WebcamRecord> cellCollection = new HashSet<WebcamRecord>();
        //Get the connection
        WebcamConnection conn = (WebcamConnection) getServletContext().getAttribute(WEBCAM_CONTROL_CONN_ATTR);


        
        try {
            //logger.warning("getting records");
            cellCollection.addAll(conn.getWebcamRecords());
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            logger.severe("runtime exception: " + ex.getMessage());
        }
        


        // See if the request comes with an "action" (e.g. Delete). If so,
        // handle it and fall through to below to re-load the page
        try {
            String action = request.getParameter("action");
            //logger.warning("ACTION: " + action);
            if (action != null && action.equalsIgnoreCase("edit") == true) {
                handleEdit(request, response, cellCollection);
            }
            else if (action != null && action.equalsIgnoreCase("change") == true) {
                handleChange(request, response, cellCollection);
            }
            else {
                // Otherwise, display the items
                handleBrowse(request, response, cellCollection);
            }
        } catch (java.lang.Exception cre) {
            throw new ServletException(cre);
        }
    }

    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         String message)
        throws ServletException, IOException
    {
        request.setAttribute("message", message);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/error.jsp");
        rd.forward(request, response);
    }

    /**
     * Handles the default "browse" action to display the webcam records.
     */
    private void handleBrowse(HttpServletRequest request,
            HttpServletResponse response, Collection<WebcamRecord> c)
            throws ServletException, IOException,
            JAXBException
    {
        
        request.setAttribute("records", c);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/browse.jsp");
        rd.forward(request, response);
    }

    /**
     * Handles the default "edit" action to edit a webcam record.
     */
    private void handleEdit(HttpServletRequest request,
            HttpServletResponse response, Collection<WebcamRecord> c)
            throws ServletException, IOException,
            JAXBException
    {

        String cellID = request.getParameter("cellID");
        int id = Integer.valueOf(cellID);
        WebcamRecord record = getRecord(c, id);
        if (record == null) {
            throw new RuntimeException("Can't find webcam record for cellID: " + id);
        }
        request.setAttribute("record", record);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/edit.jsp");
        rd.forward(request, response);
    }

    private void handleChange(HttpServletRequest request,
            HttpServletResponse response, Collection<WebcamRecord> c)
        throws ServletException, IOException
    {
        String cellID = request.getParameter("cellID");
        //logger.warning("cellID: " + cellID);
        int id = Integer.parseInt(cellID);
        WebcamRecord record = getRecord(c, id);
        String cameraURI = request.getParameter("cameraURI");
        //logger.warning("cameraURI: " + cameraURI);
        if (!record.getCameraURI().equalsIgnoreCase(cameraURI)) {
            record.setCameraURI(cameraURI);
        }
        String cameraUsername = request.getParameter("cameraUsername");
        //logger.warning("cameraUsername: " + cameraUsername);
        if (!cameraUsername.equals(record.getCameraUsername())) {
            record.setCameraUsername(cameraUsername);
        }
        String cameraPassword = request.getParameter("cameraPassword");
        //logger.warning("cameraPassword: " + cameraPassword);
        if (!cameraPassword.equals(record.getCameraPassword())) {
            record.setCameraPassword(cameraPassword);
        }
        String cameraState = request.getParameter("cameraState");
        //logger.warning("cameraState: " + cameraState);
        if (!record.getCameraState().equals(cameraState)) {
            record.setCameraState(cameraState);
        }
        Object obj = getServletContext().getAttribute(WEBCAM_CONTROL_CONN_ATTR);
        if (obj != null) {
            WebcamConnection connection = (WebcamConnection)obj;
            connection.changeSettings(id, record);
        }

        // After we have changed the entry, then redisplay the listings
        try {
            handleBrowse(request, response, c);
        } catch (java.lang.Exception cre) {
            throw new ServletException(cre);
        }
    }

    private WebcamRecord getRecord(Collection<WebcamRecord> collection, int cellID) {
        for (WebcamRecord record : collection) {
            if (record.getCellID() == cellID) {
                return record;
            }
        }
        return null;
    }

    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void contextInitialized(ServletContextEvent sce) {
        // register with the admininstration page
        context = sce.getServletContext();
        ar = new AdminRegistration("Manage Webcam Viewers",
                                   "/webcamcontrol/webcamcontrol/browse");
        ar.setFilter(AdminRegistration.ADMIN_FILTER);
        AdminRegistration.register(ar, context);

        // add ourselves as a listener for when the Darkstar server changes
        DarkstarWebLoginFactory.getInstance().addDarkstarServerListener(this);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // remove the Darkstar server listener
        DarkstarWebLoginFactory.getInstance().removeDarkstarServerListener(this);
        
        // register with the admininstration page
        AdminRegistration.unregister(ar, context);

        // log out of any connected sessions
        WonderlandSession session = (WonderlandSession)context.getAttribute(SESSION_ATTR);
        if (session != null) {
            session.logout();
        }
    }

    public void serverStarted(DarkstarRunner runner, ServerSessionManager mgr) {
        // When a darkstar server starts up, open a connection to it, and
        // create the session with the classloader of the current class (the servlet classloader),
        // so that messages will be decoded correctly
        try {
            WonderlandSession session = mgr.createSession(
                    new SessionCreator<WonderlandSession>() {

                        public WonderlandSession createSession(ServerSessionManager sessionManager,
                                WonderlandServerInfo serverInfo,
                                ClassLoader loader) {
                            return new WonderlandSessionImpl(sessionManager,
                                    serverInfo,
                                    getClass().getClassLoader());
                        }
                    });
            context.setAttribute(SESSION_ATTR, session);

            WebcamConnection conn = new WebcamConnection();
            session.connect(conn);
            context.setAttribute(WEBCAM_CONTROL_CONN_ATTR, conn);
        } catch (ConnectionFailureException ex) {
            logger.log(Level.SEVERE, "Connection failed", ex);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login failed", ex);
        }
    }

    public void serverStopped(DarkstarRunner arg0) {
        // When the darkstar server stops, remove the keys from the servlet
        // context
        context.removeAttribute(SESSION_ATTR);
        context.removeAttribute(WEBCAM_CONTROL_CONN_ATTR);
    }
}
