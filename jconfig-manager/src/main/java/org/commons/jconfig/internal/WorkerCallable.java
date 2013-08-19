package org.commons.jconfig.internal;


import java.util.concurrent.Callable;


/**
 * Internal Class used by WorkerExecutor Service, to manage worker in a safe
 * reliable way.
 * 
 * @author lafa
 * 
 * @param <V>
 */
public class WorkerCallable<V> implements Callable<V> {

    private final Worker<V> mWorker;
    private final WorkerExecutorService mExecutor;

    public WorkerCallable(final Worker<V> worker, final WorkerExecutorService executor) {
        if (worker == null) {
            throw new NullPointerException("worker is null");
        }
        mWorker = worker;
        mExecutor = executor;
    }

    @Override
    public V call() throws Exception {
        boolean done = false;
        while (!done && !mExecutor.isShutdown()) {
            done = mWorker.execute();
        }
        if (mWorker.hasErrors()) {
            throw mWorker.getCause();
        }

        return mWorker.getData();
    }

}
