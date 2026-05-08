package com.leonardo.prices.domain.model;

/**
 * excepcion custom si el precio no existe
 */
public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long productId, Long brandId, String applicationDate) {
        super(String.format(
            "No price found for productId=%d, brandId=%d, applicationDate=%s",
            productId, brandId, applicationDate
        ));
    }
}
