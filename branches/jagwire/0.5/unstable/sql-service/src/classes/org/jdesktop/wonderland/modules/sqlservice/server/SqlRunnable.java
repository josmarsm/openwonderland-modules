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
 * An interface for performing SQL queries. The connection object will
 * be passed in at run time.
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public interface SqlRunnable<V> {
    /**
     * Run the query with the given connection
     * @param connection the connection to use for queries
     */
    void run(Connection connection);

    /**
     * Return the result
     */
    V getResult();

    /**
     * Return the error
     */
    Throwable getError();
}
