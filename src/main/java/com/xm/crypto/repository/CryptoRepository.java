package com.xm.crypto.repository;

import com.xm.crypto.dto.DateRange;
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
     * @param dateRange    the date range to search for price history
     * @return full price history
     */
    Flux<PriceSnapshot> loadPriceHistory(String cryptoSymbol, DateRange dateRange);
}
