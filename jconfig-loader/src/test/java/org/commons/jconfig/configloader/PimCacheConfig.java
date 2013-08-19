package org.commons.jconfig.configloader;

/**
 * Test PimCacheConfig class
 * 
 * @author aabed
 */
public class PimCacheConfig implements PimCacheConfigMXBean {

    @Override
    public String getPimLmaSendTimeout() {
        return pimLmaSendTimeout;
    }

    @Override
    public String getPimLmaRecvTimeout() {
        return pimLmaRecvTimeout;
    }

    @Override
    public String getPimEmdSendTimeout() {
        return pimEmdSendTimeout;
    }

    @Override
    public String getPimEmdRecvTimeout() {
        return pimEmdRecvTimeout;
    }
    @Override
    public String getPimRetry() {
        return pimRetry;
    }

    @Override
    public void setPimLmaSendTimeout(final String msec) {
        pimLmaSendTimeout = msec;
    }
    @Override
    public void setPimLmaRecvTimeout(final String msec) {
        pimLmaRecvTimeout = msec;
    }
    @Override
    public void setPimEmdSendTimeout(final String msec) {
        pimEmdSendTimeout = msec;
    }
    @Override
    public void setPimEmdRecvTimeout(final String msec) {
        pimEmdRecvTimeout = msec;
    }
    @Override
    public void setPimRetry(final String retry) {
        pimRetry = retry;
    }

    @Override
    public String getLoaderAdapter() {
        return "standard";
    }

    private String pimLmaSendTimeout;
    private String pimLmaRecvTimeout;
    private String pimEmdSendTimeout;
    private String pimEmdRecvTimeout;
    private String pimRetry;
}
