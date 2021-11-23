package com.github.shenwii;

/**
 * ConfigDto
 * @author shenwii
 */
public class ConfigDto {
    /** 阿里云的区域ID */
    private String regionId = null;
    /** 阿里云的密钥ID */
    private String accessKeyId = null;
    /** 阿里云的密钥 */
    private String accessKeySecret = null;
    /** 域名 */
    private String domainName = null;
    /** 主机记录 */
    private String hostRecord = null;
    /** 超时 */
    private Integer timeout = 60;
    /** 是否使用IPv6 */
    private Boolean useIpv6 = true;

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getHostRecord() {
        return hostRecord;
    }

    public void setHostRecord(String hostRecord) {
        this.hostRecord = hostRecord;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getUseIpv6() {
        return useIpv6;
    }

    public void setUseIpv6(Boolean useIpv6) {
        this.useIpv6 = useIpv6;
    }
}
