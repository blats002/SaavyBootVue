package org.saavy.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "org.saavy")
@EnableJpaRepositories(basePackages = "org.saavy")
@EntityScan(basePackages = "org.saavy")
public class TaxPluginConfiguration {
}
