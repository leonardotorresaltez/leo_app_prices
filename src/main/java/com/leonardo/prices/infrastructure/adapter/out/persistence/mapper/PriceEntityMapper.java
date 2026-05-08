package com.leonardo.prices.infrastructure.adapter.out.persistence.mapper;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import org.springframework.stereotype.Component;

/**
 * mapper para convertir entiedades, se puede usar tambien maspstruct
 */
@Component
public class PriceEntityMapper {

    public Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getProductId(),
                entity.getBrandId(),
                entity.getPriceList(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getPriority()
        );
    }
}
