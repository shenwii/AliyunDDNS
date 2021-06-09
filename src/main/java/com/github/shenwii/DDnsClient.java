package com.github.shenwii;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.aliyun.tea.TeaModel;
import com.aliyun.teaopenapi.models.Config;

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
    private final int AF_INET = 2;
    private final int AF_INET6 = 10;
    final private String domainName;
    final private String hostRecord;

    public DDnsClient(String accessKeyId, String accessKeySecret, String regionId, String domainName, String hostRecord) throws Exception {
        Config config = new Config();
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.regionId = regionId;
        this.domainName = domainName;
        this.hostRecord = hostRecord;
        client = new Client(config);
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(family == AF_INET? "https://api.ipify.org?format=text": "https://api64.ipify.org?format=text"))
                .GET().build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                String ipAddressStr = response.body();
                if(family == AF_INET6) {
                    if(InetAddress.getByName(ipAddressStr) instanceof Inet6Address)
                        return ipAddressStr;
                    return null;
                }
                return ipAddressStr;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private void insertDnsRecord(String ipAddressStr, int family) throws Exception {
        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.domainName = domainName;
        request.RR = hostRecord;
        if(family == AF_INET)
            request.type = "A";
        else
            request.type = "AAAA";
        request.value = ipAddressStr;
        AddDomainRecordResponse response = client.addDomainRecord(request);
        if (com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(response))) {
            System.err.println("插入记录失败");
        }
    }

    private void updateDnsRecord(String recordId, String ipAddressStr, int family) throws Exception {
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
        request.RR = hostRecord;
        if(family == AF_INET)
            request.type = "A";
        else
            request.type = "AAAA";
        request.value = ipAddressStr;
        request.recordId = recordId;
        UpdateDomainRecordResponse response = client.updateDomainRecord(request);
        if (com.aliyun.teautil.Common.isUnset(TeaModel.buildMap(response))) {
            System.err.println("更新记录失败");
        }
    }

    public void updateDomain() throws Exception {
        DnsRecordDto ipv4DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET);
        DnsRecordDto ipv6DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET6);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + "\t开始任务");
        String curIpv4Address = getCurrentIpAddress(AF_INET);
        if(curIpv4Address != null) {
            if(ipv4DnsRecordDto == null) {
                System.out.println(sdf.format(new Date()) + "\t插入IPv4记录");
                insertDnsRecord(curIpv4Address, AF_INET);
            } else {
                if(!curIpv4Address.equals(ipv4DnsRecordDto.getIpAddress()))
                {
                    System.out.println(sdf.format(new Date()) + "\t更新IPv4记录");
                    updateDnsRecord(ipv4DnsRecordDto.getRecordId(), curIpv4Address, AF_INET);
                }
            }
        }
        String curIpv6Address = getCurrentIpAddress(AF_INET6);
        if(curIpv6Address != null) {
            if(ipv6DnsRecordDto == null) {
                System.out.println(sdf.format(new Date()) + "\t插入IPv6记录");
                insertDnsRecord(curIpv6Address, AF_INET6);
            } else {
                if(!curIpv6Address.equals(ipv6DnsRecordDto.getIpAddress()))
                {
                    System.out.println(sdf.format(new Date()) + "\t更新IPv6记录");
                    updateDnsRecord(ipv6DnsRecordDto.getRecordId(), curIpv6Address, AF_INET6);
                }
            }
        }
        System.out.println(sdf.format(new Date()) + "\t结束任务");
    }
}

class DnsRecordDto {
    final private String recordId;
    final private String ipAddress;

    public DnsRecordDto(String recordId, String ipAddress) {
        this.recordId = recordId;
        this.ipAddress = ipAddress;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
