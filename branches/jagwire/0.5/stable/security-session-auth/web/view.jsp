<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <script src="/wonderland-web-front/javascript/prototype.js" type="text/javascript"></script>
    </head>
    <body>
        <h2>View Users</h2>

        <c:if test="not empty ${requestScope['error']}">
            <font color="red">${requestScope['error']}</font>
        </c:if>

        <table class="installed" id="userTable">
            <caption>
                <span class="heading">Users</span>
            </caption>
            <tr class="header">
                <td class="installed">Name</td>
                <td class="installed">Id</td>
                <td class="installed">Email</td>
                <td class="installed">Actions</td>
            </tr>

            <c:forEach var="user" items="${requestScope['users']}">
                <tr class="installed">
                    <td class="installed">${user.fullname}</td>
                    <td class="installed">${user.id}</td>
                    <td class="installed">${user.email}</td>
                    <td class="installed">
                        <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=edit%26id=${user.id}"
                           target="_top"/>edit</a>
                        <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=remove%26id=${user.id}"
                           target="_top"/>remove</a>
                    </td>
                </tr>
            </c:forEach>
        </table>

        <div id="actionLinks">
            <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=create" target="_top">Add user</a>
        </div>
    </body>
