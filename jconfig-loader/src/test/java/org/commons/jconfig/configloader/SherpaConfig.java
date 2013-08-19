package org.commons.jconfig.configloader;

/**
 * Test SherpaConfig class 
 * 
 * @author aabed
 */
public class SherpaConfig implements SherpaConfigMXBean {

    public SherpaConfig() {
        this.hostName = "error";
        this.ycaAppId = "error";
        this.urlPath = "error";
        this.tableName = "error";
        this.timeout = "error";
        this.retries = "";
        this.port = "";
    }

    public String getHostName() {
        return this.hostName;
    }
    public String  getPort() {
        return this.port;
    }
    public String getUrlPath() {
        return this.urlPath;
    }    
    public String getTimeout() {
        return this.timeout;
    }
    public String getYcaAppId(){
        return this.ycaAppId;
    }
    public String getRetries() {
        return this.retries;
    }
    public String getTableName(){
        return this.tableName;
    }
    
    public synchronized void setHostName(String name) {
        this.hostName = name;
    }
    public synchronized void setPort(String port) {
        this.port = port;
    }
    public synchronized void setUrlPath(String path) {
        this.urlPath = path;
    }
    public synchronized void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    public synchronized void setYcaAppId(String AppId) {
        this.ycaAppId = AppId;
    }
    public synchronized void setRetries(String retries){
        this.retries = retries;
    }
    public synchronized void setTableName(String name){
        this.tableName = name;
    }
    
    public String getLoaderAdapter() {
        return "standard";
    }
    
    private String hostName;
    private String port;
    private String urlPath;
    private String timeout;
    private String ycaAppId;
    private String retries;
    private String tableName;
}