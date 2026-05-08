package com.leonardo.prices.infrastructure.adapter.out.persistence;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.domain.port.out.PriceRepository;
import com.leonardo.prices.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper;
import com.leonardo.prices.infrastructure.adapter.out.persistence.repository.SpringDataPriceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * repo implementa puerto de salida
 */
@Component
public class PriceRepositoryAdapter implements PriceRepository {

    private final SpringDataPriceRepository springDataPriceRepository;
    private final PriceEntityMapper mapper;

    public PriceRepositoryAdapter(SpringDataPriceRepository springDataPriceRepository,
                                  PriceEntityMapper mapper) {
        this.springDataPriceRepository = springDataPriceRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Price> findApplicablePrice(LocalDateTime applicationDate,
                                               Long productId,
                                               Long brandId) {
        return springDataPriceRepository
                .findTopApplicablePrice(applicationDate, productId, brandId)
                .map(mapper::toDomain);
    }
}
