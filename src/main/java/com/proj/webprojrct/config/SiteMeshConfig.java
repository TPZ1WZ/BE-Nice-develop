package com.proj.webprojrct.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * SiteMesh Configuration
 * This configuration sets up the SiteMesh filter for decorating HTML pages with common layouts
 */
//@Configuration
public class SiteMeshConfig {

    //@Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        // Temporarily disable SiteMesh to test if it's causing the 400 error
        filterRegistrationBean.setEnabled(false);
        filterRegistrationBean.setFilter(new ConfigurableSiteMeshFilter() {
            @Override
            protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                // Map specific decorators to paths
                builder
                        // Apply main decorator to most pages
                        .addDecoratorPath("/*", "/decorators/main-decorator")

                        // Apply admin decorator to admin pages
                        .addDecoratorPath("/admin*", "/decorators/admin-decorator")
                        .addDecoratorPath("/admin/*", "/decorators/admin-decorator")
                        .addDecoratorPath("/admin/**", "/decorators/admin-decorator")

                        // Apply checkout decorator to checkout pages
                        .addDecoratorPath("/checkout*", "/decorators/checkout-decorator")
                        .addDecoratorPath("/cart/checkout*", "/decorators/checkout-decorator")

                        // Instead of excluding, create specific decorator paths for auth pages
                        .addDecoratorPath("/login*", "/decorators/main-decorator")
                        .addDecoratorPath("/register*", "/decorators/main-decorator")

                        // Exclude paths that should not be decorated
                        .addExcludedPath("/api/*")
                        .addExcludedPath("/api/**")
                        .addExcludedPath("/resources/*")
                        .addExcludedPath("/resources/**")
                        .addExcludedPath("/static/*")
                        .addExcludedPath("/static/**")
                        .addExcludedPath("/css/*")
                        .addExcludedPath("/css/**")
                        .addExcludedPath("/js/*")
                        .addExcludedPath("/js/**")
                        .addExcludedPath("/img/*")
                        .addExcludedPath("/img/**")
                        .addExcludedPath("/images/*")
                        .addExcludedPath("/dist/**")
                        .addExcludedPath("/bootstrap/*")
                        .addExcludedPath("/plugins/*")
                        .addExcludedPath("/decorators/*")
                        .addExcludedPath("/error");
            }
        });
        filterRegistrationBean.setOrder(1); // High priority
        return filterRegistrationBean;
    }
}