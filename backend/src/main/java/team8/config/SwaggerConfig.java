package team8.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Block Coding Application API")
                        .version("1.0")
                        .description("스크래치/엔트리 스타일의 블록 코딩 애플리케이션 REST API"));
    }
}
