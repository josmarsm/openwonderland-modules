/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.sqlservicetest.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.sqlservice.server.BaseSqlRunnable;
import org.jdesktop.wonderland.modules.sqlservice.server.SqlCallback;
import org.jdesktop.wonderland.modules.sqlservice.server.SqlManager;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.UserListener;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A simple test that records when users log in and out of the Darkstar
 * server.
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */

@Plugin
public class SqlServiceTestServerPlugin implements ServerPlugin {
    private static final Logger logger =
            Logger.getLogger(SqlServiceTestServerPlugin.class.getName());

    public void initialize() {
        UserManager.getUserManager().addUserListener(new LoginUserListener());
    }

    static class LoginUserListener implements UserListener {

        public void userLoggedIn(WonderlandClientID clientID,
                                 ManagedReference<UserMO> userRef)
        {
            String username = userRef.get().getUsername();
            SqlManager manager = AppContext.getManager(SqlManager.class);
            
            manager.execute(new LastLoginQuery(username),
                            new LastLoginCallback(username));

            String insert = "INSERT into logins (userid, type) values ('" +
                            username + "', 'login')";
            AppContext.getManager(SqlManager.class).executeUpdate(insert, null);
        }

        public void userLoggedOut(WonderlandClientID clientID,
                                  ManagedReference<UserMO> userRef,
                                  ManagedReference<Queue<Task>> logoutTasksRef)
        {
            String username = userRef.get().getUsername();

            String insert = "INSERT into logins (userid, type) values ('" +
                            username + "', 'logout')";
            AppContext.getManager(SqlManager.class).executeUpdate(insert, null);
        }
    }

    private static class LastLoginQuery extends BaseSqlRunnable<Timestamp> {
        private String username;
      
        public LastLoginQuery(String username) {
            this.username = username;
        }

        @Override
        public Timestamp execute(Connection conn) throws Exception {
            Statement s = conn.createStatement();
            try {
                ResultSet rs = s.executeQuery(
                            "select ts from logins where userid='" +
                            username + "' order by ts desc limit 1;");
                
                if (rs.next()) {
                    return rs.getTimestamp("ts");
                } else {
                    return null;
                }
            } finally {
                s.close();
            }
        }
    }

    private static class LastLoginCallback 
            implements SqlCallback<Timestamp>, Serializable
    {
        private String username;

        public LastLoginCallback(String username) {
            this.username = username;
        }

        public void handleResult(Timestamp t) {
            if (t == null) {
                logger.warning("First login for " + username);
            } else {
                logger.warning("Last login for " + username + " is " +
                           new java.util.Date(t.getTime()));
            }
        }

        public void handleError(Throwable t) {
            logger.log(Level.WARNING, "Error getting last login for " +
                       username, t);
        }
    }
}
