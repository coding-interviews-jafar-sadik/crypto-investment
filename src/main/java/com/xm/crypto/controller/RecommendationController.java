package com.xm.crypto.controller;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.service.CryptoService;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/cryptos")
@Api("Cryptocurrency recommendation API")
public class RecommendationController {

    private final CryptoService cryptoService;

    public RecommendationController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/{cryptoSymbol}")
    @ApiOperation(value = "Endpoint returns cryptocurrency details such as min price, max price, first price and last price")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved cryptocurrency details"),
            @ApiResponse(code = 404, message = "Invalid or not supported cryptocurrency symbol")
    })
    public Mono<PriceRangeDetails> getPriceRangeDetails(
            @ApiParam(value = "Cryptocurrency symbol", example = "BTC") @PathVariable String cryptoSymbol
    ) {
        return cryptoService.calculatePriceRangeDetails(cryptoSymbol);
    }

    @GetMapping
    @ApiOperation(value = "Endpoint returns a descending sorted list of all cryptos compared by the normalized range (max - min)  / min. " +
            "It allows to optionally limit the result size and perform calculations only on a given time period.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Invalid query parameter.")
    })
    public Flux<PriceRangeDetails> getCryptosRank(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "from_date", required = false)
            @ApiParam(value = "Lower bound of date range", example = "2020-01-15")
            Optional<LocalDate> fromDate,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(name = "to_date", required = false)
            @ApiParam(value = "Upper bound of date range", example = "2023-06-01")
            Optional<LocalDate> toDate,

            @RequestParam(name = "limit", required = false)
            @ApiParam(value = "Limits the size of cryptocurrency ranking")
            Optional<Integer> limit
    ) {
        return cryptoService.rankCryptos(limit.orElse(Integer.MAX_VALUE),
                new DateRange(fromDate.orElse(DateRange.MIN), toDate.orElse(DateRange.MAX))
        );
    }

    @ExceptionHandler({UnknownSymbolRuntimeException.class})
    public ResponseEntity<String> handleUnknownSymbolException() {
        return ResponseEntity.notFound().build();
    }
}
