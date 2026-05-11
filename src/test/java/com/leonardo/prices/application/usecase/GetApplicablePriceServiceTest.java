package com.leonardo.prices.application.usecase;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.domain.model.PriceNotFoundException;
import com.leonardo.prices.domain.port.out.PriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetApplicablePriceService - Unit Tests")
class GetApplicablePriceServiceTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);
    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private GetApplicablePriceService service;

    @Test
    @DisplayName("Devuelve el precio cuando el repositorio encuentra una coincidencia")
    void shouldReturnPriceWhenRepositoryFindsIt() {
        Price expected = new Price(PRODUCT_ID, BRAND_ID, 1,
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new BigDecimal("35.50"), "EUR");

        given(priceRepository.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .willReturn(Optional.of(expected));

        Price result = service.getApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isSameAs(expected);
        verify(priceRepository).findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
    }

    @Test
    @DisplayName("Lanza PriceNotFoundException cuando no existe precio para los parámetros dados")
    void shouldThrowPriceNotFoundExceptionWhenNoMatch() {
        given(priceRepository.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining(String.valueOf(PRODUCT_ID))
                .hasMessageContaining(String.valueOf(BRAND_ID));
    }
}
