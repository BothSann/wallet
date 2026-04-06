package com.bothsann.wallet.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "application.wallet")
@Getter
@Setter
public class WalletProperties {
    private BigDecimal maxDailyLimit;
}
