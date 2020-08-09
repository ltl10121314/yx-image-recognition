package com.yuxue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;



/**
 * 自定义配置
 * @author yuxue
 * @date 2019-06-13
 */
@Configuration
public class DefaultMvcConfig {

	/**
	 * 国际化配置
	 * 设置区域信息
	 * @return
	 */
	@Bean
    public LocaleResolver localeResolver(){
        return new MyLocaleResolver();
    }
}
