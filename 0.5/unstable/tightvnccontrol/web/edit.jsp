<%-- 
    Document   : edit
    Author     : Bernard Horan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="/wonderland-web-front/javascript/prototype-1.6.0.3.js" type="text/javascript"></script>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <h2>Modify TightVNC Viewer Settings</h2>

        <form id="nameForm" action="/tightvnccontrol/tightvnccontrol/browse">
            <input type="hidden" name="action" value="change"/>

            <table class="installed" id="runnerTable">
                <tr class="header">
                    <td class="installed"><b>TightVNC Viewer Name</b></td>
                    <td class="installed"><b>TightVNC Viewer Settings</b></td>
                </tr>
                <tr>
                    <td class="installed">${requestScope['record'].cellName}</td>
                    <td class="installed">
                        
                            VNC Server: <input type="text" name="vncServer" value="${record.vncServer}"/><br />
                            VNC Port: <input type="text" name="vncPort" value="${record.vncPort}"/><br/>
                            VNC User: <input type="text" name="vncUsername" value="${record.vncUsername}"/><br/>
                            VNC Password: <input type="password" name="vncPassword" value="${record.vncPassword}"/>
                            <input type="hidden" name="cellID" value="${requestScope['record'].cellID}"/>
                        
                    </td>

                </tr>
            </table>
            <div id="actionLinks">
                <a href="javascript:void(0)" onclick="$('nameForm').submit()">OK</a>
                <a href="/tightvnccontrol/tightvnccontrol/browse">Cancel</a>
            </div>
        </form>
    </body>
</html>
