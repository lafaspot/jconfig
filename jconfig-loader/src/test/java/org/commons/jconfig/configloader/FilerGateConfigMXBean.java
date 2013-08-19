package org.commons.jconfig.configloader;

/**
 * Test FilerGateConfig interface 
 * 
 * @author aabed
 */
public interface FilerGateConfigMXBean {
    
    public String getFilerGateServerName();
    public String getFilerGateAppId();
    public String  getFilerGateServerPort();
    public String getFilerGateDirPath();
    public String getFilerGateTimeout();
    public String getFilerGateRetries();
   
    public void setFilerGateServerName(String name);
    public void setFilerGateAppId(String AppId);
    public void setFilerGateServerPort(String port);
    public void setFilerGateDirPath(String path);
    public void setFilerGateTimeout(String timeout);
    public void setFilerGateRetries(String retries);
    
    //Leave this out by design for testing
    //public String getLoaderAdapter();
}
