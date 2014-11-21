<%-- 
    Document   : cohorts
    Created on : Nov 29, 2010, 4:45:04 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/isocial.tld" prefix="i" %>

<html>
    <head>
        <!--link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" /-->

        <link href="css/isocial.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="css/jquery.treeTable.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="css/jquery.ui.selectmenu.css" rel="stylesheet" type="text/css" media="screen" />

        <script src="/wonderland-web-front/javascript/json2.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>      
        <script src="scripts/isocial.js" type="text/javascript"></script>
        <script src="scripts/date.js" type="text/javascript"></script>
        <script src="scripts/jquery.treeTable.js" type="text/javascript"></script>
        <script src="scripts/jquery.ui.selectmenu.js" type="text/javascript"></script>

        <style>
            div#manage-lessons { width: 800px; margin: 20px 0; }
            div#manage-lessons table { margin: 1em 0; border-collapse: collapse; width: 100%; }
            div#manage-lessons table td, div#manage-lessons table th { padding: .6em 10px .6em 20px; text-align: left; }
            div#manage-lessons select { width: 150px; }
            div#create-sheet-form select { width: 300px; }
        </style>



        <title>Manage Lessons</title>
    </head>
    <body>
        <%@ include file="navigation.jspf" %>
        <script type="text/javascript">
            navSelect("lessons");
        </script>

        <%@ include file="currentLesson.jspf" %>
        <%@ include file="unit.jspf" %>
        <%@ include file="lesson.jspf" %>
        <%@ include file="sheet.jspf" %>

        <script type="text/javascript">
            function Table() {
                this.units;
                this.lessons;
                this.sheets;
                var expanded;
                
                this.name = "lessons-table";
                this.update = function() {
                    
                    this.units = new Units(this);
                    
                    this.lessons = new Lessons(this);
                    
                    this.sheets = new Sheets(this);

                    // save the set of expanded nodes
                    expanded = [
                        <c:forEach var="id" items="${paramValues.expanded}" >
                            "${id}",
                        </c:forEach>
                               ];

                    $( "#lessons-table-body tr" ).each(function(i, obj) {
                        if ($(this).hasClass("expanded")) {
                            expanded.push(obj.id);
                        }
                    });

                    $( "#lessons-table-body *" ).remove();

                    this.units.load(this.lessons, this.sheets);
                }
                
                this.rebuild = function() {
                    rebuildTable(this.name, expanded);
                }
            }        

            // expand all
            function expandAll() {
                $("#lessons-table-body tr").each(function(index) {
                    $(this).expand();
                });
            }    

            // create UI on document creation
            var table;
            $(function()  {
                
                table = new Table();
                table.update();
            });
        </script>

        <div id="manage-lessons" class="ui-widget">
            <h1>Manage Lessons</h1>

            <table class="ui-widget ui-widget-content" id="lessons-table">
                <thead>
                    <tr class="ui-widget-header">
                        <td>Name <div class="table-header-actions">[
                                <a href="javascript:void();" onclick="table.units.showCreateDialog()">Add Unit</a> |
                                <a href="javascript:void();" onclick="expandAll()">Expand All</a>
                                ]</div></td>
                        <td>Actions</td>
                        <td>Last Modified</td>
                        <td>Status</td>
                    </tr>
                </thead>
                <tbody id="lessons-table-body">
                </tbody>
            </table>
        </div>
    </body>
</body>
</html>
