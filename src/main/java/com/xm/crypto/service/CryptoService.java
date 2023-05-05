package com.xm.crypto.service;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CryptoService {

    /**
     * Calculates price details for a given cryptocurrency symbol.
     *
     * @param cryptoSymbol cryptocurrency symbol for example BTC or XRP
     * @return min, max, first and last price for a given cryptocurrency
     * @throws UnknownSymbolRuntimeException for an unknown cryptocurrency symbol
     */
    Mono<PriceRangeDetails> calculatePriceRangeDetails(String cryptoSymbol) throws UnknownSymbolRuntimeException;

    /**
     * Returns cryptocurrencies ranked descending by normalized range
     *
     * @param limit limits size of returned ranking
     * @param dateRange limits ranking only to a specific time period
     */
    Flux<PriceRangeDetails> rankCryptos(Integer limit, DateRange dateRange);
}
