package io.securecodebox.zap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@ComponentScan(basePackages = {"de.otto.edison", "io.securecodebox.zap"})
@PropertySource("version.properties")  // Add a additional property file
@EnableAutoConfiguration
@EnableScheduling
@EnableSwagger2
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SecureCodeBox ZAP API")
                .termsOfServiceUrl("https://securecodebox.io")
                .license("iteratec GmbH 2018")
                .version("1.0").build();
    }

    @Bean
    public Docket zapApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("zap-api").apiInfo(apiInfo())
                .select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any())
                .build();
    }
}
