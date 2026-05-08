package com.leonardo.prices.domain.port.in;

import com.leonardo.prices.domain.model.Price;

import java.time.LocalDateTime;

/**
 * interfacz puerto de entrada
 */
public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
