<%-- 
    Document   : question
    Created on : May 21, 2012, 1:00:22 PM
    Author     : Vladimir Djurovic
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

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
        <p>
            To configure questions, add the question capability to objects in the world.
        </p>
        <div id="mainForm" class="ui-widget-container ui-widget ui-corner-all">
            <form id="editForm" action="#">
                <fieldset>
                    <input id="cancel" name="action" value="OK" type="submit" />
                </fieldset>
            </form>
        </div>
    </body>
</html>
