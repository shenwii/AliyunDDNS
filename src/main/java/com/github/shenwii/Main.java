package com.github.shenwii;

import com.aliyun.tea.utils.StringUtils;
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
        String configPath = null;
        for(int i = 0; i < argv.length; i++) {
            if(argv[i].equals("-c")) {
                if(++i >= argv.length) {
                    showUsage();
                    System.exit(1);
                }
                configPath = argv[i];
            } else if(argv[i].equals("-h")) {
                showUsage();
                System.exit(0);
            } else {
                showUsage();
                System.exit(1);
            }
        }
        if(configPath == null) {
            showUsage();
            System.exit(1);
        }
        final ConfigDto configDto = readConfigFile(configPath);
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
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    final DDnsClient client = new DDnsClient(
                            configDto.getAccessKeyId()
                            ,configDto.getAccessKeySecret()
                            ,configDto.getRegionId()
                            ,configDto.getDomainName()
                            ,configDto.getHostRecord()
                    );
                    client.updateDomain();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0L, configDto.getPeriod());
    }

    private static ConfigDto readConfigFile(String filePath) throws IOException {
        try(InputStream is = new FileInputStream(filePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(is, ConfigDto.class);
        }
    }

    /**
     * 打印使用方法
     */
    public static void showUsage() {
        System.err.print("Usage: TcpForward [OPTION]\n" +
                "  -c config_file            config file path\n" +
                "  -h                        print this help\n");
    }
}
