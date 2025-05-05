package org.example;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:credentials.properties")
@ConfigurationProperties
@Getter
@Setter
public class CredentialsProperties {
    private String telegramBotToken;
    private Long adminId;
}
