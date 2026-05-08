package com.leonardo.prices.domain.port.out;

import com.leonardo.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * interfaz puerto de salida
 */
public interface PriceRepository {

    Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
