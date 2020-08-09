package com.yuxue.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * controller 层日志aop
 * @author yuxue
 * @date 2018-09-07
 */
@Aspect
@Slf4j
@Component
public class WebAop {

	@Pointcut("execution(* com.yuxue.controller..*.*(..))")
	public void webLog() {}
	
	@Before("webLog()")
	public void doBefore(JoinPoint joinPoint) throws Throwable {
		// 接收到请求，记录请求内容
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		
		log.info("====================");
		log.info("Cookie: " + request.getHeader("Cookie"));
		log.info(request.getMethod() + "=>" + request.getRequestURL().toString());
		log.info("IP: " + request.getRemoteAddr());
		log.info("CLASS_METHOD: "
						+ joinPoint.getSignature().getDeclaringTypeName()
						+ "."
						+ joinPoint.getSignature().getName());
		log.info("ARGS: " + Arrays.toString(joinPoint.getArgs()));
		log.info("====================\n");
	}

	
	@AfterReturning(returning = "ret", pointcut = "webLog()")
	public void doAfterReturning(Object ret) throws Throwable {
		// 关闭:  返回前进行内容结果日志输出
		log.info("RESPONSE: " + ret);
		log.info("====================\n");
	}
	
}
