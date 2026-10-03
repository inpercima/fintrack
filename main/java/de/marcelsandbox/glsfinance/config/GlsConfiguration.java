package de.marcelsandbox.glsfinance.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GlsProperties.class)
public class GlsConfiguration {
}
