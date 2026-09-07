package org.saavy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"org.saavy"})
@EntityScan(basePackages = {"org.saavy"})
@EnableJpaRepositories(basePackages = {"org.saavy"})
@ConfigurationPropertiesScan(basePackages = {"org.saavy"})
public class SpringBootVueApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootVueApplication.class, args);
    }

}