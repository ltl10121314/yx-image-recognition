package com.yuxue.exception;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import com.yuxue.entity.Result;


/**
 * 捕获RestController抛出的异常
 * @author yuxue
 * @date 2018-09-06
 */
@RestControllerAdvice
public class ResultReturnExceptionHandler {

	protected static Logger log=LoggerFactory.getLogger(ResultReturnExceptionHandler.class);    

	/** 捕捉shiro的异常 *//*
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(ShiroException.class)
	public Result handle401(ShiroException e) {
		log.error(e.getMessage(), e);
		return Result.error(ErrorEnum.UNAUTHORIZED);
	}

	*//** 捕捉UnauthorizedException *//*
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(UnauthorizedException.class)
	public Result handle401() {
		return Result.error(ErrorEnum.UNAUTHORIZED);
	}*/

	/** 文件上传大小异常 */
	@ExceptionHandler(MultipartException.class)
	public Result handleMultipart(Throwable t) {
		log.error(t.getMessage(), t);
		return Result.error(ErrorEnum.UPLOAD_FILE_SIZE_MAX);
	}

	/** jackson转换Bean * */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result handleJsonConv(Throwable t) {
		log.error(t.getMessage(), t);
		return Result.error(ErrorEnum.COMMON_PARAMS_NOT_EXIST);
	}

	 /** 异常参数处理器 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleRRException(Throwable e) {
        //log.error(e.getMessage(), e);
        return Result.error(ErrorEnum.COMMON_PARAMS_ERR.code, e.getMessage());
    }
    
	/** 自定义异常  */
	@ExceptionHandler(ResultReturnException.class)
	public Result handleRRException(ResultReturnException e) {
		log.error(exTraceBack(e), e);
		return Result.error(e.getCode(), e.getMsg());
	}

	@ExceptionHandler(Exception.class)
	public Result handleException(Exception e) {
		log.error(exTraceBack(e), e);
		return Result.error("系统发生错误，请联系管理员");
	}

	public static String exTraceBack(Exception e) {
		StringBuilder sb = new StringBuilder();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			sb.append("<---");
			sb.append(String.format("[%s * %s]  ", stackTrace[i].getClassName(), stackTrace[i].getMethodName()));
		}
		sb.append(e.getMessage());
		return sb.toString();
	}
}
