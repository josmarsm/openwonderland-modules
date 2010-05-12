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

/**
 * Base implementation of SqlRunnable that handles results and errors.
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public abstract class BaseSqlRunnable<V> implements SqlRunnable<V> {
    private V result;
    private Throwable error;
    
    /**
     * An implementation of the run method. Subclasses should override
     * <code>execute()</code> instead of this method.
     * @param connection the connection to execute queries against
     */
    public void run(Connection connection) {
        try {
            setResult(execute(connection));
        } catch (Throwable t) {
            setError(t);
        }
    }

    /**
     * Abstract method to actually perform the SQL query or update. In the case
     * of any error, this method should throw an exception.
     *
     * @param connection the connection to execute the query on
     * @return the return value for this execution
     * @throws Exception any exceptions thrown while executing the query
     */
    public abstract V execute(Connection connection) throws Exception;

    /**
     * @inheritDoc
     */
    public V getResult() {
        return result;
    }

    /**
     * Set the result for this query. Used by <code>run()</code> to set the
     * result.
     * @param result the result to set.
     */
    protected void setResult(V result) {
        this.result = result;
    }

    /**
     * @inheritDoc
     */
    public Throwable getError() {
        return error;
    }

    /**
     * Set the error for this query. Used by <code>run()</code> to set the
     * error, if any.
     * @param error the error to set.
     */
    protected void setError(Throwable error) {
        this.error = error;
    }
}
