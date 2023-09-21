package com.yxs.friend.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

//@Configuration
//@EnableSwagger2WebMvc
public class SwaggerConfig extends WebMvcConfigurationSupport {

//
//    @Bean(value = "defaultApi2")
//    public Docket defaultApi2() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                // 这里一定要标注你控制器的位置
//                .apis(RequestHandlerSelectors.basePackage("com.yxs.friend.controller.UserController"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//
//    /**
//     * api 信息
//     * @return
//     */
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("伙伴匹配系统")
//                .description("伙伴匹配系统接口文档")
//                .termsOfServiceUrl("https://github.com/DengFengLai66")
//                .contact(new Contact("yxs","https://github.com/DengFengLai66","2390764121@qq.com"))
//                .version("1.0")
//                .build();
//    }

        public Docket docket() {
            ApiInfo apiInfo = new ApiInfoBuilder()
                    .title("伙伴匹配系统")
                    .version("2.0")
                    .description("伙伴匹配系统接口文档")
                    .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yxs.friend.controller.UserController"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    protected void addResourceHandlers(ResourceHandlerRegistry registy){
            registy.addResourceHandler("/doc.html").addResourceLocations("classpath:/METH-INF/resources/");
            registy.addResourceHandler("/webjars/**").addResourceLocations("classpath:/METH-INF/resources/webjars/");


    }
}
