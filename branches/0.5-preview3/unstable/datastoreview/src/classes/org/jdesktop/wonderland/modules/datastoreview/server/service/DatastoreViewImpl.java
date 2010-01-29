/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.datastoreview.server.service;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jkaplan
 */
public class DatastoreViewImpl extends AbstractService {
    /** The logger for this class. */
    private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(DatastoreViewImpl.class.getName()));

    /** the minimum id to scan */
    private static final String MIN_ID_PROP = "datastoreview.min";

    /** the maximum id to scan -- if this is null, no scanning will happen */
    private static final String MAX_ID_PROP = "datastoreview.max";

    /** a map from class to the count for that class */
    private final Map<Class, Integer> counts = new HashMap<Class, Integer>();

    /** minimum id that was successful */
    private BigInteger minId;

    /** maximum id that was successful */
    private BigInteger maxId;

    /** manages the context of the current transaction */
    private TransactionContextFactory<IncrementTransactionContext> ctxFactory;

    public DatastoreViewImpl(Properties prop,
                             ComponentRegistry registry,
                             TransactionProxy transactionProxy)
    {
        super (prop, registry, transactionProxy, logger);

        ctxFactory = new TransactionContextFactoryImpl(transactionProxy);
    }

    @Override
    public String getName() {
        return DatastoreViewImpl.class.getName();
    }

    @Override
    protected void doReady() throws Exception {
        // figure out the minimum and maximum values to scan
        String minIdStr = System.getProperty(MIN_ID_PROP, "0");
        String maxIdStr = System.getProperty(MAX_ID_PROP);

        // if the maximum id isn't specified, don't scan
        if (maxIdStr == null) {
            logger.log(Level.INFO, "No maximum id set, not scanning");
            return;
        }

        // find the values
        BigInteger min = new BigInteger(minIdStr);
        BigInteger max = new BigInteger(maxIdStr);

        // set the min and max counter initial values
        minId = max;
        maxId = min;

        // scan
        BigInteger cur = min;
        while (cur.compareTo(max) == -1) {
            transactionScheduler.runTask(new PrintObjectTask(cur), taskOwner);
            cur = cur.add(BigInteger.ONE);
        }

        // sort the results
        List<SortedClass> res = new ArrayList<SortedClass>(counts.size());
        for (Map.Entry<Class, Integer> e : counts.entrySet()) {
            res.add(new SortedClass(e.getKey(), e.getValue()));
        }
        Collections.sort(res);

        long count = 0;
        StringBuffer out = new StringBuffer("Object report:\n");
        for (SortedClass sc : res) {
            out.append(String.format(" %5d %s%n", sc.count, sc.clazz.getName()));
            count += sc.count;
        }
        
        out.append("Processed " + count + " objects.  MinId = " + 
                   minId + " MaxId = " + maxId + "\n");

        logger.log(Level.INFO, out.toString());
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

    private void increment(Class clazz, Integer count) {
        Integer cur = counts.get(clazz);
        if (cur == null) {
            cur = count;
        } else {
            cur = Integer.valueOf(cur.intValue() + count.intValue());
        }

        counts.put(clazz, cur);
    }

    private void minId(BigInteger minId) {
        if (minId.compareTo(this.minId) == -1) {
            this.minId = minId;
        }
    }

    private void maxId(BigInteger maxId) {
        if (maxId.compareTo(this.maxId) == 1) {
            this.maxId = maxId;
        }
    }

    private class PrintObjectTask implements KernelRunnable {
        private final BigInteger id;

        public PrintObjectTask(BigInteger id) {
            this.id = id;
        }

        public String getBaseTaskType() {
            return PrintObjectTask.class.getName();
        }

        public void run() throws Exception {
            try {
                ManagedReference ref = dataService.createReferenceForId(id);

                ctxFactory.joinTransaction().increment(ref.get().getClass());
                ctxFactory.joinTransaction().id(id);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Object " + id + ": " + ref.get().getClass() +
                               " (" + ref.get().toString() + ")");
                }
            } catch (ObjectNotFoundException onfe) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "No object for id " + id);
                }
            }
        }
    }

    /**
     * Transaction state
     */
    private class IncrementTransactionContext extends TransactionContext {
        private final Map<Class, Integer> increments =
                new HashMap<Class, Integer>();

        private BigInteger minId;
        private BigInteger maxId;

        public IncrementTransactionContext(Transaction txn) {
            super (txn);
        }

        public void increment(Class clazz) {
            Integer i = increments.get(clazz);
            if (i == null) {
                i = Integer.valueOf(1);
            } else {
                i = Integer.valueOf(i.intValue() + 1);
            }

            increments.put(clazz, i);
        }

        public void id(BigInteger id) {
            if (minId == null || id.compareTo(minId) == -1) {
                minId = id;
            }

            if (maxId == null || id.compareTo(maxId) == 1) {
                maxId = id;
            }
        }

        @Override
        public void abort(boolean retryable) {
            increments.clear();
        }

        @Override
        public void commit() {
            for (Map.Entry<Class, Integer> e : increments.entrySet()) {
                DatastoreViewImpl.this.increment(e.getKey(), e.getValue());
            }

            if (minId != null) {
                DatastoreViewImpl.this.minId(minId);
            }

            if (maxId != null) {
                DatastoreViewImpl.this.maxId(maxId);
            }

            isCommitted = true;
        }
    }

    /** Private implementation of {@code TransactionContextFactory}. */
    private class TransactionContextFactoryImpl
            extends TransactionContextFactory<IncrementTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, DatastoreViewImpl.class.getName());
        }

        /** {@inheritDoc} */
        protected IncrementTransactionContext createContext(Transaction txn) {
            return new IncrementTransactionContext(txn);
        }
    }

    private class SortedClass implements Comparable<SortedClass> {
        private Class clazz;
        private int count;

        public SortedClass(Class clazz, int count) {
            this.clazz = clazz;
            this.count = count;
        }

        public int compareTo(SortedClass o) {
            if (count > o.count) {
                return -1;
            } else if (count < o.count) {
                return 1;
            } else {
                return clazz.getName().compareTo(o.clazz.getName());
            }
        }
    }
}
