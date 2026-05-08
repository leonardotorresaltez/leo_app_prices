package com.leonardo.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * clase de modelo sin dependencias, tambien se puede usar loombok
 */
public class Price {

    private final Long productId;
    private final Long brandId;
    private final Integer priceList;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final BigDecimal amount;
    private final String currency;
    private final Integer priority;

    public Price(Long productId, Long brandId, Integer priceList,
                 LocalDateTime startDate, LocalDateTime endDate,
                 BigDecimal amount, String currency, Integer priority) {
        this.productId = productId;
        this.brandId = brandId;
        this.priceList = priceList;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.currency = currency;
        this.priority = priority;
    }

    public Long getProductId() { return productId; }
    public Long getBrandId() { return brandId; }
    public Integer getPriceList() { return priceList; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Integer getPriority() { return priority; }
}
