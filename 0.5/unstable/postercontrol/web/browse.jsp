<%-- 
    Document   : browse.jsp
    Author     : Bernard Horan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>Manage Posters</title>
    </head>
    <body>
        <h2>Manage Posters</h2>
        <p>Use this page to configure poster cells.</p>
        <table class="installed" id="runnerTable">
            <tr class="header">
                <td class="installed"><b>Poster CellID</b></td>
                <td class="installed"><b>Poster Name</b></td>
                <td class="installed"><b>Poster Contents</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="record" items="${requestScope['records']}">
                <tr>
                    <td class="installed">${record.cellID}</td>
                    <td class="installed">${record.cellName}</td>
                    <td class="installed">${record.posterContents}</td>
                    <td class="installed">
                    <c:forEach var="action" items="${record.actions}">
                        <a href="${pageContext.servletContext.contextPath}/browse?action=${action.url}">${action.name}</a>
                    </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>
