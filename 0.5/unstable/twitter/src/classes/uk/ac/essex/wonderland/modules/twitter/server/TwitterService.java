/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *  
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.twitter.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.ac.essex.wonderland.modules.twitter.server.TwitterManager.TwitterQueryListener;
import uk.ac.essex.wonderland.modules.twitter.server.TwitterManager.TwitterQueryResult;

/**
 *
 * @author Bernard Horan, bernard@essex.ac.uk
 */
public class TwitterService extends AbstractService {

    /** The name of this class. */
    private static final String NAME = TwitterService.class.getName();
    /** The package name. */
    private static final String PKG_NAME = "uk.ac.essex.wonderland.modules.twitter.server";
    /** The logger for this class. */
    private static final LoggerWrapper twitterLogger =
            new LoggerWrapper(Logger.getLogger(PKG_NAME));
    /** The name of the version key. */
    private static final String VERSION_KEY = PKG_NAME + ".service.version";
    /** The major version. */
    private static final int MAJOR_VERSION = 1;
    /** The minor version. */
    private static final int MINOR_VERSION = 0;
    /** manages the context of the current transaction */
    private TransactionContextFactory<TwitterTransactionContext> ctxFactory;
    /** executes the actual remote calls */
    private ExecutorService executor;

    public TwitterService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {
        super(props, registry, proxy, twitterLogger);


        twitterLogger.log(Level.CONFIG, "Creating TwitterService properties:{0}",
                props);

        // create the transaction context factory
        ctxFactory = new TransactionContextFactoryImpl(proxy);

        try {
            /*
             * Check service version.
             */
            transactionScheduler.runTask(new KernelRunnable() {

                public String getBaseTaskType() {
                    return NAME + ".VersionCheckRunner";
                }

                public void run() {
                    checkServiceVersion(
                            VERSION_KEY, MAJOR_VERSION, MINOR_VERSION);
                }
            }, taskOwner);
        } catch (Exception ex) {
            twitterLogger.logThrow(Level.SEVERE, ex, "Error creating service");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
        // create the executor thread
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void doShutdown() {
        // stop the executor, attempting an orderly shutdown
        boolean shutdown = false;
        executor.shutdown();

        try {
            shutdown = executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        if (!shutdown) {
            List<Runnable> leftover = executor.shutdownNow();
            twitterLogger.log(Level.WARNING, "Terminating executor with tasks in"
                    + "  progress: " + leftover);
        }
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
            Version currentVersion) {
        throw new IllegalStateException(
                "unable to convert version:" + oldVersion
                + " to current version:" + currentVersion);
    }

    void runQuery(Twitter twitter, Query aQuery, TwitterQueryListener listener) {
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedTwitterQueryWrapper(listener);
        }
        // create a reference to the listener
        ManagedReference<TwitterQueryListener> scl =
                dataService.createReference(listener);
        // now add the query request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        TwitterQuery l = new TwitterQuery(twitter, aQuery, scl.getId());
        ctxFactory.joinTransaction().add(l);
    }

    private class TwitterQuery implements Runnable {

        private final BigInteger listenerID;
        private final Twitter twitter;
        private final Query query;

        private TwitterQuery(Twitter twitter, Query aQuery, BigInteger id) {
            this.twitter = twitter;
            this.query = aQuery;
            listenerID = id;
        }

        public void run() {
            TwitterQueryResultImpl result;
            try {
                QueryResult queryResult = twitter.search(query);
                result = new TwitterQueryResultImpl(queryResult);
            } catch (TwitterException ex) {
                result = new TwitterQueryResultImpl(null, "Failed to perform query", ex);
            }

            // notify the listener
            NotifyTwitterQueryListener notify =
                    new NotifyTwitterQueryListener(listenerID, result);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex) {
                twitterLogger.logThrow(Level.WARNING, ex, "Error calling listener");
            }
        }
    }

    /**
     * Transaction state
     */
    private class TwitterTransactionContext extends TransactionContext {

        private List<Runnable> changes;

        public TwitterTransactionContext(Transaction txn) {
            super(txn);
            changes = new LinkedList<Runnable>();
        }

        public void add(Runnable change) {
            changes.add(change);
        }

        @Override
        public void abort(boolean retryable) {
            twitterLogger.getLogger().log(Level.SEVERE, "retryable: {0}", retryable);
            changes.clear();
        }

        @Override
        public void commit() {
            for (Runnable r : changes) {
                executor.submit(r);
            }

            changes.clear();
            isCommitted = true;
        }
    }

    /** Private implementation of {@code TransactionContextFactory}. */
    private class TransactionContextFactoryImpl
            extends TransactionContextFactory<TwitterTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, NAME);
        }

        /** {@inheritDoc} */
        protected TwitterTransactionContext createContext(Transaction txn) {
            return new TwitterTransactionContext(txn);
        }
    }

    /**
     * A wrapper around the TwitterQueryListener as a managed object.
     * This assumes a serializable TwitterQueryListener
     */
    private static class ManagedTwitterQueryWrapper
            implements TwitterQueryListener, ManagedObject, Serializable {

        private TwitterQueryListener wrapped;

        public ManagedTwitterQueryWrapper(TwitterQueryListener listener) {
            wrapped = listener;
        }

        public void twitterQueryResult(TwitterQueryResult result) {
            wrapped.twitterQueryResult(result);
        }
    }

    /**
     * The result of running a query
     */
    class TwitterQueryResultImpl implements TwitterQueryResult {

        private boolean success;
        private String failureReason;
        private Throwable failureCause;
        private final QueryResult queryResult;

        public TwitterQueryResultImpl(QueryResult queryResult) {
            this.queryResult = queryResult;
            this.success = true;
        }

        public TwitterQueryResultImpl(QueryResult queryResult, String failureReason,
                Throwable failureCause) {
            this.success = false;
            this.queryResult = queryResult;
            this.failureReason = failureReason;
            this.failureCause = failureCause;
        }

        public boolean isSuccess() {
            return success;
        }

        public QueryResult getQueryResult() {
            return queryResult;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public Throwable getFailureCause() {
            return failureCause;
        }
    }

    /**
     * A task to notify a TwitterQueryListener
     */
    private class NotifyTwitterQueryListener implements KernelRunnable {

        private BigInteger listenerID;
        private TwitterQueryResult result;

        public NotifyTwitterQueryListener(BigInteger listenerID, TwitterQueryResult result) {
            this.listenerID = listenerID;
            this.result = result;
        }

        public String getBaseTaskType() {
            return NAME + ".TWITTER_QUERY_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            TwitterQueryListener l =
                    (TwitterQueryListener) lr.get();

            try {
                l.twitterQueryResult(result);
            } finally {
                // clean up
                if (l instanceof ManagedTwitterQueryWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }
}
