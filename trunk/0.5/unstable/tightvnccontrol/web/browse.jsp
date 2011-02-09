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
    </head>
    <body>
        <h2>Manage TightVNC Viewers</h2>
        <p>Use this page to configure TightVNC Viewers.</p>
        <table class="installed" id="runnerTable">
            <col width="10%"/>
            <col width="20%"/>
            <col width="50%"/>
            <col width="20%"/>
            <tr class="header">
                <td class="installed"><b>CellID</b></td>
                <td class="installed"><b>Cell Name</b></td>
                <td class="installed"><b>Settings</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="record" items="${requestScope['records']}">
                <tr>
                    <td class="installed">${record.cellID}</td>
                    <td class="installed">${record.cellName}</td>
                    <td class="installed">
                        <table>

                            <tr>
                                <td>VNC Server:</td>
                                <td><c:choose>
                                        <c:when test="${empty record.vncServer}">
                                            <em>No server set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.vncServer}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            <tr>
                                <td>VNC Port:</td>
                                <td><c:choose>
                                        <c:when test="${empty record.vncPort}">
                                            <em>No port set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.vncPort}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td>VNC User name:</td>
                                <td><c:choose>
                                        <c:when test="${empty record.vncUsername}">
                                            <em>No user set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.vncUsername}
                                        </c:otherwise>
                                    </c:choose>

                                </td>
                            </tr>
                            <tr>
                                <td>VNC Password:</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty record.vncPassword}">
                                            <em>No password set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.vncObscuredPassword}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
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
