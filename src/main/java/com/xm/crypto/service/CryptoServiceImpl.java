package com.xm.crypto.service;

import com.xm.crypto.dto.DateRange;
import com.xm.crypto.dto.PriceRangeDetails;
import com.xm.crypto.exceptions.UnknownSymbolRuntimeException;
import com.xm.crypto.repository.CryptoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CryptoServiceImpl implements CryptoService {

    private final CryptoRepository cryptoRepository;

    public CryptoServiceImpl(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public Mono<PriceRangeDetails> calculatePriceRangeDetails(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        return calculatePriceRangeDetails(cryptoSymbol, DateRange.unbounded());
    }

    @Override
    public Flux<PriceRangeDetails> rankCryptos(Integer limit, DateRange dateRange) {
        return Flux.fromIterable(cryptoRepository.getSupportedSymbols())
                .flatMap(symbol -> calculatePriceRangeDetails(symbol, dateRange))
                .sort((o1, o2) -> Float.compare(o2.normalizedRange(), o1.normalizedRange()))
                .take(limit);
    }

    private Mono<PriceRangeDetails> calculatePriceRangeDetails(String cryptoSymbol, DateRange dateRange) throws UnknownSymbolRuntimeException {
        ensureCryptoSymbolIsSupported(cryptoSymbol);
        return cryptoRepository.loadPriceHistory(cryptoSymbol.toUpperCase(), dateRange)
                .reduce(new MutablePriceRangeDetails(), (acc, value) -> {
                    acc.minPrice = acc.hasMin() ? acc.minPrice.min(value.getPrice()) : value.getPrice();
                    acc.maxPrice = acc.hasMax() ? acc.maxPrice.max(value.getPrice()) : value.getPrice();
                    acc.oldestPrice = acc.hasOldestPrice() ? acc.oldestPrice : value.getPrice();
                    acc.newestPrice = value.getPrice();
                    return acc;
                })
                .filter(MutablePriceRangeDetails::isInitializedWithAtLeastOnePriceSnapshot)
                .map(it -> new PriceRangeDetails(cryptoSymbol.toUpperCase(), it.oldestPrice, it.newestPrice, it.minPrice, it.maxPrice));
    }

    private void ensureCryptoSymbolIsSupported(String cryptoSymbol) throws UnknownSymbolRuntimeException {
        if (!cryptoRepository.getSupportedSymbols().contains(cryptoSymbol.toUpperCase())) {
            throw new UnknownSymbolRuntimeException(cryptoSymbol);
        }
    }

    private static class MutablePriceRangeDetails {
        BigDecimal oldestPrice;

        BigDecimal newestPrice;

        BigDecimal minPrice;

        BigDecimal maxPrice;

        boolean hasOldestPrice() {
            return oldestPrice != null;
        }

        boolean hasMin() {
            return minPrice != null;
        }

        boolean hasMax() {
            return maxPrice != null;
        }

        boolean isInitializedWithAtLeastOnePriceSnapshot() {
            return hasMin() && hasMax();
        }
    }
}
