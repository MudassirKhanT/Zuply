//package com.zuply.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.nio.file.Paths;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Value("${upload.path:./uploads}")
//    private String uploadPath;
//
//    @Value("${cors.allowed-origins:https://zingy-axolotl-993600.netlify.app}")
//    private String corsAllowedOrigins;
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        String[] origins = corsAllowedOrigins.split(",");
//        registry.addMapping("/api/**")
//                .allowedOriginPatterns(origins)
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toString().replace("\\", "/");
//        String location = "file:///" + absolutePath;
//        if (!location.endsWith("/")) location += "/";
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations(location);
//    }
//}
package com.zuply.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${cors.allowed-origins:https://zingy-axolotl-993600.netlify.app,http://localhost:4200}")
    private String corsAllowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toString().replace("\\", "/");
        String location = "file:///" + absolutePath;
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}