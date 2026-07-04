package com.youran.bookkeeping.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${avatar.storage-dir:${user.home}/.youran/avatars}")
    private String avatarStorageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(avatarStorageDir).toAbsolutePath().normalize().toUri().toString();
        // 支持直接访问和通过网关 /api/auth 路由访问
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
        registry.addResourceHandler("/api/auth/avatars/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
        // 也支持通过 /api/avatars 路由访问（兼容旧路径）
        registry.addResourceHandler("/api/avatars/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
    }
}
