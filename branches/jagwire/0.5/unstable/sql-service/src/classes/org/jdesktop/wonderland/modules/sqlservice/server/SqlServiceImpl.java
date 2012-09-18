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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.FileInputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Implementation of the SQL service
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SqlServiceImpl extends AbstractService {
    /** The logger for this class. */
    private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(SqlServiceImpl.class.getName()));

    /** manages the context of the current transaction */
    private final TransactionContextFactory<SqlTransactionContext> ctxFactory;

    /** connection properties */
    private static Properties connectionProps;

    /** executor */
    private final ExecutorService executor;

    private static ThreadLocal<Connection> connectionLocal = new ThreadLocal() {
        @Override
        protected Connection initialValue() {
            try {
                String url = connectionProps.getProperty("url");
                String username = connectionProps.getProperty("username");
                String password = connectionProps.getProperty("password");

                return DriverManager.getConnection(url, username, password);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    public SqlServiceImpl(Properties prop,
                             ComponentRegistry registry,
                             TransactionProxy transactionProxy)
    {
        super (prop, registry, transactionProxy, logger);

        ctxFactory = new TransactionContextFactoryImpl(transactionProxy);
        executor = new ThreadPoolExecutor(0, 2, 5, TimeUnit.SECONDS,
                                          new LinkedBlockingQueue<Runnable>(),
                                          new SqlThreadFactory());
    }

    @Override
    public String getName() {
        return SqlServiceImpl.class.getName();
    }

    @Override
    protected void doReady() throws Exception {
        // load the connection properties file
        String driverFile = System.getProperty("sqlservice.properties.file");
        if (driverFile == null) {
            throw new Exception("sqlservice.properties not loaded");
        }

        // load the properties
        connectionProps = new Properties();
        connectionProps.load(new FileInputStream(driverFile));

        // create an instance of the driver object to make sure it
        // is loaded by the system
        String driver = connectionProps.getProperty("driver");
        Class.forName(driver).newInstance();
    }

    @Override
    protected void doShutdown() {
        // nothing to do
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
                                                Version currentVersion)
    {
        throw new IllegalStateException(
                    "unable to convert version:" + oldVersion +
                    " to current version:" + currentVersion);
    }

    <T> void execute(SqlRunnable<T> runnable, SqlCallback<T> callback) {
        // add data to the transaction
        ctxFactory.joinTransaction().add(runnable, callback);
    }

    /**
     * Transaction state
     */
    private class SqlTransactionContext extends TransactionContext {
        private final List<SqlRunner> runners = new ArrayList<SqlRunner>();

        public SqlTransactionContext(Transaction txn) {
            super (txn);
        }

        public <T> void add(SqlRunnable<T> runnable, SqlCallback<T> callback) {
            BigInteger callbackID = null;
            if (callback != null) {
                // wrap the callback if it is not a ManagedObject
                if (!(callback instanceof ManagedObject)) {
                    callback = new SqlCallbackWrapper<T>(callback);
                }

                // create a managed reference for the callback, which we know
                // is a managed object because it was wrapped above
                ManagedReference<SqlCallback<T>> callbackRef =
                        AppContext.getDataManager().createReference(callback);

                // get the ID  of the callback
                callbackID = callbackRef.getId();
            }

            // add a runner to our list that will be run on commit
            runners.add(new SqlRunner(runnable, callbackID,
                                      txnProxy.getCurrentOwner()));
        }
        
        @Override
        public void abort(boolean retryable) {
            runners.clear();
        }

        @Override
        public void commit() {
            // execute each runner on a separate thread
            for (SqlRunner runner : runners) {
                executor.submit(runner);
            }
            runners.clear();
        }
    }

    /** Private implementation of {@code TransactionContextFactory}. */
    private class TransactionContextFactoryImpl
            extends TransactionContextFactory<SqlTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, SqlServiceImpl.class.getName());
        }

        /** {@inheritDoc} */
        protected SqlTransactionContext createContext(Transaction txn) {
            return new SqlTransactionContext(txn);
        }
    }

    private class SqlRunner implements Runnable {
        private final SqlRunnable<?> runnable;
        private final BigInteger callbackID;
        private final Identity identity;

        public SqlRunner(SqlRunnable<?> runnable, BigInteger callbackID,
                         Identity identity)
        {
            this.runnable = runnable;
            this.callbackID = callbackID;
            this.identity = identity;
        }

        public void run() {
            Connection connection = connectionLocal.get();
            runnable.run(connection);

            if (callbackID != null) {
                SqlCallbackKernelRunnable kernelRunnable =
                    new SqlCallbackKernelRunnable(callbackID,
                                                  runnable.getResult(),
                                                  runnable.getError());
                transactionScheduler.scheduleTask(kernelRunnable, identity);
            }
        }
    }

    private static class SqlThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r, "SqlFactoryThread");
        }
    }

    private class SqlCallbackKernelRunnable implements KernelRunnable {
        private final BigInteger callbackID;
        private final Object result;
        private final Throwable error;

        public SqlCallbackKernelRunnable(BigInteger callbackID, Object result,
                                         Throwable error)
        {
            this.callbackID = callbackID;
            this.result = result;
            this.error = error;
        }

        public String getBaseTaskType() {
            return SqlServiceImpl.class.getName() + ".SqlCallbackKernelRunnable";
        }

        public void run() throws Exception {
            // create a managed reference from the callback id we were given
            ManagedReference<SqlCallback> callbackRef = (ManagedReference<SqlCallback>)
                    dataService.createReferenceForId(callbackID);

            // execute the callback
            SqlCallback callback = callbackRef.get();
            if (error != null) {
                callback.handleError(error);
            } else {
                callback.handleResult(result);
            }

            // clean up the task in the data store
            if (callback instanceof SqlCallbackWrapper) {
                dataService.removeObject(callback);
            }
        }
    }

    /**
     * Wrapper for SqlCallback implemented as a managed object
     */
    static class SqlCallbackWrapper<V> 
            implements SqlCallback<V>, ManagedObject, Serializable
    {
        private SqlCallback<V> callback;

        public SqlCallbackWrapper(SqlCallback<V> callback) {
            this.callback = callback;
        }

        public void handleResult(V result) {
            callback.handleResult(result);
        }

        public void handleError(Throwable error) {
            callback.handleError(error);
        }
    }
}
