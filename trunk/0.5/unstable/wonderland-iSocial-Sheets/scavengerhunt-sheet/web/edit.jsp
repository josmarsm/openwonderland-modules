<%-- 
    Document   : edit1.jsp
    Created on : Dec 8, 2010, 9:41:51 AM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Editor</title>

        <link href="../isocial-sheets/css/isocial.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="../isocial-sheets/css/jquery.treeTable.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="../isocial-sheets/css/jquery.ui.selectmenu.css" rel="stylesheet" type="text/css" media="screen" />

        <script src="/wonderland-web-front/javascript/json2.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>     
        <script src="../isocial-sheets/scripts/isocial.js" type="text/javascript"></script>
        <script src="../isocial-sheets/scripts/jquery.treeTable.js" type="text/javascript"></script>
        <script src="../isocial-sheets/scripts/jquery.ui.selectmenu.js" type="text/javascript"></script>

        <style>
            div#mainForm input { padding-bottom: 10px; }
            table tbody td {
                border: none;
            }
        </style>

        <script type="text/javascript">
            $(function() {
                $( "#publish" ).button().focus();
                $( "#save" ).button();
                $( "#cancel" ).button();
            });
        </script>
    </head>
    <body>
        <c:set var="details" value="${requestScope['sheet'].details}" />

        <div id="mainForm" class="ui-widget-container ui-widget ui-corner-all">
            <h1>Edit ${details.name}</h1>

            <form id="editForm" action="#">
                <fieldset>
                <table>
                    <tbody>
                        <tr>
                            <td>Sheet Name:</td>
                            <td><input class="ui-widget-content ui-corner-all" size="30" type="text" name="name" value="${details.name}"/></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <input class="ui-widget-content ui-corner-all" style="display: inline" type="checkbox" name="autoOpen" value="true"
                                       <c:if test="${details.autoOpen}">checked="checked"</c:if>/> <label for="autoOpen" style="display: inline">Visible on startup</label>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                 <input class="ui-widget-content ui-corner-all" style="display: inline" type="checkbox" name="includeInMenu" value="true"
                                        <c:if test="${details.includeInMenu}">checked="checked"</c:if>/> <label for="includeInMenu" style="display: inline">Include in Windows Menu</label>
                            </td>
                        </tr>
                        <tr>
                            <td style="vertical-align: top">Instructions:</td>
                            <td>
                                <textarea class="ui-widget-content ui-corner-all" cols="30" rows="5" name="instructions">${details.instructions}</textarea>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <p style="font-weight: bold;margin-top: 5em;">
                                The following settings govern what the property sheet will look like when the Scavenger Hunt 
                                capability is add to an object in-world.
                                </p>
                        </tr>
                        <tr>
                            <td colspan="2"> Default "Question" text (leave blank if no default text is desired)</td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <textarea class="ui-widget-content ui-corner-all" cols="30" rows="3"  name="question">${details.question}</textarea>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"> Default value for "Give Up" setting and text (leave blank if no default text is desired)</td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <input class="ui-widget-content ui-corner-all" style="display: inline" type="checkbox" name="giveUp" value="true"
                                    <c:if test="${details.giveUp}">checked="checked"</c:if>/> <label for="giveUp" style="display: inline">Enable "Give up" message after hints are exhausted?</label>
                            </td> 
                        </tr>
                        <tr>
                            <td></td>
                            <td><textarea class="ui-widget-content ui-corner-all" cols="30" rows="3" name="giveUpText" >${details.giveUpText}</textarea></td>
                        </tr>
                    </tbody>
                </table>
                
                    <input id="publish" name="action" value="Publish" type="submit"/>
                    <input id="save" name="action" value="Save" type="submit"/>
                    <input id="cancel" name="action" value="Cancel" type="submit"/>
                </fieldset>
            </form>
        </div>
    </body>
</html>
