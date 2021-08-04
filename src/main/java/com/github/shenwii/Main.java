package com.github.shenwii;

import com.aliyun.tea.utils.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main
 * @author shenwii
 */
public class Main {
    public static void main(String[] argv) throws Exception {
        ConfigDto configDto = new ConfigDto();
        for(int i = 0; i < argv.length; i++) {
            if(argv[i].equals("-c") || argv[i].equals("--config")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto = readConfigFile(argv[i]);
            } else if(argv[i].equals("-h") || argv[i].equals("--help")) {
                showUsage();
            } else if(argv[i].equals("-r") || argv[i].equals("--region")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto.setRegionId(argv[i]);
            } else if(argv[i].equals("-i") || argv[i].equals("--key-id")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto.setAccessKeyId(argv[i]);
            } else if(argv[i].equals("-s") || argv[i].equals("--key-secret")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto.setAccessKeySecret(argv[i]);
            } else if(argv[i].equals("-d") || argv[i].equals("--domain")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto.setDomainName(argv[i]);
            } else if(argv[i].equals("-t") || argv[i].equals("--host")) {
                if(++i >= argv.length) {
                    showUsage();
                }
                configDto.setHostRecord(argv[i]);
            } else {
                showUsage();
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
        System.err.print("Usage: TcpForward [OPTION]\n" +
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
