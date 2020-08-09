package com.yuxue.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 自定义方法注解
 * controller层api，如果添加了该注解，则不进行返回值封装
 * 即：返回值封装排除注解
 * @author yuxue
 * @date 2019-08-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetExclude {
	
	String value() default "";
	
}
