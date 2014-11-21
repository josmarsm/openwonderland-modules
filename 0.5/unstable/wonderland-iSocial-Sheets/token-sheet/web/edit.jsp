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
            .divTable
            {
                width: 80%;
                display:block;
                padding-top:10px;
                padding-bottom:10px;
                padding-right:10px;
                padding-left:10px;
                display:table;
            }
            .divRow
            {
                width: 99%;
                display:table-row;
                padding-bottom:15px;


            }
            .divColumnSmall
            {
                float: left;
                width: 25px;
                display:table-cell;
                padding-bottom:15px;
            }
            .divColumnMedium
            {
                float: left;
                width: 125px;
                display:table-cell;
                padding-bottom:15px;
            }
            .divColumnLarge
            {
                float: left;
                width: 250px;
                display:table-cell;
                padding-bottom:15px;

            }
        </style>

        <script type="text/javascript">
            $(function() {
                $( "#publish" ).button().focus();
                $( "#save" ).button();
                $( "#cancel" ).button();
            });
            function validate(evt) {
                var theEvent = evt || window.event;
                var key = theEvent.keyCode || theEvent.which;
                key = String.fromCharCode( key );
                var regex = /[0-9]|\./;
                if( !regex.test(key) ) {
                    theEvent.returnValue = false;
                    if(theEvent.preventDefault) theEvent.preventDefault();
                }
            }
        </script>
    </head>
    <body>
        <c:set var="details" value="${requestScope['sheet'].details}" />

        <div id="mainForm" class="ui-widget-container ui-widget ui-corner-all">
            <h1>Edit ${details.name}</h1>

            <form id="editForm" action="#">                
                <div class="divTable">
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="name">Name:</label></div>
                        <div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="name" value="${details.name}"/></div>
                    </div>
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="autoOpen">Visible on startup?</label></div>
                        <div class="divColumnSmall">
                            <input class="ui-widget-content ui-corner-all" type="checkbox" name="autoOpen" value="true"
                                   <c:if test="${details.autoOpen}">checked="checked"</c:if>/>
                        </div>
                    </div>
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="autoOpen">Dockable?</label></div>
                        <div class="divColumnSmall">
                            <input class="ui-widget-content ui-corner-all" type="checkbox" name="dockable" value="true"
                                   <c:if test="${details.dockable}">checked="checked"</c:if>/>
                        </div>
                    </div>
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="maxStudents">Number of Students?</label></div>
                        <div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="maxStudents" value="${details.maxStudents}" onkeypress="validate(event)"/></div>
                </div>
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="maxLessonTokens">Max. Lesson Tokens / Student?</label></div>
                        <div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="maxLessonTokens" value="${details.maxLessonTokens}" onkeypress="validate(event)"/></div>
                    </div>
                    <div class="divRow">
                        <div class="divColumnMedium"><label for="maxUnitTokens">Max. Unit Tokens / Class?</label></div>
                        <div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="maxUnitTokens" value="${details.maxUnitTokens}" onkeypress="validate(event)"/></div>
                    </div>                    
                </div>
        </div>
                <input id="publish" name="action" value="Publish" type="submit"/>
                <input id="save" name="action" value="Save" type="submit"/>
                <input id="cancel" name="action" value="Cancel" type="submit"/>
            </form>
        </div>
    </body>
</html>
