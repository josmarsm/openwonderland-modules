<%-- 
    Document   : index
    Created on : Feb 19, 2009, 5:48:24 PM
    Author     : bhoran
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Wonderland Recordings</title>
    </head>
    <body>
        <h3>Wonderland Recordings</h3>
        <table>
            <%@ page import="org.jdesktop.wonderland.web.wfs.*" %>
            <%@ page import="java.util.List" %>
            <% List<WFSRecording> recordings = WFSManager.getWFSManager().getWFSRecordings(); %>
            <% for (WFSRecording recording : recordings) { %>
            <tr>
                <td><%= recording.getName()%></td>
            </tr>
            <% } %>
        </table>
    </body>
</html>
