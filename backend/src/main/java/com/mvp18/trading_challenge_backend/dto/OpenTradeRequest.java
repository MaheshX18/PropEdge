package com.mvp18.trading_challenge_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OpenTradeRequest {

    @NotNull(message = "Challenge attempt ID is required")
    private Long challengeAttemptId;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Direction is required")
    @Pattern(regexp = "^(BUY|SELL)$",
            message = "Direction must be BUY or SELL")
    private String direction;

    @NotNull(message = "Lot size is required")
    @DecimalMin(value = "0.01",
            message = "Minimum lot size is 0.01")
    @DecimalMax(value = "100.00",
            message = "Maximum lot size is 100")
    private BigDecimal lotSize;

    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
}