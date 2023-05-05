package com.xm.crypto.controller;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.service.CryptoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/cryptos")
public class RecommendationController {

    private final CryptoService cryptoService;

    public RecommendationController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/{cryptoSymbol}")
    public Mono<PriceRangeDetails> getPriceRangeDetails(@PathVariable String cryptoSymbol) {
        return cryptoService.calculatePriceRangeDetails(cryptoSymbol);
    }

    @GetMapping
    public Flux<PriceRangeDetails> getCryptosRank(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "from_date", required = false) Optional<LocalDate> fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "to_date", required = false) Optional<LocalDate> toDate,
            @RequestParam(name = "limit", required = false) Optional<Integer> limit
    ) {
        LocalDate YEAR_1900 = LocalDate.of(1900, 1, 1);
        LocalDate YEAR_9999 = LocalDate.of(9999, 1, 1);

        return cryptoService.rankCryptos(limit.orElse(Integer.MAX_VALUE),
                new DateRange(fromDate.orElse(YEAR_1900), toDate.orElse(YEAR_9999))
        );
    }

    @ExceptionHandler({UnknownSymbolRuntimeException.class})
    public ResponseEntity<String> handleUnknownSymbolException() {
        return ResponseEntity.notFound().build();
    }
}
