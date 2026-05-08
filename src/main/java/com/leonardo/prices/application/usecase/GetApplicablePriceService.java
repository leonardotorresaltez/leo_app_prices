package com.leonardo.prices.application.usecase;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.domain.model.PriceNotFoundException;
import com.leonardo.prices.domain.port.in.GetApplicablePriceUseCase;
import com.leonardo.prices.domain.port.out.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * clase de servicio,implementa puerto de entrada
 */
@Service
public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepository priceRepository;

    public GetApplicablePriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return priceRepository
                .findApplicablePrice(applicationDate, productId, brandId)
                .orElseThrow(() -> new PriceNotFoundException(
                        productId, brandId, applicationDate.toString()
                ));
    }
}
