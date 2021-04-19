package com.github.shenwii;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.aliyun.tea.TeaModel;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DDnsClient {

    private final Client client;
    private DnsRecordDto ipv4DnsRecordDto = null;
    private DnsRecordDto ipv6DnsRecordDto = null;
    private final int AF_INET = 2;
    private final int AF_INET6 = 10;
    private String domainName;
    private String hostRecord;

    public DDnsClient(String accessKeyId, String accessKeySecret, String regionId, String domainName, String hostRecord) throws Exception {
        Config config = new Config();
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.regionId = regionId;
        this.domainName = domainName;
        this.hostRecord = hostRecord;
        client = new Client(config);
        ipv4DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET);
        ipv6DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET6);
    }

    private DnsRecordDto getDnsRecordIpAddress(String domainName, String hostRecord, int family) throws Exception {
        DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
        request.domainName = domainName;
        request.RRKeyWord = hostRecord;
        if(family == AF_INET)
            request.type = "A";
        else
            request.type = "AAAA";
        DescribeDomainRecordsResponse resp = client.describeDomainRecords(request);
        if (com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(resp)) || resp.body.domainRecords.record.size() == 0 || com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(resp.body.domainRecords.record.get(0)))) {
            return null;
        }
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record = resp.body.domainRecords.record.get(0);
        return new DnsRecordDto(record.recordId, record.value);
    }

    private String getCurrentIpAddress(int family) {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(family == AF_INET? "https://api.ipify.org?format=text": "https://api64.ipify.org?format=text"))
                .GET().build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                String ipAddrStr = response.body();
                if(family == AF_INET6) {
                    if(InetAddress.getByName(ipAddrStr) instanceof Inet6Address)
                        return ipAddrStr;
                    return null;
                }
                return ipAddrStr;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private DnsRecordDto insertDnsRecord(String ipAddrStr, int family) throws Exception {
        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.domainName = domainName;
        request.RR = hostRecord;
        if(family == AF_INET)
            request.type = "A";
        else
            request.type = "AAAA";
        request.value = ipAddrStr;
        AddDomainRecordResponse response = client.addDomainRecord(request);
        if (com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(response))) {
            System.err.println("插入记录失败");
            return null;
        }
        return new DnsRecordDto(response.body.recordId, ipAddrStr);
    }

    private void updateDnsRecord(DnsRecordDto dnsRecordDto, String ipAddrStr, int family) throws Exception {
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
        request.RR = hostRecord;
        if(family == AF_INET)
            request.type = "A";
        else
            request.type = "AAAA";
        request.value = ipAddrStr;
        request.recordId = dnsRecordDto.getRecordId();
        UpdateDomainRecordResponse response = client.updateDomainRecord(request);
        if (com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(response))) {
            System.err.println("更新记录失败");
        } else {
            dnsRecordDto.setIpAddress(ipAddrStr);
        }
    }

    public void updateDomain() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + "\t开始任务");
        String curIpv4Addr = getCurrentIpAddress(AF_INET);
        if(curIpv4Addr != null) {
            if(ipv4DnsRecordDto == null) {
                System.out.println(sdf.format(new Date()) + "\t插入IPv4记录");
                ipv4DnsRecordDto = insertDnsRecord(curIpv4Addr, AF_INET);
            } else {
                if(!curIpv4Addr.equals(ipv4DnsRecordDto.getIpAddress()))
                {
                    System.out.println(sdf.format(new Date()) + "\t更新IPv4记录");
                    updateDnsRecord(ipv4DnsRecordDto, curIpv4Addr, AF_INET);
                }
            }
        }
        String curIpv6Addr = getCurrentIpAddress(AF_INET6);
        if(curIpv6Addr != null) {
            if(ipv6DnsRecordDto == null) {
                System.out.println(sdf.format(new Date()) + "\t插入IPv6记录");
                ipv6DnsRecordDto = insertDnsRecord(curIpv6Addr, AF_INET6);
            } else {
                if(!curIpv6Addr.equals(ipv6DnsRecordDto.getIpAddress()))
                {
                    System.out.println(sdf.format(new Date()) + "\t更新IPv6记录");
                    updateDnsRecord(ipv6DnsRecordDto, curIpv6Addr, AF_INET6);
                }
            }
        }
        System.out.println(sdf.format(new Date()) + "\t结束任务");
    }
}

class DnsRecordDto {
    private String recordId;
    private String ipAddress;

    public DnsRecordDto() {
    }

    public DnsRecordDto(String recordId, String ipAddress) {
        this.recordId = recordId;
        this.ipAddress = ipAddress;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
