package com.xm.crypto.repository;

import com.xm.crypto.dto.PriceSnapshot;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CryptoRepository {

    /**
     * Returns all currently supported cryptocurrency symbols
     */
    List<String> getSupportedSymbols();

    /**
     * Loads full price history for a given cryptocurrency
     *
     * @param cryptoSymbol cryptocurrency symbol
     * @return full price history
     */
    Flux<PriceSnapshot> loadFullPriceHistory(String cryptoSymbol);

}
