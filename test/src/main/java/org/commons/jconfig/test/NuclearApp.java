package org.commons.jconfig.test;

import java.util.Random;

import org.commons.jconfig.config.ConfigContext;
import org.commons.jconfig.config.ConfigContext.Entry;
import org.commons.jconfig.config.ConfigManager;

/**
 * If only NuclearApp is running and no ConfigLoader application is running than
 * NuclearApp with run with default configurations which are defined in
 * NuclearConfig class.
 * 
 * <pre>
 * <code>
 * Default Configuration from {@link NuclearConfig} class.
    HostProxy: proxy.mail.yahoo.com
    HostUrl: mail.yahoo.com
    CacheSize: 90 mb
    HostPort: 80.0 
 * </code>
 * 
 * <pre>
 * 
 * Once you start ConfigLoader application, NuclearApp will receive config values from ConfigLoader
 * and it will start using those configuration values for it's task.
 */
public class NuclearApp {
    public static void main(String args[]) throws InterruptedException {
        NuclearApp app = new NuclearApp();
        app.runEvery5Sec();
    }

    public void runEvery5Sec() throws InterruptedException {
        Random number = new Random();
        while (true) {
            Thread.sleep(5000);
            String subset1 = Integer.toString(number.nextInt(10));
            ConfigContext context = new ConfigContext(new Entry("SUBSET1",
                    subset1));
            System.out.println("\n\n\nRequest for subset: " + subset1);
            NuclearConfig nuclearConfig = ConfigManager.INSTANCE.getConfig(
                    NuclearConfig.class, context);
            System.out.println("HostProxy: " + nuclearConfig.getHostProxy());
            System.out.println("HostUrl: " + nuclearConfig.getHostUrl());
            System.out.println("CacheSize: " + nuclearConfig.getCacheSize());
            System.out.println("HostPort: " + nuclearConfig.getHostPort());

            if (nuclearConfig.getHostPort().intValue() == 0)
                break;
        }

    }
}
