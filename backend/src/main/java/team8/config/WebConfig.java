package team8.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")                                      // 모든 API 경로
        .allowedOriginPatterns("*")                              // 포트에 상관없이 허용
        .allowedMethods("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS") // HTTP 메서드 허용
        .allowedHeaders("*")                                     // 모든 헤더 허용
        .allowCredentials(true);                                 // 쿠키 인증 요청 허용
  }
}
