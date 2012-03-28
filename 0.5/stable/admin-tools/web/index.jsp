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
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />

        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>

        <script src="scripts/date.js" type="text/javascript"></script>

        <script type="text/javascript">
            var hide = true;
            var updating = false;
            
            $(function() {
                $("#broadcastButton").button().click(function() {
                    broadcast();
                });
                
                $('#view-report-form').dialog({
                    autoOpen: false,
                    resizable: true,
                    width: 720,
                    height: 300,
                    modal: true,
                    buttons: {
                        "Cancel": function() {
                            $(this).dialog("close");
                        },
                        "Submit": function() {
                            generateErrorReport();
                            $(this).dialog("close");
                        }
                    }
                });
                
                updateTimer();
            });
            
            
            function broadcast() {
                var val = $("#broadcastMessage").val();
                $.ajax({
                    type: "POST",
                    url: 'resources/users/broadcast',
                    data: val,
                    contentType: "text/plain"
                }).success(function() {
                    $("#broadcastMessage").val('');
                });
            }
            
            function updateTimer() {
                update();
                setTimeout(updateTimer, 5000);
            }
            
            function update() {
                if (updating) {
                    return;
                }
                
                updating = true;
                
                $.getJSON('resources/users/list')
                .success(function(response) {
                    updateUsers(response.users);
                })
                .error(function() {
                    updateUsers(null);
                })
                .complete(function() {
                    updating = false;
                });
            }
            
            function updateUsers(users) {
                var hidden = 0;
                var table = $("#usersTable");
                
                table.empty();
                $('<tr class="header">' +
                    '<td class="installed">Name</td>' +
                    '<td class="installed">Connected Since</td>' +
                    '<td class="installed">Actions</td>' +
                  '</tr>').appendTo(table);
                
                if (!users) {
                    $('<tr><td class="installed" colspan="3">Server unavailable</td></tr>').appendTo(table);
                    return;
                }
                
                for (var i = 0; i < users.length; i++) {
                    var user = users[i];
                    
                    if (hide && systemUser(user)) {
                        hidden++;
                    } else {
                        processUser(user).appendTo(table);
                    }   
                }
                
                var hideLink = $("#hideLink");
                hideLink.empty();
                if (hidden > 0) {
                    $('<a href="#">Show all users</a>').click(function() {
                        setHidden(false);
                    }).appendTo(hideLink);
                } else {
                    $('<a href="#">Hide system users</a>').click(function() {
                        setHidden(true);
                    }).appendTo(hideLink);
                }
            }
            
            function systemUser(user) {
                return (user.name == "webserver" ||
                        user.name == "sasxprovider" ||
                        user.name == "admin" ||
                        user.name == "darkstar" ||
                        user.name == "cmu-player");
            }
            
            function processUser(user) {
                var row = $('<tr></tr>');
                var date = new Date(Date.parse(user.when));
                
                $('<td class="installed">' + user.name + "</td>").appendTo(row);
                $('<td class="installed">' + date.format() + "</td>").appendTo(row);
                
                var actions = $('<td class="installed"></td>');
                
                $('<a href="#">mute </a>').click(function() {
                    mute(user.id);
                    return false;
                }).appendTo(actions);
                
                $('<a href="#">error report </a>').click(function() {
                    errorReport(user.id, user.name);
                    return false;
                }).appendTo(actions);
                
                $('<a href="#">disconnect</a>').click(function() {
                    disconnect(user.id, user.name);
                    return false;
                }).appendTo(actions);
                
                actions.appendTo(row);
                return row;
            }
            
            function mute(userId) {
                $.get('resources/users/' + userId + '/mute')
                 .success(function() {
                     window.location.reload();
                 });
            }
            
            function errorReport(userId, userName) {
                $("#view-user").val(userName);
                $("#view-user-id").val(userId);
                $("#view-comments").val('');
                $("#view-report-form").dialog("open");
            }
            
            function disconnect(userId, userName) {
                if (confirm('Disconnect user ' + userName + '?')) {
                    $.get('resources/users/' + userId + '/disconnect')
                     .success(function() {
                        window.location.reload();
                    });
                }
            }
            
            function generateErrorReport() {
                var errorReport = {
                    creator: $("#view-user").val(),
                    timeStamp: new Date().format(dateFormat.masks.isoUtcDateTime),
                    comments: $("#view-comments").val(),
                    content: "Not available."
                }
                
                var userId = $("#view-user-id").val();
                
                $.ajax({
                    type: "POST",
                    url: '../../error-report/error-report/resources/errorReports/create',
                    data: JSON.stringify(errorReport),
                    contentType: "application/json",
                    dataType: "json"
                })
                 .success(function(data) {
                    $.get('resources/users/' + userId + '/errorReport?reportId=' + data.id)
                     .success(function() {
                        window.location = "../../error-report/error-report/ErrorReports.html";
                    });
                 });
            }
            
            function setHidden(hidden) {
                hide = hidden;
                update();
            }
        </script>
</head>
<body>

    <h3>Broadcast Message</h3>
    <input type="text" size="60" id="broadcastMessage">
    <button id="broadcastButton" value="Broadcast">Broadcast</button>

    <br>
    <h3>Connected Users</h3>

    <table class="installed" id="usersTable">
    </table>

    <div id="hideLink">
    </div>
    
    <div id="view-report-form" title="Submit Error Report">   
        <input type="hidden" id="view-user"/>
        <input type="hidden" id="view-user-id"/>
        
        <label for="view-comments">Comments:</label>
        <br>
        <textarea style="width: 97%;" rows="4" name="view-comments" id="view-comments" class="ui-widget-content ui-corner-all"></textarea>
        <br>
    </div>
</body>
