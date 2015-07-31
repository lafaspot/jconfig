package org.commons.jconfig.internal;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @see WorkerFuture
 * @author lafa
 * 
 * @param <T>
 */
public class WorkerFutureImpl<T> implements WorkerFuture<T> {
    private final Future<T> mFuture;

    /**
     * Constructs a WorkerFutureImpl with a Future. Constructor is called by the
     * WorkerExecutorService.
     * 
     * @param future
     */
    public WorkerFutureImpl(final Future<T> future) {
        mFuture = future;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return mFuture.cancel(mayInterruptIfRunning);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return mFuture.isCancelled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        return mFuture.isDone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
    TimeoutException {
        return mFuture.get(timeout, unit);
    }

}
