package com.leonardo.prices.infrastructure.adapter.in.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * clase para los tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Price Controller - Integration Tests")
class PriceControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/prices";
    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test 1: 10:00 on June 14 -> price list 1 (35.50 EUR)")
    void test1_june14_at10h_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    @DisplayName("Test 2: 16:00 on June 14 -> price list 2 (25.45 EUR, higher priority)")
    void test2_june14_at16h_shouldReturnPriceList2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2020-06-14T16:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.price").value(25.45));
    }

    @Test
    @DisplayName("Test 3: 21:00 on June 14 -> price list 1 (35.50 EUR, only active tariff)")
    void test3_june14_at21h_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2020-06-14T21:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    @DisplayName("Test 4: 10:00 on June 15 -> price list 3 (30.50 EUR, higher priority)")
    void test4_june15_at10h_shouldReturnPriceList3() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2020-06-15T10:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(3))
                .andExpect(jsonPath("$.price").value(30.50));
    }

    @Test
    @DisplayName("Test 5: 21:00 on June 16 -> price list 4 (38.95 EUR, higher priority)")
    void test5_june16_at21h_shouldReturnPriceList4() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2020-06-16T21:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(4))
                .andExpect(jsonPath("$.price").value(38.95));
    }

    @Test
    @DisplayName("Returns 404 when no price is found for the given parameters")
    void shouldReturn404WhenNoPriceFound() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("applicationDate", "2019-01-01T00:00:00")
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .param("brandId", String.valueOf(BRAND_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Returns 400 when required parameters are missing")
    void shouldReturn400WhenParametersMissing() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("productId", String.valueOf(PRODUCT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
