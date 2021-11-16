package com.github.shenwii;

import com.aliyun.tea.utils.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main
 * @author shenwii
 */
public class Main {
    public static void main(String[] argv) throws Exception {
        ConfigDto configDto = new ConfigDto();
        for(int i = 0; i < argv.length; i++) {
            switch (argv[i]) {
                case "-c":
                case "--config":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto = readConfigFile(argv[i]);
                    break;
                case "-r":
                case "--region":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto.setRegionId(argv[i]);
                    break;
                case "-i":
                case "--key-id":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto.setAccessKeyId(argv[i]);
                    break;
                case "-s":
                case "--key-secret":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto.setAccessKeySecret(argv[i]);
                    break;
                case "-d":
                case "--domain":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto.setDomainName(argv[i]);
                    break;
                case "-t":
                case "--host":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    configDto.setHostRecord(argv[i]);
                    break;
                case "-o":
                case "--timeout":
                    if (++i >= argv.length) {
                        showUsage();
                    }
                    try {
                        configDto.setTimeout(Integer.valueOf(argv[i]));
                    } catch (NumberFormatException ignore) {}
                    break;
                case "-h":
                case "--help":
                default:
                    showUsage();
                    break;
            }
        }
        if(StringUtils.isEmpty(configDto.getRegionId())) {
            System.err.println("RegionId不能为空");
            System.exit(1);
        }
        if(StringUtils.isEmpty(configDto.getAccessKeyId())) {
            System.err.println("AccessKeyId不能为空");
            System.exit(1);
        }
        if(StringUtils.isEmpty(configDto.getAccessKeySecret())) {
            System.err.println("AccessKeySecret不能为空");
            System.exit(1);
        }
        if(StringUtils.isEmpty(configDto.getDomainName())) {
            System.err.println("DomainName不能为空");
            System.exit(1);
        }
        if(StringUtils.isEmpty(configDto.getHostRecord())) {
            System.err.println("HostRecord不能为空");
            System.exit(1);
        }
        final DDnsClient client = new DDnsClient(
                configDto.getAccessKeyId()
                ,configDto.getAccessKeySecret()
                ,configDto.getRegionId()
                ,configDto.getDomainName()
                ,configDto.getHostRecord()
                ,configDto.getTimeout()
        );
        client.updateDomain();
    }

    private static ConfigDto readConfigFile(String filePath) throws IOException {
        try(InputStream is = new FileInputStream(filePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(is, ConfigDto.class);
        }
    }

    /**
     * 打印使用方法
     */
    public static void showUsage() {
        System.err.print("Usage: AliyunDDNS      [OPTION]\n" +
                "  -c, --config config_file      Config file path\n" +
                "  -r, --region region_id        Region Id\n" +
                "  -i, --key-id key_id           Access Key Id\n" +
                "  -s, --key-secret key_secret   Access Key Secret\n" +
                "  -d, --domain domain           Domain Name\n" +
                "  -t, --host host               Host Record\n" +
                "  -h, --help                    Print this help\n");
        System.exit(1);
    }
}
