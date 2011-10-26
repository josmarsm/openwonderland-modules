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
<script type="text/javascript">
    var hide = true;
    var updating = false;

    function update() {
        if (updating) {
            return;
        }

        updating = true;

        new Ajax.Request('resources/users/list', {
            method:'get', 
            requestHeaders: { Accept:'application/json' },
            onSuccess: function(response) {
                var response = response.responseText.evalJSON(true);
                updateUsers(response.users);
            },
            onFailure: function(response) {
                var row = new Element('tr');
                var td = new Element('td', { 'class': 'installed',
                                             'colspan': 3});
                td.update("Server unavailable");

                row.insert(td);
                var t = $('usersTable').down('tr', 1);
                while (t != null) {
                    var n = t.next();
                    t.remove();
                    t = n;
                }
                $('usersTable').insert(row);
            },
            onComplete: function() {
                updating = false;
            }
        });

        setTimeout(update, 5000);
    }
    
    function updateUsers(users) {
        var hidden = 0;

        var t = $('usersTable').down('tr', 1);
        while (t != null) {
            var n = t.next();
            t.remove();
            t = n;
        }

        for (var i = 0; i < users.length; i++) {
            var user = users[i];
            processUser(user);
            
            if (user.hidden) {
                hidden++;
            } else {
                var row = new Element('tr');
                row.insert(new Element('td', { 'class': 'installed' }));
                row.insert(new Element('td', { 'class': 'installed' }));
                row.insert(new Element('td', { 'class': 'installed' }));
                $('usersTable').insert(row);
            
                row.down('td', 0).update(user.name);
                row.down('td', 1).update(user.when);

                var actions = row.down('td', 2);
                actions.update();
                for (var l = 0; l < user.link.length; l++) {
                    actions.insert(user.link[l]);
                    actions.insert(' ');
                }
            }
        }

        $('hideLink').update();
        if (hidden > 0) {
            $('hideLink').insert(new Element('a', { 'href': 'javascript:void(0);',
                                                    'onclick': 'setHidden(false)'}).update("Show all users"));
        } else {
            $('hideLink').insert(new Element('a', { 'href': 'javascript:void(0);',
                                                    'onclick': 'setHidden(true)'}).update("Hide system users"));
        }
    }
    
    function processUser(user) {
        user.link = [];

        user.link.push(new Element('a', { 'href': 'javascript:void(0);',
                                          'onclick': 'mute(\'' + user.id + '\')' }).update("mute"));
        user.link.push(new Element('a', { 'href': 'javascript:void(0);',
                                          'onclick': 'disconnect(\'' + user.id + '\', \'' +
                                                     user.name + '\')' }).update("disconnect"));

        if (hide &&
            (user.name == "webserver" ||
             user.name == "sasxprovider" ||
             user.name == "admin" ||
             user.name == "darkstar"))
         {
             user.hidden = true;
         }
    }

    function mute(userId) {
        new Ajax.Request('resources/users/' + userId + '/mute', {
            method:'get',
            onSuccess: function(response) {
                window.location.reload();
            }
        });
    }

    function disconnect(userId, userName) {
        if (confirm('Disconnect user ' + userName + '?')) {
            new Ajax.Request('resources/users/' + userId + '/disconnect', {
                method:'get',
                onSuccess: function(response) {
                    window.location.reload();
                }
            });
        }
    }

    function setHidden(hidden) {
        hide = hidden;
        update();
    }

    function broadcast() {
        var form = $('broadcast');
        var input = form['broadcastMessage'];

        new Ajax.Request('resources/users/broadcast', {
            method: 'post',
            contentType: 'text/plain',
            postBody: $F(input),
            onSuccess: function(response) {
                input.clear();
            }
        });
    }
</script>
</head>
<body onload="update();">

    <h3>Broadcast Message</h3>
    <form id="broadcast" action="javascript:broadcast()">
        <input type="text" size="60" id="broadcastMessage">
        <input type="submit" value="Broadcast">
    </form>

    <br>
    <h3>Connected Users</h3>

    <table class="installed" id="usersTable">
        <tr class="header">
            <td class="installed">Name</td>
            <td class="installed">Connected Since</td>
            <td class="installed">Actions</td>
        </tr>
    </table>

    <div id="hideLink">
    </div>
</body>
