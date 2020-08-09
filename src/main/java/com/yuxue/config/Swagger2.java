package com.yuxue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * 在swagger-annotations jar包中 1.5.X版本以上, 注解 io.swagger.annotations.API 中的description被废弃了。
 * 新的swagger组件中使用了新的方法来对Web api 进行分组。原来使用 description ，默认一个Controller类中包含的方法构成一 个api分组。
 * 现在使用tag，可以更加方便的分组。
 * 比如把两个Controller类里的方法划分成同一个分组。tag的key用来区分不同的分组。tag的value用做分组的描述。
 * @ApiOperation 中value是api的简要说明，在界面api 链接的右侧，少于120个字符。
 * @ApiOperation 中notes是api的详细说明，需要点开api 链接才能看到。
 * @ApiOperation 中 produces 用来标记api返回值的具体类型。这里是json格式，utf8编码。
 */
/**
 * 集成swagger2 接口管理文档
 * @author yuxue
 * @date 2018-09-07
 */
@Configuration
@EnableSwagger2
public class Swagger2 {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Image Recognition API")
                .description("图像识别技术")
                .version("1.0.0")
                .build();
    }
}
