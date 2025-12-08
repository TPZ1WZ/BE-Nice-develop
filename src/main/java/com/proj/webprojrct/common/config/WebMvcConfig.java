package com.proj.webprojrct.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Removed JSP view resolver configuration as we're using Thymeleaf now

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Gán theo từng “root” chuẩn — tất cả nằm dưới /static/
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/banners/**")
                .addResourceLocations("classpath:/static/banners/");

        registry.addResourceHandler("/bootstrap/**")
                .addResourceLocations("classpath:/static/bootstrap/");

        registry.addResourceHandler("/plugins/**")
                .addResourceLocations("classpath:/static/plugins/");

        registry.addResourceHandler("/dist/**")
                .addResourceLocations("classpath:/static/dist/");

        // Tuỳ chọn: fallback cho mọi thứ còn lại trong /static/
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Register view controllers for decorator templates
        registry.addViewController("/decorators/main-decorator").setViewName("decorators/main-decorator");
        registry.addViewController("/decorators/admin-decorator").setViewName("decorators/admin-decorator");
        registry.addViewController("/decorators/checkout-decorator").setViewName("decorators/checkout-decorator");
    }
}
