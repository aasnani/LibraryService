package com.library.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "library.loans")
@Getter
@Setter
public class LoanPolicyProperties {

    @Min(0)
    private int maxActive;

    @Min(0)
    private int overdueBlockThreshold;

    @Min(1)
    private int loanDurationDays;
}