package com.github.shenwii;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.aliyun.tea.TeaModel;
import com.aliyun.tea.utils.StringUtils;
import com.aliyun.teaopenapi.models.Config;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DDnsClient {

    private final Client client;
    private final int AF_INET = 2;
    private final int AF_INET6 = 10;
    private final String IP_API_URL = "https://api64.ipify.org?format=text";
    final private String domainName;
    final private String hostRecord;
    final private int timeout;
    final private boolean useIpv6;

    public DDnsClient(String accessKeyId
            , String accessKeySecret
            , String regionId
            , String domainName
            , String hostRecord
            , int timeout
            , boolean useIpv6) throws Exception {
        Config config = new Config();
        config.accessKeyId = accessKeyId;
        config.accessKeySecret = accessKeySecret;
        config.regionId = regionId;
        config.connectTimeout = timeout * 1000;
        config.readTimeout = timeout * 1000;
        this.domainName = domainName;
        this.hostRecord = hostRecord;
        this.timeout = timeout;
        this.useIpv6 = useIpv6;
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
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .dns(hostname -> {
                    if(StringUtils.isEmpty(hostname))
                        return Dns.SYSTEM.lookup(hostname);
                    List<InetAddress> inetAddressList = new ArrayList<>();
                    InetAddress[] inetAddresses = InetAddress.getAllByName(hostname);
                    for (InetAddress inetAddress : inetAddresses) {
                        if(family == AF_INET) {
                            if(inetAddress instanceof Inet4Address)
                                inetAddressList.add(inetAddress);
                        } else if(family == AF_INET6) {
                            if(inetAddress instanceof Inet6Address)
                                inetAddressList.add(inetAddress);
                        }
                    }
                    return inetAddressList;
                })
                .build();
        Request request = new Request.Builder()
                .url(IP_API_URL)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (ConnectException ignore) {
        }
        catch (IOException e) {
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
            System.err.println("??????????????????");
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
            System.err.println("??????????????????");
        }
    }

    public void updateDomain() throws Exception {
        DnsRecordDto ipv4DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET);
        DnsRecordDto ipv6DnsRecordDto = getDnsRecordIpAddress(domainName, hostRecord, AF_INET6);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + "\t????????????");
        System.out.println(sdf.format(new Date()) + "\t??????IPv4??????");
        String curIpv4Address = getCurrentIpAddress(AF_INET);
        if(!StringUtils.isEmpty(curIpv4Address)) {
            System.out.println(sdf.format(new Date()) + "\t??????IPv4????????????" + curIpv4Address);
            if(ipv4DnsRecordDto == null) {
                System.out.println(sdf.format(new Date()) + "\t?????????????????????????????????");
                System.out.println(sdf.format(new Date()) + "\t??????IPv4??????");
                insertDnsRecord(curIpv4Address, AF_INET);
            } else {
                System.out.println(sdf.format(new Date()) + "\t???????????????????????????" + ipv4DnsRecordDto.getIpAddress());
                if(!curIpv4Address.equals(ipv4DnsRecordDto.getIpAddress())) {
                    System.out.println(sdf.format(new Date()) + "\t?????????????????????IPv4??????");
                    updateDnsRecord(ipv4DnsRecordDto.getRecordId(), curIpv4Address, AF_INET);
                } else {
                    System.out.println(sdf.format(new Date()) + "\t???????????????????????????");
                }
            }
        } else {
            System.err.println(sdf.format(new Date()) + "\t??????IPv4????????????");
            System.err.println(sdf.format(new Date()) + "\t????????????????????????");
        }
        if(useIpv6)
        {
            System.out.println(sdf.format(new Date()) + "\t??????IPv6??????");
            String curIpv6Address = getCurrentIpAddress(AF_INET6);
            if(!StringUtils.isEmpty(curIpv6Address)) {
                System.out.println(sdf.format(new Date()) + "\t??????IPv6????????????" + curIpv6Address);
                if(ipv6DnsRecordDto == null) {
                    System.out.println(sdf.format(new Date()) + "\t?????????????????????????????????");
                    System.out.println(sdf.format(new Date()) + "\t??????IPv6??????");
                    insertDnsRecord(curIpv6Address, AF_INET6);
                } else {
                    System.out.println(sdf.format(new Date()) + "\t???????????????????????????" + ipv6DnsRecordDto.getIpAddress());
                    if(!curIpv6Address.equals(ipv6DnsRecordDto.getIpAddress())) {
                        System.out.println(sdf.format(new Date()) + "\t?????????????????????IPv6??????");
                        updateDnsRecord(ipv6DnsRecordDto.getRecordId(), curIpv6Address, AF_INET6);
                    } else {
                        System.out.println(sdf.format(new Date()) + "\t???????????????????????????");
                    }
                }
            } else {
                System.err.println(sdf.format(new Date()) + "\t??????IPv6????????????");
                System.err.println(sdf.format(new Date()) + "\t????????????????????????");
                System.err.println(sdf.format(new Date()) + "\t??????????????????????????????????????????IPv6");
            }
        }
        System.out.println(sdf.format(new Date()) + "\t????????????");
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
