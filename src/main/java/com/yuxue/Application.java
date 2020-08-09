package com.yuxue;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

/**
 * spring boot 启动类
 * @author yuxue
 * @date 2019-12-06
 */
@SpringBootApplication
@MapperScan("mapper")
@EnableScheduling //开启对定时任务的支持
@Slf4j
public class Application {

	public static void main(String[] args) {
		
		String version = System.getProperty("java.version");
        if (Integer.parseInt(
                version.substring(0,1)) == 1 
                && Integer.parseInt(version.substring(2, 3)) >= 8 
                && Integer.parseInt(version.substring(6)) >= 60 
                || Integer.parseInt(version.substring(0,1))>=9) {
            SpringApplication.run(Application.class, args);
        } else {
            log.error("java version need greater than 1.8.60, and do not use open jdk !!!");
        }
	}
	

}
