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
package org.jdesktop.wonderland.modules.sqlservice.server;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * A service for performing SQL queries as a Darkstar service.
 * 
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SqlManager {
    SqlServiceImpl service;

    /**
     * Constructor, required by a manager object.
     **/
    public SqlManager(SqlServiceImpl service) {
        this.service = service;
    }

    /**
     * Execute the given runnable and pass results to the given callback.
     */
    public <T> void execute(SqlRunnable<T> runnable, SqlCallback<T> callback) {
        service.execute(runnable, callback);
    }

    /**
     * Execute a string query and return a result set
     */
    public void executeQuery(String query, SqlCallback<ResultSet> callback) {
        execute(new QueryRunnable(query), callback);
    }

    /**
     * Execute a string update and return a count of modified rows
     */
    public void executeUpdate(String update, SqlCallback<Integer> callback) {
        execute(new UpdateRunnable(update), callback);
    }

    /**
     * A basic query that takes a string and returns a ResultSet
     */
    class QueryRunnable extends BaseSqlRunnable<ResultSet> {
        private final String query;
      
        QueryRunnable(String query) {
            this.query = query;
        }

        @Override
        public ResultSet execute(Connection connection) throws Exception {
            return connection.createStatement().executeQuery(query);
        }
    }

    /**
     * A basic query that takes a string and returns an Integer
     */
    class UpdateRunnable extends BaseSqlRunnable<Integer> {
        private final String update;

        UpdateRunnable(String update) {
            this.update = update;
        }

        @Override
        public Integer execute(Connection connection) throws Exception {
            return connection.createStatement().executeUpdate(update);
        }
    }
}
