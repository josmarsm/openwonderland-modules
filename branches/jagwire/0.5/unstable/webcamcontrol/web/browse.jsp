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
        <h2>Manage Webcam Viewers</h2>

        <table class="installed" id="runnerTable">
            <caption>
              <span class="heading">Configure Webcam Viewers</span>
            </caption>
            <tr class="header">
                <td class="installed">CellID</td>
                <td class="installed">Cell Name</td>
                <td class="installed">Settings</td>
                <td class="installed">Actions</td>
            </tr>
            <c:forEach var="record" items="${requestScope['records']}">
                <tr>
                    <td class="installed">${record.cellID}</td>
                    <td class="installed">${record.cellName}</td>
                    <td class="installed">
                        <table>

                            <tr>
                                <td>Camera URI:</td>
                                <td><c:choose>
                                        <c:when test="${empty record.cameraURI}">
                                            <em>No URI set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.cameraURI}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td>Camera User name:</td>
                                <td><c:choose>
                                        <c:when test="${empty record.cameraUsername}">
                                            <em>No user set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.cameraUsername}
                                        </c:otherwise>
                                    </c:choose>

                                </td>
                            </tr>
                            <tr>
                                <td>Camera Password:</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty record.cameraPassword}">
                                            <em>No password set</em>
                                        </c:when>
                                        <c:otherwise>
                                            ${record.cameraObscuredPassword}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td>Camera State:</td>
                                <td>
                                    ${record.cameraState}
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
