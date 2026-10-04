package net.inpercima.fintrack.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GlsProperties.class)
public class GlsConfiguration {
}
