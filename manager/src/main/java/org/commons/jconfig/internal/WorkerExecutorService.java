package org.commons.jconfig.internal;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Implements a generic executor Service, where class that implement the
 * interface worker can be submitted for execution.
 * <code>
 * WorkerExecutorService executor = new WorkerExecutorService();
 * WorkerFuture<String> future = executor.submit(new ReverseStringWorker("12345"));
 * String reversedString = future.get();
 * executor.shutdown();
 * </code>
 * 
 * @author lafa
 * 
 */
@ThreadSafe
public class WorkerExecutorService {

    private final String mName;
    private final ExecutorThreadFactory mThreadFactory;

    // private final ExecutorThreadFactory mThreadFactoryBoss = new
    // ExecutorThreadFactory("async worker boss thread");
    private final ExecutorService mScheduler;

    // private final ScheduledExecutorService mBossScheduler;
    // private final ScheduledFuture<?> bossFuture;
    // private final long mDelay;
    // private final TimeUnit mUnit;
    // private final List<WorkerCallable<?>> mWorkers = new
    // CopyOnWriteArrayList<WorkerCallable<?>>();

    /**
     * Creates a WorkerExecutorService that creates new threads as needed, but
     * will reuse previously constructed threads when available.
     */
    public WorkerExecutorService(int nThreads) {
        this("WorkerExecutorService thread", nThreads);
    }

    /**
     * Creates a WorkerExecutorService that creates new threads as needed, but
     * will reuse previously constructed threads when available.
     */
    /**
     * @param clazz
     */
    public WorkerExecutorService(final Class<?> clazz, int nThreads) {
        this(clazz.getName(), nThreads);
    }

    /**
     * Creates a WorkerExecutorService that creates new threads as needed, but
     * will reuse previously constructed threads when available.
     */
    /**
     * @param name
     */
    public WorkerExecutorService(final String name, int nThreads) {
        mName = name;
        mThreadFactory = new ExecutorThreadFactory("WorkerExecutorService thread for " + mName);
        mScheduler = Executors.newFixedThreadPool(nThreads, mThreadFactory);
    }

    /**
     * Submits a value-returning worker for execution and returns a WorkerFuture
     * representing the pending results of the task. The Future's get method
     * will return the task's result upon successful completion. If you would
     * like to immediately block waiting for a task, you can use constructions
     * of the form <code> result = exec.submit(aWorker).get(); </code>
     * 
     * Throws:
     * 
     * RejectedExecutionException - if the worker cannot be scheduled for
     * execution
     * 
     * NullPointerException - if the worker is null
     * 
     * @param <V>
     * @param worker
     * @return
     */
    public <V> WorkerFuture<V> submit(final Worker<V> worker) {
        Future<V> fut = mScheduler.submit(new WorkerCallable<V>(worker, this));
        return new WorkerFutureImpl<V>(fut);
    }

    /**
     * Initiates an shutdown in which previously submitted tasks are executed,
     * but no new tasks will be accepted.Invocation has no additional effect if
     * already shut down.
     * 
     * Throws:
     * 
     * SecurityException - if a security manager exists and shutting down this
     * ExecutorService may manipulate threads that the caller is not permitted
     * to modify because it does not hold
     * java.lang.RuntimePermission("modifyThread"), or the security manager's
     * checkAccess method denies access.
     */
    public void shutdown() {
        if (mIsShutdown) {
            return;
        }
        mIsShutdown = true;
        mScheduler.shutdown();
        try {
            mScheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignore
        }
        if (!mScheduler.isShutdown() || !mScheduler.isTerminated()) {
            mScheduler.shutdownNow();
            try {
                mScheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        if (!mScheduler.isShutdown() || !mScheduler.isTerminated()) {
            throw new RuntimeException("WorkerExecutorService failed to shutdown.");
        }
    }

    private volatile boolean mIsShutdown = false;

    /**
     * Returns true if this executor has been shut down.
     * 
     * @return
     */
    public boolean isShutdown() {
        return mIsShutdown;
    }
}
