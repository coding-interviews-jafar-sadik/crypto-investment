package com.xm.crypto.service;

import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CryptoService {

    /**
     * Calculates price details for a given cryptocurrency symbol.
     * @param cryptoSymbol cryptocurrency symbol for example BTC or XRP
     * @return min, max, first and last price for a given cryptocurrency
     * @throws UnknownSymbolRuntimeException for an unknown cryptocurrency symbol
     */
    Mono<PriceRangeDetails> getPriceDetails(String cryptoSymbol) throws UnknownSymbolRuntimeException;

    /**
     * Returns cryptocurrencies ranked descending by normalized range
     */
    Flux<PriceRangeDetails> rankCryptos();
}
