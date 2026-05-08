package com.leonardo.prices.infrastructure.adapter.in.rest.mapper;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import org.springframework.stereotype.Component;

@Component
public class PriceResponseMapper {

    public PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.getProductId(),
                price.getBrandId(),
                price.getPriceList(),
                price.getStartDate(),
                price.getEndDate(),
                price.getAmount(),
                price.getCurrency()
        );
    }
}
