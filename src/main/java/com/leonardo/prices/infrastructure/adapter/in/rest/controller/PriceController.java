package com.leonardo.prices.infrastructure.adapter.in.rest.controller;

import com.leonardo.prices.domain.model.Price;
import com.leonardo.prices.domain.port.in.GetApplicablePriceUseCase;
import com.leonardo.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import com.leonardo.prices.infrastructure.adapter.in.rest.mapper.PriceResponseMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 
 */
@RestController
@RequestMapping("/api/v1/prices")
@Validated
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;
    private final PriceResponseMapper mapper;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase,
                           PriceResponseMapper mapper) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime applicationDate,

            @RequestParam @NotNull Long productId,
            @RequestParam @NotNull Long brandId
    ) {
        Price price = getApplicablePriceUseCase.getApplicablePrice(
                applicationDate, productId, brandId
        );
        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
