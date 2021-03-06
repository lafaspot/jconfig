package org.commons.jconfig.configloader;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.commons.jconfig.internal.Worker;
import org.commons.jconfig.internal.WorkerException;
import org.commons.jconfig.internal.WorkerExecutorService;
import org.commons.jconfig.internal.WorkerFuture;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;


public class ConfigLoaderWorker implements Worker<Object> {
    private final ConfigLoaderJmx mbean;
    private final Exception cause = null;
    private final WorkerExecutorService executor;
    private long lastUpdateTimeStamp = 0;
    private final static Logger logger = Logger.getLogger(ConfigLoaderWorker.class);

    public ConfigLoaderWorker(final WorkerExecutorService executor, final ConfigLoaderJmx mbean) {
        this.mbean = mbean;
        this.executor = executor;
    }

    private final ConcurrentHashMap<String, WorkerFuture<Object>> vms = new ConcurrentHashMap<String, WorkerFuture<Object>>();

    @Override
    public boolean execute() {
        /* if config sync interval is greater than lastUpdate interval than return false */
        if (mbean.getConfig().getConfigSyncInterval().toMillis() > (System.currentTimeMillis() - lastUpdateTimeStamp)) {
            try {
                // sleep for 2s before checking again.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // Ignore exception
            }
            return false;
        }
        lastUpdateTimeStamp = System.currentTimeMillis();
        // clean up the UpdateWorkers for workers that are done
        for (String key : vms.keySet()) {
            WorkerFuture<Object> future = vms.get(key);
            if (future.isDone() || future.isCancelled()) {
                vms.remove(key);
                logger.info("completed updating vm with new configs: " + key);
            }
        }

        // get all ConfigManager vms and create Update Workers for the new ones
        for (ConfigManagerJvm vm : ConfigManagerJvm.find()) {
            try {
                if (!vms.containsKey(vm.getObjectName().getCanonicalName())) {
                    UpdateVmWorker worker = new UpdateVmWorker(mbean, vm);
                    WorkerFuture<Object> future = executor.submit(worker);
                    vms.put(vm.getObjectName().getCanonicalName(), future);
                    logger.info("start updating vm with new configs: " + vm.getObjectName().getCanonicalName());
                } else {
                    try {
                        vm.close();
                    } catch (VirtualMachineException e) {
                        // ignore exception to allow GC to collect vm object, config to the next jvm
                    }
                }
            } catch (WorkerException e) {
                try {
                    vm.close();
                } catch (VirtualMachineException e1) {
                    logger.error("Failed to Attach to Config Manager vm " + vm.toString(), e);
                }
                logger.error("Failed to Attach to Config Manager vm " + vm.toString(), e);
            }
        }
        return false;
    }

    @Override
    public Exception getCause() {
        return cause;
    }

    @Override
    public boolean hasErrors() {
        return cause != null;
    }

    @Override
    public Object getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
