package com.leonardo.prices.infrastructure.adapter.in.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Price Controller - Integration Tests")
class PriceControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/prices";
    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    /////////////////  Escenarios para probar prioridad

    @ParameterizedTest(name = "[{index}] {0} -> price list {1}, {2} EUR")
    @MethodSource("requiredPriceScenarios")
    @DisplayName("Escenarios requeridos: verifica tarifa, precio, fechas y moneda")
    void requiredScenarios(String applicationDate, int expectedPriceList, double expectedPrice,
                           String expectedStartDate, String expectedEndDate) throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", applicationDate)
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(expectedPriceList))
                .andExpect(jsonPath("$.price").value(expectedPrice))
                .andExpect(jsonPath("$.startDate").value(expectedStartDate))
                .andExpect(jsonPath("$.endDate").value(expectedEndDate))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    static Stream<Arguments> requiredPriceScenarios() {
        return Stream.of(
            // Test 1: 10:00 del 14 -> solo lista 1 activa (prioridad 0)
            Arguments.of("2020-06-14T10:00:00", 1, 35.50, "2020-06-14T00:00:00", "2020-12-31T23:59:59"),
            // Test 2: 16:00 del 14 -> listas 1 y 2 activas; lista 2 gana por prioridad (1 > 0)
            Arguments.of("2020-06-14T16:00:00", 2, 25.45, "2020-06-14T15:00:00", "2020-06-14T18:30:00"),
            // Test 3: 21:00 del 14 -> lista 2 ya cerró a las 18:30; solo lista 1
            Arguments.of("2020-06-14T21:00:00", 1, 35.50, "2020-06-14T00:00:00", "2020-12-31T23:59:59"),
            // Test 4: 10:00 del 15 -> listas 1 y 3 activas; lista 3 gana por prioridad (1 > 0)
            Arguments.of("2020-06-15T10:00:00", 3, 30.50, "2020-06-15T00:00:00", "2020-06-15T11:00:00"),
            // Test 5: 21:00 del 16 -> listas 1 y 4 activas; lista 4 gana por prioridad (1 > 0)
            Arguments.of("2020-06-16T21:00:00", 4, 38.95, "2020-06-15T16:00:00", "2020-12-31T23:59:59")
        );
    }

    ////////  Condiciones de frontera, limite

    @Nested
    @DisplayName("Condiciones de frontera en rangos de fechas")
    class BoundaryConditions {

        @Test
        @DisplayName("Al inicio exacto de lista 2 (15:00:00), la mayor prioridad aplica")
        void atExactStartOfPriceList2_shouldApplyHigherPriority() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2020-06-14T15:00:00")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(2))
                    .andExpect(jsonPath("$.price").value(25.45));
        }

        @Test
        @DisplayName("Al fin exacto de lista 2 (18:30:00), la mayor prioridad todavía aplica")
        void atExactEndOfPriceList2_shouldApplyHigherPriority() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2020-06-14T18:30:00")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(2))
                    .andExpect(jsonPath("$.price").value(25.45));
        }

        @Test
        @DisplayName("Un segundo antes de lista 2 (14:59:59), solo la tarifa base aplica")
        void oneSecondBeforePriceList2_shouldReturnBaseTariff() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2020-06-14T14:59:59")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50));
        }

        @Test
        @DisplayName("Un segundo después de lista 2 (18:30:01), vuelve la tarifa base")
        void oneSecondAfterPriceList2_shouldReturnBaseTariff() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2020-06-14T18:30:01")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50));
        }
    }

    ///////////////// Escenarios de error 

    @Nested
    @DisplayName("Escenarios de error")
    class ErrorScenarios {

        @Test
        @DisplayName("Devuelve 404 cuando la fecha está fuera de todos los rangos activos")
        void dateOutsideAllRanges_shouldReturn404() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2019-01-01T00:00:00")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("Devuelve 404 cuando el producto no existe para la marca dada")
        void unknownProduct_shouldReturn404() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", "99999")
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Devuelve 400 cuando el formato de fecha es inválido")
        void malformedDate_shouldReturn400() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("applicationDate", "14-06-2020 10:00")
                            .param("productId", String.valueOf(PRODUCT_ID))
                            .param("brandId", String.valueOf(BRAND_ID)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devuelve 400 cuando faltan parámetros requeridos")
        void missingRequiredParameters_shouldReturn400() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", String.valueOf(PRODUCT_ID)))
                    .andExpect(status().isBadRequest());
        }
    }
}
