package com.xm.crypto.controller;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Flux<PriceRangeDetails> getCryptosRank() {
        return cryptoService.rankCryptos();
    }

    @ExceptionHandler({UnknownSymbolRuntimeException.class})
    public ResponseEntity<String> handleUnknownSymbolException() {
        return ResponseEntity.notFound().build();
    }
}
