package com.yuxue.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


/**
 * 配置自动启动浏览器
 * 
 */
@Slf4j
@Component
public class CommandRunner implements CommandLineRunner {

    @Value("${server.port}")
    private String port;

    @Override
    public void run(String... args) {
        try {
        	String os = System.getProperty("os.name").toLowerCase();
        	if(os.contains("windows")) {
        	    // 默认浏览器打开
        		// Runtime.getRuntime().exec("cmd   /c   start   http://localhost:" + port + "/index");
        	}
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("打开默认浏览器异常", ex);
        }
    }
}
