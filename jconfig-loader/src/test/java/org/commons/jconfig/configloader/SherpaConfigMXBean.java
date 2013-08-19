package org.commons.jconfig.configloader;

/**
 * Test SherpaConfig interface 
 * 
 * @author aabed
 */
public interface SherpaConfigMXBean {
    
    public String getHostName();
    public String  getPort();
    public String getUrlPath();
    public String getTimeout();
    public String getYcaAppId();
    public String getRetries();
    public String getTableName();
    
    public void setHostName(String name);
    public void setPort(String port);
    public void setUrlPath(String path);
    public void setTimeout(String timeout);
    public void setYcaAppId(String AppId);
    public void setRetries(String retries);
    public void setTableName(String tableName);
   
    public String getLoaderAdapter();
}