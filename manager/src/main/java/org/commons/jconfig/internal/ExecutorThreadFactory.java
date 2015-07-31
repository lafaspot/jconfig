package org.commons.jconfig.internal;

import java.util.concurrent.ThreadFactory;

/**
 * Create daemon threads
 * 
 * @author lafa
 * 
 */
public class ExecutorThreadFactory implements ThreadFactory {

    private ThreadGroup mGroup = null;
    private volatile long mCount = 1;

    /**
     * takes a thread group name as an argument.
     * 
     * @param groupName
     */
    public ExecutorThreadFactory(final String groupName) {
        mGroup = new ThreadGroup(groupName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable target) {
        // Threads with 4k stack
        Thread t = new Thread(mGroup, target, mGroup.getName() + "-" + mCount++);
        // jvm should not wait for these thread before it exits
        t.setDaemon(true);
        return t;
    }
}
