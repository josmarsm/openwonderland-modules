/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.web;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.QuestionSheetDetails;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntSheet;
import org.jdesktop.wonderland.modules.isocial.weblib.servlet.ISocialServletBase;

/**
 * Servlet that handles editing for the sample sheet
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ScavengerHuntEditorServlet extends ISocialServletBase {
    private static final Logger LOGGER =
            Logger.getLogger(ScavengerHuntEditorServlet.class.getName());

    private static final String UNIT_ID_PARAM = "unitId";
    private static final String LESSON_ID_PARAM = "lessonId";
    private static final String SHEET_ID_PARAM = "sheetId";
    private static final String ACTION_PARAM = "action";

    private static final String SHEET_ATTR = "sheet";

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
        try {
            // find the sheet
            Sheet sheet = getSheet(request);

            // store it in request scope
            request.setAttribute(SHEET_ATTR, sheet);

            // find the action
            String action = request.getParameter(ACTION_PARAM);
            if (action == null) {
                action = "edit";
            }

            if (action.equalsIgnoreCase("save") ||
                action.equalsIgnoreCase("publish"))
            {
                doSave(sheet, request, response);
            } else if (action.equalsIgnoreCase("cancel")) {
                doCancel(sheet, request, response);
            } else if(action.equalsIgnoreCase("ok")){
                // this wil handle OK button on question sheet, ie. return to previous page
                doCancel(sheet, request, response);
            } else {
                // default action
                doEdit(sheet, request, response);
            }
        } catch (WebApplicationException wae) {
            handleException(wae, response);
        }
    }

    /**
     * Handle the edit action on a sheet. This simply forwards to the relevant
     * editor.
     * @param sheet the sheet to edit
     * @param request the servlet request
     * @param response the servlet response
     */
    private void doEdit(Sheet sheet, HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        String editor = null;
        if (sheet.getDetails() instanceof ScavengerHuntSheet) {
            editor = "/edit.jsp";
        } else if(sheet.getDetails() instanceof QuestionSheetDetails){
            editor = "/question.jsp";
        }else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RequestDispatcher rd = request.getRequestDispatcher(editor);
        rd.forward(request, response);
    }

    /**
     * Save the sheet details to the DAO
     * @param sheet the sheet to save
     * @param request the servlet request
     * @param response the servlet response
     */
    private void doSave(Sheet sheet, HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException
    {
        if (sheet.getDetails() instanceof ScavengerHuntSheet) {
            String question = request.getParameter("question");
            ((ScavengerHuntSheet) sheet.getDetails()).setQuestion(question);

            String name = request.getParameter("name");
            ((ScavengerHuntSheet) sheet.getDetails()).setName(name);

            String autoOpen = request.getParameter("autoOpen");
            ((ScavengerHuntSheet) sheet.getDetails()).setAutoOpen(Boolean.parseBoolean(autoOpen));
            
            String includeInMenu = request.getParameter("includeInMenu");
            ((ScavengerHuntSheet) sheet.getDetails()).setIncludeInMenu(Boolean.parseBoolean(includeInMenu));
            
            String giveUp = request.getParameter("giveUp");
            ((ScavengerHuntSheet) sheet.getDetails()).setGiveUp(Boolean.parseBoolean(giveUp));
            
            String giveUpText = request.getParameter("giveUpText");
            ((ScavengerHuntSheet) sheet.getDetails()).setGiveUpText(giveUpText);
            
            String instructions = request.getParameter("instructions");
            ((ScavengerHuntSheet) sheet.getDetails()).setInstructions(instructions);
            
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        boolean publish = request.getParameter("action").equalsIgnoreCase("publish");
        sheet.setPublished(publish);

        dao(request).updateSheet(sheet);


        String query = "?expanded=" + sheet.getUnitId() +
                       "&expanded=" + sheet.getUnitId() + "-" + sheet.getLessonId();


        response.sendRedirect("/isocial-sheets/isocial-sheets/lessons.jsp" + query);
    }

    /**
     * Return to the lesson editor without saving
     * @param sheet the sheet that we are editing
     * @param request the servlet request
     * @param response the servlet response
     */
    private void doCancel(Sheet sheet, HttpServletRequest request,
                          HttpServletResponse response)
        throws IOException, ServletException
    {
        String query = "?expanded=" + sheet.getUnitId() +
                       "&expanded=" + sheet.getUnitId() + "-" + sheet.getLessonId();


        response.sendRedirect("/isocial-sheets/isocial-sheets/lessons.jsp" + query);
    }

    private Sheet getSheet(HttpServletRequest request) {
        Sheet out = null;

        // first see if the request specified a new sheet to load
        String unitId = request.getParameter(UNIT_ID_PARAM);
        String lessonId = request.getParameter(LESSON_ID_PARAM);
        String sheetId = request.getParameter(SHEET_ID_PARAM);
        if (unitId != null && lessonId != null && sheetId != null) {
            out = dao(request).getSheet(unitId, lessonId, sheetId);

            if (out != null) {
                // store the sheet in the session
                request.getSession().setAttribute(SHEET_ATTR, out);
                return out;
            }
        }

        // next see if there is a stored sheet in the session
        
        out = (Sheet) request.getSession().getAttribute(SHEET_ATTR);
        if (out != null) {
            return out;
        }

        // not found
        throw new WebApplicationException(Response.
                status(Response.Status.NOT_FOUND).
                entity("No sheet found").build());
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
}
