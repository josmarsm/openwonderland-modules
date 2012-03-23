/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.postercontrol.web.servlet;

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
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterControlConnection;
import uk.ac.essex.wonderland.modules.postercontrol.web.PosterRecord;

/**
 *
 * @author Bernard Horan
 */
public class PosterControlServlet extends HttpServlet implements ServletContextListener, DarkstarServerListener {

    private static final Logger logger = Logger.getLogger(PosterControlServlet.class.getName());
    private AdminRegistration ar = null;
    private ServletContext context = null;

    /** the key to identify the connection in the servlet context */
    public static final String POSTER_CONTROL_CONN_ATTR = "__postercontrolConfigConnection";

    /** the key to identify the darkstar session in the servlet context */
    public static final String SESSION_ATTR = "__postercontrolConfigSession";

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
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        Collection<PosterRecord> cellCollection = new HashSet<PosterRecord>();
        //Get the connection
        PosterControlConnection conn = (PosterControlConnection) getServletContext().getAttribute(POSTER_CONTROL_CONN_ATTR);


        
        try {
            cellCollection.addAll(conn.getPosterRecords());
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "runtime exception: {0}", ex.getMessage());
        }
        


        // See if the request comes with an "action" (e.g. Delete). If so,
        // handle it and fall through to below to re-load the page
        try {
            String action = request.getParameter("action");
            if (action != null && action.equalsIgnoreCase("delete") == true) {
                handleDelete(request, response, cellCollection);
            }
            else if (action != null && action.equalsIgnoreCase("edit") == true) {
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
     * Handles the default "browse" action to display the poster records.
     */
    private void handleBrowse(HttpServletRequest request,
            HttpServletResponse response, Collection<PosterRecord> c)
            throws ServletException, IOException,
            JAXBException
    {
        
        request.setAttribute("records", c);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/browse.jsp");
        rd.forward(request, response);
    }

    /**
     * Handles the default "edit" action to edit a poster record.
     */
    private void handleEdit(HttpServletRequest request,
            HttpServletResponse response, Collection<PosterRecord> c)
            throws ServletException, IOException,
            JAXBException
    {

        String cellID = request.getParameter("cellID");
        int id = Integer.valueOf(cellID);
        PosterRecord record = getRecord(c, id);
        if (record == null) {
            throw new RuntimeException("Can't find poster record for cellID: " + id);
        }
        request.setAttribute("record", record);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/edit.jsp");
        rd.forward(request, response);
    }

    /**
     * Deletes a poster.
     */
    private void handleDelete(HttpServletRequest request,
            HttpServletResponse response, Collection<PosterRecord> c)
        throws ServletException, IOException
    {
        String cellID = request.getParameter("cellID");        

        Object obj = getServletContext().getAttribute(POSTER_CONTROL_CONN_ATTR);
        if (obj != null) {
            int id = Integer.valueOf(cellID);
            PosterControlConnection connection = (PosterControlConnection)obj;
            connection.removePosterCell(id);
            PosterRecord oldRecord = getRecord(c, id);
            c.remove(oldRecord);
        }

        // After we have deleted the entry, then redisplay the listings
        try {
            handleBrowse(request, response, c);
        } catch (java.lang.Exception cre) {
            throw new ServletException(cre);
        }
    }

    private void handleChange(HttpServletRequest request,
            HttpServletResponse response, Collection<PosterRecord> c)
        throws ServletException, IOException
    {
        String cellID = request.getParameter("cellID");
        String contents = request.getParameter("contents");
        Object obj = getServletContext().getAttribute(POSTER_CONTROL_CONN_ATTR);
        if (obj != null) {
            int id = Integer.valueOf(cellID);
            PosterControlConnection connection = (PosterControlConnection)obj;
            connection.setPosterContents(id, contents);
            PosterRecord record = getRecord(c, id);
            record.setPosterContents(contents);
        }

        // After we have deleted the entry, then redisplay the listings
        try {
            handleBrowse(request, response, c);
        } catch (java.lang.Exception cre) {
            throw new ServletException(cre);
        }
    }

    private PosterRecord getRecord(Collection<PosterRecord> collection, int cellID) {
        for (PosterRecord posterRecord : collection) {
            if (posterRecord.getCellID() == cellID) {
                return posterRecord;
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
        ar = new AdminRegistration("Manage Posters",
                                   "/postercontrol/postercontrol/browse");
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

            PosterControlConnection conn = new PosterControlConnection();
            session.connect(conn);
            context.setAttribute(POSTER_CONTROL_CONN_ATTR, conn);
        } catch (ConnectionFailureException ex) {
            logger.log(Level.SEVERE, "Connection failed", ex);
        } catch (LoginFailureException ex) {
            logger.log(Level.WARNING, "Login failed", ex);
        }
    }

    public void serverStopped(DarkstarRunner arg0) {
        // When the darkstar server stops, remove the keys from the servlet
        // context
        context.removeAttribute(SESSION_ATTR);
        context.removeAttribute(POSTER_CONTROL_CONN_ATTR);
    }
}
