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
        <script src="scripts/jquery.treeTable.js" type="text/javascript"></script>
        <script src="scripts/jquery.ui.selectmenu.js" type="text/javascript"></script>
        <script src="scripts/isocial.js" type="text/javascript"></script>

        <style>
            div#manage-cohorts { width: 550px; margin: 20px 0; }
            div#manage-cohorts table { margin: 1em 0; border-collapse: collapse; width: 100%; }
            div#manage-cohorts table td, div#manage-cohorts table th { padding: .6em 10px .6em 20px; text-align: left; }
            div#manage-cohorts select { width: 150px; }
        </style>

        <script type="text/javascript">
            var cohorts;

            function update() {
                cohorts = new Array();

                $( "#cohorts-table-body" ).html("");

                $.getJSON("resources/cohorts", function(data) {
                    // if we get back an array with length 1, the each
                    // won't work correctly. In this case, manually create
                    // an array
                    if (!(data.cohort instanceof Array)) {
                        var c = data.cohort;
                        data.cohort = new Array();
                        data.cohort.push(c);
                    }

                    $.each(data.cohort, function(i, cohort) {
                        updateCohort(cohort);
                    });

                    $( "#cohorts-table" ).treeTable({
                        initialState: "collapsed"
                    });
                });
            }

            function updateCohort(cohort) {
                cohorts[cohort.id] = cohort;

                $( "#cohorts-table-body" ).append(
                "<tr id=\"" + cohort.id + "\"> " +
                    "<td>" + cohort.name + "</td>" +
                    "<td>" + generateActions(cohort.id + "-select") + "</td></tr>");

                // create a styled select menu
                $('select#' + cohort.id + "-select").selectmenu({
                    select: function(event, options) {
                        switch (options.value) {
                            case "edit":
                                editCohortDialog(cohort);
                                break;
                            case "delete":
                                deleteCohortDialog(cohort);
                                break;
                            case "add":
                                addGuideDialog(cohort);
                                break;
                            case "results":
                                getResults(cohort);
                                break;
                        }
                    }
                });

                // if we get back an array of length 1, expand it into
                // a real array
                cohort.guides = ensureArray(cohort.guides);

                $.each(cohort.guides, function(index, guide) {
                    if (guide == "") {
                        cohort.guides.splice(index, 1);
                        return;
                    }
                    
                    var guideId = guide.replace(/[^\w]+/, "_");
                    $( "#cohorts-table-body" ).append(
                        "<tr id=\"" + cohort.id + "-child\" class=\"child-of-" + cohort.id + "\"> " +
                        "<td>" + guide + "</td>" +
                        "<td>" + generateGuideActions(guideId + "-guideselect") + "</td></tr>");
                    
                    $('select#' + guideId + "-guideselect").selectmenu({
                        select: function(event, options) {
                            switch (options.value) {
                                case "delete":
                                    removeGuide(cohort.id, index);
                                    break;
                            }
                        }
                    });
                });
            }
            

            function editCohortDialog(cohort) {
                $( "#edit-id").val(cohort.id);
                $( "#edit-name").val(cohort.name);
                $( "#edit-cohort-form" ).dialog( "open" );
            }

            function deleteCohortDialog(cohort) {
                $( "#delete-id" ).val(cohort.id);
                $( "#delete-confirm-dialog" ).dialog( "open" );
            }

            function addGuideDialog(cohort) {
                $( "#guide-id" ).val(cohort.id);
                $( "#add-guide-form" ).dialog( "open" );
            }

            function generateActions(id) {
                return "<form action=\"#\"><fieldset> " +
                       "<select name=\"" + id + "\" id=\"" + id + "\">" +
                         "<option value=\"none\">Select action</option>" +
                         "<option value=\"edit\">Edit</option>" +
                         "<option value=\"delete\">Delete</option>" +
                         "<option value=\"add\">Add guide</option>" +
                         "<option value=\"results\">Get results</option>" +
                       "</select></fieldset></select></form>";
            }

            function generateGuideActions(id) {
                return "<form action=\"#\"><fieldset> " +
                       "<select name=\"" + id + "\" id=\"" + id + "\">" +
                         "<option value=\"none\">Select action</option>" +
                         "<option value=\"delete\">Delete</option>" +
                       "</select></fieldset></select></form>";
            }

            function removeGuide(cohortId, index) {
                var cohort = cohorts[cohortId];
                cohort.guides.splice(index, 1);

                $.ajax({
                    type: 'POST',
                    url: "resources/cohorts/" + cohort.id,
                    data: JSON.stringify(cohort),
                    success: function(data) {
                        update();
                    },
                    contentType: "application/json"
                });
            }

            // show the add cohort dialog
            function addCohortDialog() {
                $( "#create-cohort-form" ).dialog( "open" );
            }

            // expand all
            function expandAll() {
                $("#cohorts-table-body tr").each(function(index) {
                    $(this).expand();
                });
            }

            function getResults(cohort) {
                var prefix = "?";
                var query = "";

                if (cohort) {
                    query += prefix + "cohortId=" + cohort.id;
                    prefix = "&";
                }

                window.location.href="results.jsp" + query;
            }

            // create UI on document creation
            $(document).ready(function()  {
                // create the add cohort dialog
                $( "#create-cohort-form" ).dialog({
                    autoOpen: false,
                    height: 300,
                    width: 350,
                    modal: true,
                    buttons: {
                        "Create cohort": function() {
                            var cohort = new Object();
                            cohort.name = $( "#name" ).val();

                            $.ajax({
                                type: 'POST',
                                url: "resources/cohorts/new",
                                data: JSON.stringify(cohort),
                                dataType: 'json',
                                success: function(data) {
                                    $( "#create-cohort-form" ).dialog( "close" );
                                    $( "#name" ).val("");
                                    update();
                                },
                                contentType: "application/json"
                            });

                        },
                        Cancel: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                });

                // create the add cohort dialog
                $( "#edit-cohort-form" ).dialog({
                    autoOpen: false,
                    height: 300,
                    width: 350,
                    modal: true,
                    buttons: {
                        "Update cohort": function() {
                            // find the cohort to update
                            var cohortId = $( "#edit-id").val();
                            var cohort = cohorts[cohortId];

                            // change its name
                            cohort.name = $( "#edit-name" ).val();
                            
                            $.ajax({
                                type: 'POST',
                                url: "resources/cohorts/" + cohort.id,
                                data: JSON.stringify(cohort),
                                success: function(data) {
                                    $( "#edit-cohort-form" ).dialog( "close" );
                                    update();
                                },
                                contentType: "application/json"
                            });

                        },
                        Cancel: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                });

                $( "#delete-confirm-dialog" ).dialog({
			autoOpen: false,
                        resizable: false,
			height:140,
			modal: true,
			buttons: {
				"Delete cohort": function() {
                                    var cohortId = $( "#delete-id" ).val();

                                    $.ajax({
                                        type: 'DELETE',
                                        url: "resources/cohorts/" + cohortId,
                                        success: function(data) {
                                            $( "#delete-confirm-dialog" ).dialog( "close" );
                                            update();
                                        },
                                        contentType: "application/json"
                                    });
				},
				Cancel: function() {
                                    $( this ).dialog( "close" );
				}
			}
		});

                // create the add cohort dialog
                $( "#add-guide-form" ).dialog({
                    autoOpen: false,
                    height: 300,
                    width: 350,
                    modal: true,
                    buttons: {
                        "Add guide": function() {
                            // find the cohort to update
                            var cohortId = $( "#guide-id").val();
                            var cohort = cohorts[cohortId];

                            cohort.guides.push($( "#guide-name" ).val());

                            $.ajax({
                                type: 'POST',
                                url: "resources/cohorts/" + cohort.id,
                                data: JSON.stringify(cohort),
                                success: function(data) {
                                    $( "#add-guide-form" ).dialog( "close" );
                                    update();
                                },
                                contentType: "application/json"
                            });

                        },
                        Cancel: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                });

                update();
            });
        </script>


        <title>Manage Cohorts</title>
    </head>
    <body>
        <%@ include file="navigation.jspf" %>
        <script type="text/javascript">
            navSelect("cohorts");
        </script>

        <div id="manage-cohorts" class="ui-widget">
            <h1>Manage Cohorts</h1>

            <table class="ui-widget ui-widget-content" id="cohorts-table">
                <thead>
                    <tr class="ui-widget-header">
                        <td>Name <div class="table-header-actions">[
                                <a href="javascript:void();" onclick="addCohortDialog()">Add Cohort</a> |
                                <a href="javascript:void();" onclick="expandAll()">Expand All</a>
                                ]</div></td>
                        <td>Actions</td>
                    </tr>
                </thead>
                <tbody id="cohorts-table-body">
                </tbody>
            </table>
        </div>

        <div id="create-cohort-form" title="Create new cohort">
            <form action="#">
                <fieldset>
                    <label for="name">Name</label>
                    <input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />
                </fieldset>
            </form>
        </div>

        <div id="edit-cohort-form" title="Edit cohort">
            <form action="#">
                <fieldset>
                    <label for="edit-name">Name</label>
                    <input type="text" name="edit-name" id="edit-name" class="text ui-widget-content ui-corner-all" />
                    <input type="hidden" name="edit-id" id="edit-id"/>
                </fieldset>
            </form>
        </div>

        <div id="delete-confirm-dialog" title="Delete cohort?">
            <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
            This cohort will be permanently deleted and cannot be recovered. Are you sure?</p>

            <form action="#">
                <fieldset>
                    <input type="hidden" name="delete-id" id="delete-id"/>
                </fieldset>
            </form>
        </div>

        <div id="add-guide-form" title="Add an online guide">
            <form action="#">
                <fieldset>
                    <label for="guide-name">Name</label>
                    <input type="text" name="guide-name" id="guide-name" class="text ui-widget-content ui-corner-all" />
                    <input type="hidden" name="guide-id" id="guide-id"/>
                </fieldset>
            </form>
        </div>
    </body>
</body>
</html>
